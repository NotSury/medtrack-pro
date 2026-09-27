package com.surya.s35651628.medtrack.data.patient

import kotlinx.coroutines.flow.Flow

class PatientRepository(
    private val patientDao: PatientDao
) {
    suspend fun insertPatient(patient: Patient) {
        patientDao.insertPatient(patient)
    }

    suspend fun insertPatients(patients: List<Patient>) {
        patientDao.insertPatients(patients)
    }

    suspend fun getPatientById(patientId: String): Patient? {
        return patientDao.getPatientById(patientId)
    }

    fun observePatientById(patientId: String): Flow<Patient?> {
        return patientDao.observePatientById(patientId)
    }

    suspend fun getPatientForClaiming(
        patientId: String,
        phoneNumber: String
    ): Patient? {
        return patientDao.getPatientForClaiming(patientId, phoneNumber)
    }

    suspend fun login(
        patientId: String,
        password: String
    ): Patient? {
        return patientDao.login(patientId, password)
    }

    suspend fun updatePassword(
        patientId: String,
        password: String
    ) {
        patientDao.updatePassword(patientId, password)
    }

    suspend fun getPatientCount(): Int {
        return patientDao.getPatientCount()
    }

    // Used by Sign Up to find the latest PatientID.
    suspend fun getLastPatientId(): String? {
        return patientDao.getLastPatientId()
    }
}