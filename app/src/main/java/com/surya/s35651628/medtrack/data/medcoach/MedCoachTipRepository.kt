package com.surya.s35651628.medtrack.data.medcoach

import kotlinx.coroutines.flow.Flow

class MedCoachTipRepository(
    private val medCoachTipDao: MedCoachTipDao
) {
    fun observeTipsForPatient(patientId: String): Flow<List<MedCoachTip>> {
        return medCoachTipDao.observeTipsForPatient(patientId)
    }

    suspend fun insertTip(tip: MedCoachTip) {
        medCoachTipDao.insertTip(tip)
    }
}