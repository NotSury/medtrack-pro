package com.surya.s35651628.medtrack.data.openfda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OpenFdaViewModel(
    private val openFdaRepository: OpenFdaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DrugInfoUiState())
    val uiState: StateFlow<DrugInfoUiState> = _uiState.asStateFlow()

    // Updates the medication name typed by the user.
    fun updateDrugName(drugName: String) {
        _uiState.value = _uiState.value.copy(
            drugName = drugName,
            errorMessage = ""
        )
    }

    // Searches OpenFDA for drug label information based on the entered drug name.
    fun searchDrug() {
        // Remove extra spaces before validating/searching.
        val drugName = _uiState.value.drugName.trim()

        // Do not search if the input is empty.
        if (drugName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter a medication name."
            )
            return
        }

        viewModelScope.launch {
            // Show loading state and clear old results before starting a new search.
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = "",
                purpose = "",
                warnings = "",
                dosage = ""
            )

            // Ask the repository to search the OpenFDA API.
            // The repository handles brand-name and generic-name search attempts.
            val result = openFdaRepository.searchDrugByName(drugName)

            result
                .onSuccess { drugLabel ->
                    // If a drug label is found, show the required fields on the screen.
                    // OpenFDA fields are lists, so the first item is displayed.
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        purpose = drugLabel.purpose.firstOrNotAvailable(),
                        warnings = drugLabel.warnings.firstOrNotAvailable(),
                        dosage = drugLabel.dosage_and_administration.firstOrNotAvailable(),
                        errorMessage = ""
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Drug information was not found or the network is unavailable.",
                        purpose = "",
                        warnings = "",
                        dosage = ""
                    )
                }
        }
    }

    // Updates the drug name when the user selects a medication from the dropdown.
    fun selectDrugFromDropdown(drugName: String) {
        _uiState.value = _uiState.value.copy(
            drugName = drugName,
            errorMessage = ""
        )
    }

    // Helper function for OpenFDA fields.
    // Some fields may be missing, so this prevents the app from crashing.
    private fun List<String>?.firstOrNotAvailable(): String {
        return this?.firstOrNull() ?: "Not available"
    }

    fun resetDrugInfoState() {
        _uiState.value = DrugInfoUiState()
    }
}

class OpenFdaViewModelFactory(private val openFdaRepository: OpenFdaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OpenFdaViewModel(
            openFdaRepository = openFdaRepository
        ) as T
    }
}