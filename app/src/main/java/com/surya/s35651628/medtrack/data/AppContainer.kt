package com.surya.s35651628.medtrack.data

import android.content.Context
import com.surya.s35651628.medtrack.data.interaction.InteractionReviewRepository
import com.surya.s35651628.medtrack.data.medcoach.MedCoachTipRepository
import com.surya.s35651628.medtrack.data.medication.MedicationRepository
import com.surya.s35651628.medtrack.data.openfda.OpenFdaApiService
import com.surya.s35651628.medtrack.data.openfda.OpenFdaRepository
import com.surya.s35651628.medtrack.data.patient.PatientRepository
import com.surya.s35651628.medtrack.data.symptom.SymptomRepository

object AppContainer {

    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var patientRepository: PatientRepository? = null

    @Volatile
    private var medicationRepository: MedicationRepository? = null

    @Volatile
    private var symptomRepository: SymptomRepository? = null

    @Volatile
    private var openFdaRepository: OpenFdaRepository? = null

    @Volatile
    private var medCoachTipRepository: MedCoachTipRepository? = null

    private fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val db = AppDatabase.getDatabase(context.applicationContext)
            database = db
            db
        }
    }

    fun getPatientRepository(context: Context): PatientRepository {
        return patientRepository ?: synchronized(this) {
            val db = getDatabase(context)

            val repository = PatientRepository(
                patientDao = db.patientDao()
            )

            patientRepository = repository
            repository
        }
    }

    fun getMedicationRepository(context: Context): MedicationRepository {
        return medicationRepository ?: synchronized(this) {
            val db = getDatabase(context)

            val repository = MedicationRepository(
                medicationDao = db.medicationDao()
            )

            medicationRepository = repository
            repository
        }
    }

    fun getSymptomRepository(context: Context): SymptomRepository {
        return symptomRepository ?: synchronized(this) {
            val db = getDatabase(context)

            val repository = SymptomRepository(
                symptomDao = db.symptomDao()
            )

            symptomRepository = repository
            repository
        }
    }

    fun getOpenFdaRepository(): OpenFdaRepository {
        return openFdaRepository ?: synchronized(this) {
            val repository = OpenFdaRepository(
                apiService = OpenFdaApiService.create()
            )

            openFdaRepository = repository
            repository
        }
    }

    fun getMedCoachTipRepository(context: Context): MedCoachTipRepository {
        return medCoachTipRepository ?: synchronized(this) {
            val db = getDatabase(context)

            val repository = MedCoachTipRepository(
                medCoachTipDao = db.medCoachTipDao()
            )

            medCoachTipRepository = repository
            repository
        }
    }

    fun getInteractionReviewRepository(context: Context): InteractionReviewRepository {
        val database = AppDatabase.getDatabase(context)
        return InteractionReviewRepository(database.interactionReviewDao())
    }
}