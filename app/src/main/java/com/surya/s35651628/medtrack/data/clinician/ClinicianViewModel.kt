package com.surya.s35651628.medtrack.data.clinician
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.surya.s35651628.medtrack.BuildConfig
import com.surya.s35651628.medtrack.data.medication.MedicationRepository
import com.surya.s35651628.medtrack.data.patient.PatientRepository
import com.surya.s35651628.medtrack.data.symptom.SymptomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

// Stores the aggregate statistics shown on the clinician dashboard.
data class ClinicianStats(
    val totalPatients: Int = 0,
    val averageMedicationsPerPatient: Double = 0.0,
    val mostCommonSymptomCategory: String = "Not available",
    val averageSymptomSeverity: Double = 0.0,
    val highestAverageSeverityCategory: String = "Not available",
    val symptomCategorySummary: String = "No symptom data available.",
    val symptomNotesSummary: String = "No symptom notes available."
)

// Holds all UI state for the clinician dashboard screen.
data class ClinicianUiState(
    val isLoadingStats: Boolean = false,
    val isGeneratingInsights: Boolean = false,
    val stats: ClinicianStats = ClinicianStats(),
    val insights: List<String> = emptyList(),
    val errorMessage: String = ""
)

class ClinicianViewModel(
    private val patientRepository: PatientRepository,
    private val medicationRepository: MedicationRepository,
    private val symptomRepository: SymptomRepository
) : ViewModel() {

    // Private mutable state used inside the ViewModel.
    private val _uiState = MutableStateFlow(ClinicianUiState())

    // Public read-only state observed by the clinician dashboard screen.
    val uiState: StateFlow<ClinicianUiState> = _uiState.asStateFlow()

    // Gemini model used to generate clinician insights.
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.apiKey
    )

    // Loads all aggregate statistics from Room through the repositories.
    fun loadDashboardStats() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                isLoadingStats = true,
                errorMessage = ""
            )

            try {
                // Gets the total number of patients in the database.
                val totalPatients = patientRepository.getPatientCount()

                // Gets the average number of medications per patient.
                val averageMedications =
                    medicationRepository.getAverageMedicationsPerPatient()

                // Gets the symptom category that appears the most often.
                val mostCommonSymptom =
                    symptomRepository.getMostCommonSymptomCategory()
                        ?: "Not available"

                // Gets the average symptom severity across all patients.
                val averageSeverity =
                    symptomRepository.getAverageSymptomSeverity() ?: 0.0

                // Gets the number of reports for each symptom category.
                val categoryCounts =
                    symptomRepository.getSymptomCategoryCounts()

                // Gets the symptom category with the highest average severity.
                val highestSeverityCategory =
                    symptomRepository.getHighestAverageSeverityCategory()

                // Gets recent symptom records to provide extra context for GenAI.
                val recentSymptomsForAi =
                    symptomRepository.getRecentSymptomsForAi()

                // Converts symptom category counts into readable text for the dashboard and AI prompt.
                val categorySummary = if (categoryCounts.isEmpty()) {
                    "No symptom data available."
                } else {
                    categoryCounts.joinToString(separator = "\n") { item ->
                        "- ${item.category}: ${item.count} reports"
                    }
                }

                // Formats the highest average severity category for display.
                val highestSeverityText = if (highestSeverityCategory == null) {
                    "Not available"
                } else {
                    "${highestSeverityCategory.category} (${formatDouble(highestSeverityCategory.averageSeverity)} / 10)"
                }

                // Converts recent symptom records into safe summary text without patient identity.
                val symptomNotesSummary = if (recentSymptomsForAi.isEmpty()) {
                    "No symptom notes available."
                } else {
                    recentSymptomsForAi.joinToString(separator = "\n") { symptom ->
                        val cleanNotes = if (symptom.notes.isBlank()) {
                            "No notes provided"
                        } else {
                            symptom.notes
                        }

                        val symptomTime = extractTimeOnly(symptom.dateTime)

                        "- Category: ${symptom.category}, " +
                                "Severity: ${symptom.severity}/10, " +
                                "Time: $symptomTime, " +
                                "Notes: $cleanNotes"
                    }
                }

                // Stores all calculated statistics in one ClinicianStats object.
                val stats = ClinicianStats(
                    totalPatients = totalPatients,
                    averageMedicationsPerPatient = averageMedications,
                    mostCommonSymptomCategory = mostCommonSymptom,
                    averageSymptomSeverity = averageSeverity,
                    highestAverageSeverityCategory = highestSeverityText,
                    symptomCategorySummary = categorySummary,
                    symptomNotesSummary = symptomNotesSummary
                )

                // Updates the UI state after the statistics are loaded successfully.
                _uiState.value = _uiState.value.copy(
                    isLoadingStats = false,
                    stats = stats,
                    errorMessage = ""
                )
            } catch (exception: Exception) {
                // Shows a user-friendly error message if the statistics cannot be loaded.
                _uiState.value = _uiState.value.copy(
                    isLoadingStats = false,
                    errorMessage = "Could not load clinician dashboard statistics."
                )
            }
        }
    }

    // Sends the dashboard statistics to Gemini and asks it to find 3 patterns.
    fun findPatterns() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                isGeneratingInsights = true,
                errorMessage = "",
                insights = emptyList()
            )

            try {
                val stats = _uiState.value.stats

                val prompt = """
                    You are helping a clinician review aggregate medication and symptom data.

                    Use ONLY the data provided below.
                    Do not invent patient details.
                    You may describe possible patterns, timing trends, or notes worth clinician review.
                    Focus on time-of-day patterns, not calendar date patterns. 
                    Only mention time-of-day when it is clearly useful maybe for insight 3. Do not make every insight about time.
                    Use careful wording like "appears", "suggests" since you are actually reviewing the pattern of the data and it may not always be fully accurate.
                    
                     Aggregate dashboard data:
                    - Total patients: ${stats.totalPatients}
                    - Average medications per patient: ${formatDouble(stats.averageMedicationsPerPatient)}
                    - Most common symptom category: ${stats.mostCommonSymptomCategory}
                    - Average symptom severity: ${formatDouble(stats.averageSymptomSeverity)} out of 10
                    - Highest average severity symptom category: ${stats.highestAverageSeverityCategory}

                    Symptom category counts:
                    ${stats.symptomCategorySummary}

                    Recent symptom records without patient identity:
                    ${stats.symptomNotesSummary}

                    Task:
                    Generate exactly 3 short, data-driven observations or patterns.
                    
                    Output format:
                    1. Insight one
                    2. Insight two
                    3. Insight three
                """.trimIndent()

                val response = generativeModel.generateContent(
                    content {
                        text(prompt)
                    }
                )

                val rawText = response.text?.trim()

                // Handles the case where Gemini returns an empty response.
                if (rawText.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingInsights = false,
                        errorMessage = "No insights were generated. Please try again."
                    )
                    return@launch
                }

                // Cleans Gemini's response and keeps only the first 3 insights.
                val parsedInsights = rawText
                    .lines()
                    .map { line ->
                        line.trim()
                            .removePrefix("1.")
                            .removePrefix("2.")
                            .removePrefix("3.")
                            .removePrefix("-")
                            .trim()
                    }
                    .filter { line -> line.isNotBlank() }
                    .take(3)

                // Updates the UI with the generated insights.
                _uiState.value = _uiState.value.copy(
                    isGeneratingInsights = false,
                    insights = parsedInsights,
                    errorMessage = ""
                )
            } catch (exception: Exception) {
                // Shows an error if Gemini cannot generate the insights.
                _uiState.value = _uiState.value.copy(
                    isGeneratingInsights = false,
                    errorMessage = "Could not generate insights. Please check your API key or internet connection."
                )
            }
        }
    }

    // Formats double values to 2 decimal places.
    private fun formatDouble(value: Double): String {
        return String.format(Locale.getDefault(), "%.2f", value)
    }

    // Extracts only the time part from a date-time string.
    private fun extractTimeOnly(dateTime: String): String {
        return if (dateTime.length >= 16) {
            dateTime.substring(11, 16)
        } else {
            dateTime
        }
    }
}

class ClinicianViewModelFactory(
    private val patientRepository: PatientRepository,
    private val medicationRepository: MedicationRepository,
    private val symptomRepository: SymptomRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ClinicianViewModel(
            patientRepository = patientRepository,
            medicationRepository = medicationRepository,
            symptomRepository = symptomRepository
        ) as T
    }
}