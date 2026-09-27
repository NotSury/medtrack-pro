package com.surya.s35651628.medtrack.data.openfda

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFdaApiService {

    @GET("drug/label.json")
    suspend fun searchDrugLabel(
        @Query("search") searchQuery: String,
        @Query("limit") limit: Int = 1
    ): Response<OpenFdaResponse>

    companion object {
        private const val BASE_URL = "https://api.fda.gov/"

        // Creates the Retrofit API service.
        fun create(): OpenFdaApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(OpenFdaApiService::class.java)
        }
    }
}