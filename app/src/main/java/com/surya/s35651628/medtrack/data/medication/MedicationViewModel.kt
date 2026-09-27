package com.surya.s35651628.medtrack.data.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class MedicationViewModel(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    // Private mutable medication list used inside the ViewModel.
    private val _medications = MutableStateFlow<List<Medication>>(emptyList())

    // Public read-only medication list observed by the Home screen.
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    // Private message state used for success or validation messages.
    private val _message = MutableStateFlow("")

    // Public read-only message state observed by the UI.
    val message: StateFlow<String> = _message.asStateFlow()

    // Loads medications for the logged-in patient.
    // It also resets old taken statuses before showing the current medication list.
    fun loadMedications(patientId: String) {

        viewModelScope.launch {
            // Get today's date so the app can check whether taken statuses are from today.
            val todayDate = LocalDate.now().toString()

            // Reset medications that were marked as taken on a previous day.
            medicationRepository.resetOldTakenStatuses(
                patientId = patientId,
                todayDate = todayDate
            )

            // Observe the patient's medications from Room through the repository.
            // Whenever the database changes, the Home screen updates automatically.
            medicationRepository
                .observeMedicationsForPatient(patientId)
                .collect { medicationList ->
                    _medications.value = medicationList
                }
        }
    }

    // Adds a new medication for the logged-in patient.
    fun addMedication(
        patientId: String,
        medicationName: String,
        dosage: String,
        frequency: String,
        scheduledTime: String,
        medicationType: String,
        notes: String
    ) {
        // Check that all required fields have been filled in before saving.
        if (
            medicationName.isBlank() ||
            dosage.isBlank() ||
            frequency.isBlank() ||
            scheduledTime.isBlank() ||
            medicationType.isBlank()
        ) {
            _message.value = "Please fill in all required fields"
            return
        }

        viewModelScope.launch {
            // Create a Medication object using the values entered by the user.
            // New medications start as not taken.
            val medication = Medication(
                patientId = patientId,
                medicationName = medicationName,
                dosage = dosage,
                frequency = frequency,
                scheduledTime = scheduledTime,
                medicationType = medicationType,
                notes = notes,
                isTaken = false,
                takenDate = null
            )

            // Saves the new medication into Room through the repository.
            medicationRepository.insertMedication(medication)

            // Shows a success message after saving.
            _message.value = "Medication added successfully"
        }
    }

    // Updates whether a medication has been taken today.
    fun updateTakenStatus(
        medicationId: Int,
        isTaken: Boolean
    ) {
        viewModelScope.launch {
            // If the medication is marked as taken, store today's date.
            // If it is unticked, clear the taken date.
            val todayDate = if (isTaken) {
                LocalDate.now().toString()
            } else {
                null
            }

            medicationRepository.updateTakenStatus(
                medicationId = medicationId,
                isTaken = isTaken,
                takenDate = todayDate
            )
        }
    }
}

class MedicationViewModelFactory(
    private val medicationRepository: MedicationRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MedicationViewModel(
            medicationRepository = medicationRepository
        ) as T
    }
}