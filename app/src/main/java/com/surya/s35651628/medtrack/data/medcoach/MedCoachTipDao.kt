package com.surya.s35651628.medtrack.data.medcoach

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedCoachTipDao {

    // Saves one generated AI tip into Room.
    @Insert
    suspend fun insertTip(tip: MedCoachTip)

    // Loads all previous tips for the current logged-in patient.
    @Query("""
        SELECT * FROM medcoach_tips
        WHERE patientId = :patientId
        ORDER BY timestamp DESC
    """)
    fun observeTipsForPatient(patientId: String): Flow<List<MedCoachTip>>
}