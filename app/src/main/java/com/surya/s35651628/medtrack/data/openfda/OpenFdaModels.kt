package com.surya.s35651628.medtrack.data.openfda

data class OpenFdaResponse(
    val results: List<DrugLabelResult>? = null
)

data class DrugLabelResult(
    val purpose: List<String>? = null,
    val warnings: List<String>? = null,
    val dosage_and_administration: List<String>? = null
)

data class DrugInfoUiState(
    val drugName: String = "",
    val purpose: String = "",
    val warnings: String = "",
    val dosage: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)