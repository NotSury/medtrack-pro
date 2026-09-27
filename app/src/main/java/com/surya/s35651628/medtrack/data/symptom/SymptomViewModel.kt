package com.surya.s35651628.medtrack.data.symptom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SymptomViewModel(
    private val symptomRepository: SymptomRepository
) : ViewModel() {

    private val _symptoms = MutableStateFlow<List<Symptom>>(emptyList())
    val symptoms: StateFlow<List<Symptom>> = _symptoms.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    // Loads symptoms for the logged-in patient from Room.
    fun loadSymptoms(patientId: String) {
        viewModelScope.launch {
            // Observe the patient's symptoms through the repository.
            symptomRepository
                .observeSymptomsForPatient(patientId)
                .collect { symptomList ->
                    _symptoms.value = symptomList
                }
        }
    }

    // Saves a new symptom record for the logged-in patient.
    fun addSymptom(
        patientId: String,
        category: String,
        severity: Int,
        notes: String
    ) {
        if (category.isBlank()) {
            _message.value = "Please select a symptom category"
            return
        }

        val currentDateTime = SimpleDateFormat(
            "yyyy-MM-dd HH:mm",
            Locale.getDefault()
        ).format(Date())

        viewModelScope.launch {
            val symptom = Symptom(
                patientId = patientId,
                category = category,
                severity = severity,
                notes = notes,
                dateTime = currentDateTime
            )

            symptomRepository.insertSymptom(symptom)

            _message.value = "Symptom saved successfully"
        }
    }

    fun clearMessage() {
        _message.value = ""
    }
}

class SymptomViewModelFactory(
    private val symptomRepository: SymptomRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SymptomViewModel(
            symptomRepository = symptomRepository
        ) as T
    }
}