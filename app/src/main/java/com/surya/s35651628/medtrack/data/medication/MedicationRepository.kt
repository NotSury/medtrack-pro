package com.surya.s35651628.medtrack.data.medication

import kotlinx.coroutines.flow.Flow

class MedicationRepository(
    private val medicationDao: MedicationDao
) {
    suspend fun insertMedication(medication: Medication) {
        medicationDao.insertMedication(medication)
    }

    suspend fun insertMedications(medications: List<Medication>) {
        medicationDao.insertMedications(medications)
    }

    fun observeMedicationsForPatient(patientId: String): Flow<List<Medication>> {
        return medicationDao.observeMedicationsForPatient(patientId)
    }

    suspend fun getMedicationsForPatient(patientId: String): List<Medication> {
        return medicationDao.getMedicationsForPatient(patientId)
    }

    suspend fun getMedicationCountForPatient(patientId: String): Int {
        return medicationDao.getMedicationCountForPatient(patientId)
    }

    suspend fun updateTakenStatus(
        medicationId: Int,
        isTaken: Boolean,
        takenDate: String?
    ) {
        medicationDao.updateTakenStatus(
            medicationId = medicationId,
            isTaken = isTaken,
            takenDate = takenDate
        )
    }

    suspend fun resetOldTakenStatuses(
        patientId: String,
        todayDate: String
    ) {
        medicationDao.resetOldTakenStatuses(
            patientId = patientId,
            todayDate = todayDate
        )
    }

    suspend fun getAverageMedicationsPerPatient(): Double {
        return medicationDao.getAverageMedicationsPerPatient()
    }
}