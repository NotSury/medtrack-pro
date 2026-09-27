package com.surya.s35651628.medtrack.data.symptom

import kotlinx.coroutines.flow.Flow

class SymptomRepository(
    private val symptomDao: SymptomDao
) {
    suspend fun insertSymptom(symptom: Symptom) {
        symptomDao.insertSymptom(symptom)
    }

    suspend fun insertSymptoms(symptoms: List<Symptom>) {
        symptomDao.insertSymptoms(symptoms)
    }

    fun observeSymptomsForPatient(patientId: String): Flow<List<Symptom>> {
        return symptomDao.observeSymptomsForPatient(patientId)
    }

    suspend fun getSymptomsForPatient(patientId: String): List<Symptom> {
        return symptomDao.getSymptomsForPatient(patientId)
    }

    suspend fun getMostCommonSymptomCategory(): String? {
        return symptomDao.getMostCommonSymptomCategory()
    }

    suspend fun getAverageSymptomSeverity(): Double? {
        return symptomDao.getAverageSymptomSeverity()
    }

    suspend fun getSymptomCategoryCounts(): List<SymptomCategoryCount> {
        return symptomDao.getSymptomCategoryCounts()
    }

    suspend fun getHighestAverageSeverityCategory(): SymptomSeverityAverage? {
        return symptomDao.getHighestAverageSeverityCategory()
    }

    suspend fun getRecentSymptomsForAi(): List<SymptomAiContext> {
        return symptomDao.getRecentSymptomsForAi()
    }
}