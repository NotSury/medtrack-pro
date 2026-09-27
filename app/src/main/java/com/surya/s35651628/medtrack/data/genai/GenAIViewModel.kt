package com.surya.s35651628.medtrack.data.genai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.surya.s35651628.medtrack.BuildConfig
import com.surya.s35651628.medtrack.data.medcoach.MedCoachTip
import com.surya.s35651628.medtrack.data.medcoach.MedCoachTipRepository
import com.surya.s35651628.medtrack.data.medication.MedicationRepository
import com.surya.s35651628.medtrack.data.symptom.SymptomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// Holds all UI-related data for the GenAI section in the MedCoach screen.
data class GenAIUiState(
    // True when Gemini is currently generating a tip.
    val isLoading: Boolean = false,

    // Stores the latest generated tip shown on the screen.
    val generatedTip: String = "",

    // Stores an error message if the GenAI request fails.
    val errorMessage: String = "",

    // Stores all previous tips generated for the logged-in patient.
    val tipHistory: List<MedCoachTip> = emptyList()
)

class GenAIViewModel(
    private val medCoachTipRepository: MedCoachTipRepository,
    private val medicationRepository: MedicationRepository,
    private val symptomRepository: SymptomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenAIUiState())
    val uiState: StateFlow<GenAIUiState> = _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.apiKey
    )

    // Loads all saved GenAI tips for the logged-in patient.
    fun loadTipHistory(patientId: String) {
        // Clear the previous user's generated tip when loading a new patient's tips.
        _uiState.value = _uiState.value.copy(
            generatedTip = "",
            errorMessage = ""
        )

        // Start observing the patient's saved tips from Room through the repository.
        viewModelScope.launch {
            medCoachTipRepository
                .observeTipsForPatient(patientId)
                .collect { tips ->
                    // Update the UI whenever the tip history changes in Room.
                    _uiState.value = _uiState.value.copy(
                        tipHistory = tips
                    )
                }
        }
    }

    // Generates one personalized medication tip using Gemini.
    fun generateTip(patientId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Show loading state and clear old errors before starting the request.
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = ""
            )

            try {
                // Get patient-specific data from Room.
                // This makes the AI response more personalized instead of generic.
                val medications = medicationRepository.getMedicationsForPatient(patientId)
                val symptoms = symptomRepository.getSymptomsForPatient(patientId)

                // Convert the patient's medication list into readable text for the AI prompt.
                val medicationText = if (medications.isEmpty()) {
                    "No medications recorded."
                } else {
                    medications.joinToString(separator = "\n") { medication ->
                        "- ${medication.medicationName}, ${medication.dosage}, ${medication.frequency}, scheduled at ${medication.scheduledTime}"
                    }
                }

                // Convert recent symptom records into readable text for the AI prompt.
                val symptomText = if (symptoms.isEmpty()) {
                    "No symptoms recorded."
                } else {
                    symptoms.joinToString(separator = "\n") { symptom ->
                        "- ${symptom.category}, severity ${symptom.severity}/10, notes: ${symptom.notes}"
                    }
                }

                val prompt = """
                    You are MedCoach, a friendly medication adherence assistant.

                    Write ONE personalised medication adherence tip for this patient.
                
                    Patient medication list:
                    $medicationText
                
                    Recent symptom history:
                    $symptomText
                
                    Instructions:
                    - Make it feel personalised by mentioning at least ONE medication name from the medication list.
                    - If symptoms are available, gently acknowledge ONE recent symptom category
                    - Do not suggest changing medication dosage.
                    - Encourage the patient to follow their prescribed schedule.
                    - Mention contacting a healthcare professional if symptoms continue or feel concerning.
                    - Use a warm and encouraging tone.
                """.trimIndent()

                // Send the prompt to Gemini.
                val response = generativeModel.generateContent(
                    content {
                        text(prompt)
                    }
                )

                // Get the text output from Gemini.
                val outputText = response.text?.trim()

                // If Gemini returns nothing, stop and show an error message.
                if (outputText.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No tip was generated. Please try again."
                    )
                    return@launch
                }

                // Create a timestamp for when the tip was generated.
                val timestamp = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    Locale.getDefault()
                ).format(Date())

                // Create a MedCoachTip object so the generated tip can be saved into Room.
                val tip = MedCoachTip(
                    patientId = patientId,
                    tipText = outputText,
                    timestamp = timestamp
                )

                // Save the generated tip into Room through the repository.
                medCoachTipRepository.insertTip(tip)

                // Update the screen with the newly generated tip.
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generatedTip = outputText,
                    errorMessage = ""
                )
            } catch (exception: Exception) {
                // Handles possible errors such as no internet, invalid API key, or Gemini request failure.
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Could not generate a tip. Please check your API key or internet connection."
                )
            }
        }
    }
}

class GenAIViewModelFactory(
    private val medCoachTipRepository: MedCoachTipRepository,
    private val medicationRepository: MedicationRepository,
    private val symptomRepository: SymptomRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GenAIViewModel(
            medCoachTipRepository = medCoachTipRepository,
            medicationRepository = medicationRepository,
            symptomRepository = symptomRepository
        ) as T
    }
}

