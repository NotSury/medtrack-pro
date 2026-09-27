package com.surya.s35651628.medtrack.data.interaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.surya.s35651628.medtrack.BuildConfig
import com.surya.s35651628.medtrack.data.medication.Medication
import com.surya.s35651628.medtrack.data.medication.MedicationRepository
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// This data class is used by the UI to display one medication interaction result.
data class InteractionReviewItem(
    val firstMedication: String,
    val secondMedication: String,
    val riskLevel: String,
    val reason: String
)

class InteractionReviewViewModel(
    private val medicationRepository: MedicationRepository,
    private val interactionReviewRepository: InteractionReviewRepository
) : ViewModel() {

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    private val _reviewItems = MutableStateFlow<List<InteractionReviewItem>>(emptyList())
    val reviewItems: StateFlow<List<InteractionReviewItem>> = _reviewItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.apiKey
    )

    // Loads both the patient's medication list and any saved interaction review.
    fun loadInteractionReviewData(patientId: String) {
        loadMedications(patientId)
        loadSavedInteractionReviews(patientId)
    }

    // Loads medications from Room through the medication repository.
    private fun loadMedications(patientId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _medications.value =
                    medicationRepository.getMedicationsForPatient(patientId)
            } catch (exception: Exception) {
                _message.value = "Could not load medications for interaction review."
            }
        }
    }

    // Loads previously saved interaction reviews for the patient.
    private fun loadSavedInteractionReviews(patientId: String) {

        viewModelScope.launch {
            interactionReviewRepository
                .observeReviewsForPatient(patientId)
                .collect { savedReviews ->
                    _reviewItems.value = savedReviews.map { review ->
                        InteractionReviewItem(
                            firstMedication = review.firstMedication,
                            secondMedication = review.secondMedication,
                            riskLevel = review.riskLevel,
                            reason = review.reason
                        )
                    }

                    if (savedReviews.isNotEmpty()) {
                        _message.value =
                            "Showing latest saved AI review. Please confirm any concerns with a doctor or pharmacist."
                    }
                }
        }
    }

    // Sends the patient's medication pairs to Gemini and saves the AI review result.
    fun reviewMedicationInteractions(patientId: String) {
        val currentMedications = _medications.value

        if (currentMedications.size < 2) {
            _reviewItems.value = emptyList()
            _message.value =
                "At least two medications are needed to check possible interactions."
            return
        }

        val medicationPairs = createMedicationPairs(currentMedications)

        if (medicationPairs.isEmpty()) {
            _reviewItems.value = emptyList()
            _message.value = "No medication combinations found for review."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _message.value = ""
            _reviewItems.value = emptyList()

            try {
                // Formats the medication pairs so the AI can review each pair clearly.
                val pairText = medicationPairs.joinToString(separator = "\n") { pair ->
                    val first = pair.first
                    val second = pair.second

                    "- ${first.medicationName} (${first.dosage}, ${first.frequency}, ${first.scheduledTime}) + " +
                            "${second.medicationName} (${second.dosage}, ${second.frequency}, ${second.scheduledTime})"
                }

                val prompt = """
                    You are reviewing a patient's medication list for possible drug-drug interaction concerns.

                    Medication combinations to review:
                    $pairText

                    Important rules:
                    - Use only the medication combinations provided.
                    - Do not invent extra medications.
                    - Do not present the result as confirmed medical advice.
                    - Give one AI review level for every medication combination.
                    - The review level must be exactly one of: Low, Moderate, High.

                    Review level meanings:
                    - Low: no obvious interaction concern identified from the medication pair.
                    - Moderate: a possible concern may require pharmacist or doctor review.
                    - High: a potentially serious concern may require prompt professional review.

                    Output one line per medication pair using exactly this format:
                    Medication A | Medication B | Low/Moderate/High | Short reason

                    Do not include markdown tables.
                    Do not include extra headings.
                    Do not include numbering.
                """.trimIndent()

                // Sends the prompt to Gemini.
                val response = generativeModel.generateContent(
                    content {
                        text(prompt)
                    }
                )

                val rawText = response.text?.trim()

                if (rawText.isNullOrBlank()) {
                    _message.value =
                        "No interaction review was generated. Please try again."
                    return@launch
                }

                val parsedItems = parseInteractionReview(rawText)

                if (parsedItems.isEmpty()) {
                    _message.value =
                        "Could not read the interaction review format. Please try again."
                    return@launch
                }

                // Converts UI review items into Room entity objects for saving.
                val reviewsToSave = parsedItems.map { item ->
                    InteractionReview(
                        patientId = patientId,
                        firstMedication = item.firstMedication,
                        secondMedication = item.secondMedication,
                        riskLevel = item.riskLevel,
                        reason = item.reason,
                        createdAt = LocalDateTime.now().toString()
                    )
                }

                // Replaces old reviews with the newest generated review.
                interactionReviewRepository.clearReviewsForPatient(patientId)
                interactionReviewRepository.insertReviews(reviewsToSave)

                _reviewItems.value = parsedItems
                _message.value =
                    "AI review completed and saved. Please confirm any concerns with a doctor or pharmacist."
            } catch (exception: Exception) {
                _message.value =
                    "Could not generate interaction review. Please check your internet connection or API key."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Creates all possible medication pairs from the patient's medication list.
    private fun createMedicationPairs(
        medications: List<Medication>
    ): List<Pair<Medication, Medication>> {
        val pairs = mutableListOf<Pair<Medication, Medication>>()

        for (i in medications.indices) {
            for (j in i + 1 until medications.size) {
                pairs.add(Pair(medications[i], medications[j]))
            }
        }

        return pairs
    }

    // Parses Gemini's response into InteractionReviewItem objects.
    private fun parseInteractionReview(rawText: String): List<InteractionReviewItem> {
        return rawText
            .lines()
            .mapNotNull { line ->
                val parts = line
                    .split("|")
                    .map { item -> item.trim() }

                if (parts.size < 4) {
                    null
                } else {
                    InteractionReviewItem(
                        firstMedication = parts[0],
                        secondMedication = parts[1],
                        riskLevel = normaliseRiskLevel(parts[2]),
                        reason = parts.drop(3).joinToString(" | ").trim()
                    )
                }
            }
    }

    // Makes sure the risk level is always displayed as Low, Moderate, or High.
    private fun normaliseRiskLevel(value: String): String {
        return when {
            value.contains("high", ignoreCase = true) -> "High"
            value.contains("moderate", ignoreCase = true) -> "Moderate"
            else -> "Low"
        }
    }
}

class InteractionReviewViewModelFactory(
    private val medicationRepository: MedicationRepository,
    private val interactionReviewRepository: InteractionReviewRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InteractionReviewViewModel(
            medicationRepository = medicationRepository,
            interactionReviewRepository = interactionReviewRepository
        ) as T
    }
}