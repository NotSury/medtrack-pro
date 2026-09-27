package com.surya.s35651628.medtrack.data.openfda

class OpenFdaRepository(
    private val apiService: OpenFdaApiService
) {
    suspend fun searchDrugByName(drugName: String): Result<DrugLabelResult> {
        return try {
            // Remove extra spaces from the user input before searching.
            val cleanDrugName = drugName.trim()

            // Try searching by brand name first because users may enter names like "Advil" or "Panadol".
            val brandQuery = """openfda.brand_name:"$cleanDrugName""""

            val brandResponse = apiService.searchDrugLabel(
                searchQuery = brandQuery,
                limit = 1
            )

            if (brandResponse.isSuccessful) {
                val result = brandResponse.body()?.results?.firstOrNull()

                if (result != null) {
                    return Result.success(result)
                }
            }

            // If no brand result is found, try generic name because users may enter names like "ibuprofen" or "metformin".
            val genericQuery = """openfda.generic_name:"$cleanDrugName""""

            val genericResponse = apiService.searchDrugLabel(
                searchQuery = genericQuery,
                limit = 1
            )

            if (genericResponse.isSuccessful) {
                val result = genericResponse.body()?.results?.firstOrNull()

                if (result != null) {
                    Result.success(result)
                } else {
                    Result.failure(Exception("No drug information found."))
                }
            } else {
                Result.failure(Exception("No drug information found."))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}