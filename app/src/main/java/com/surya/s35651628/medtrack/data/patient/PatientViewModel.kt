package com.surya.s35651628.medtrack.data.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.surya.s35651628.medtrack.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PatientViewModel(
    private val patientRepository: PatientRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _currentPatient = MutableStateFlow<Patient?>(null)
    val currentPatient: StateFlow<Patient?> = _currentPatient.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _loggedInPatientId = MutableStateFlow(sessionManager.getLoggedInPatientId())
    val loggedInPatientId: StateFlow<String?> = _loggedInPatientId.asStateFlow()

    private val _newPatientId = MutableStateFlow<String?>(null)

    val newPatientId: StateFlow<String?> = _newPatientId.asStateFlow()

    // Loads one patient's details from Room using their PatientID.
    fun loadPatient(patientId: String) {
        viewModelScope.launch {
            _currentPatient.value = patientRepository.getPatientById(patientId)
        }
    }

    // Handles first-time account claiming for seeded CSV patients.
    fun claimAccount(
        patientId: String,
        phoneNumber: String,
        newPassword: String,
        confirmPassword: String
    ) {
        // Check that all required fields are filled in.
        if (
            patientId.isBlank() ||
            phoneNumber.isBlank() ||
            newPassword.isBlank() ||
            confirmPassword.isBlank()
        ) {
            _message.value = "Please fill in all fields"
            return
        }

        if (newPassword != confirmPassword) {
            // Make sure the password confirmation matches.
            _message.value = "Passwords do not match"
            return
        }

        viewModelScope.launch {
            // Look for a patient with the matching PatientID and phone number.
            val patient = patientRepository.getPatientForClaiming(
                patientId = patientId.trim(),
                phoneNumber = phoneNumber.trim()
            )

            // If no matching patient is found, the account cannot be claimed.
            if (patient == null) {
                _message.value = "Patient ID and phone number do not match"
                return@launch
            }

            // If the password is already set, the seeded account has already been claimed.
            if (patient.password != null) {
                _message.value = "This account has already been claimed"
                return@launch
            }

            // Save the new password into the Patient table.
            patientRepository.updatePassword(
                patientId = patient.patientId,
                password = newPassword
            )

            _message.value = "Account claimed successfully. You can now log in."
        }
    }

    // Handles normal login after the account has already been claimed.
    fun login(
        patientId: String,
        password: String
    ) {
        // Check that the user entered both PatientID and password.
        if (patientId.isBlank() || password.isBlank()) {
            _message.value = "Please enter Patient ID and password"
            return
        }

        viewModelScope.launch {
            // Validate login details against the Room database.
            val patient = patientRepository.login(
                patientId = patientId.trim(),
                password = password
            )

            if (patient != null) {
                // Save the PatientID in SharedPreferences so the session survives app restart.
                sessionManager.saveLoggedInPatientId(patient.patientId)

                // Update ViewModel state after successful login.
                _loggedInPatientId.value = patient.patientId
                _currentPatient.value = patient
                _message.value = "Login successful"
            } else {
                _message.value = "Invalid Patient ID or password"
            }
        }
    }

    // Clear login session.
    fun logout() {
        sessionManager.clearSession()

        // Clear the current logged-in state from the ViewModel.
        _loggedInPatientId.value = null
        _currentPatient.value = null
        _message.value = "Logged out"
    }

    fun clearMessage() {
        _message.value = ""
    }

    // Creates a new patient account and saves it into Room.
    // This is used for new users who are not from the seeded CSV data.
    fun signUp(
        name: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ) {
        if (
            // Check that all required Sign Up fields are filled in.
            name.isBlank() ||
            phoneNumber.isBlank() ||
            password.isBlank() ||
            confirmPassword.isBlank()
        ) {
            _message.value = "Please fill in all fields"
            return
        }

        if (password != confirmPassword) {
            _message.value = "Passwords do not match"
            return
        }

        viewModelScope.launch {
            // Get the latest PatientID from Room so the next ID can be generated.
            val lastPatientId = patientRepository.getLastPatientId()

            // Extract the number part from the latest ID and add 1.
            val nextNumber = if (lastPatientId != null && lastPatientId.length > 1) {
                val numberPart = lastPatientId.substring(1).toIntOrNull() ?: 1000
                numberPart + 1
            } else {
                1001
            }

            val generatedPatientId = "P$nextNumber"

            // Create the new patient object using the generated PatientID.
            val newPatient = Patient(
                patientId = generatedPatientId,
                phoneNumber = phoneNumber.trim(),
                name = name.trim(),
                password = password
            )

            patientRepository.insertPatient(newPatient)

            _newPatientId.value = generatedPatientId
            _message.value = "Account created. Your Patient ID is $generatedPatientId"
        }
    }
}

class PatientViewModelFactory(
    private val patientRepository: PatientRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PatientViewModel(
            patientRepository = patientRepository,
            sessionManager = sessionManager
        ) as T
    }
}