package com.surya.s35651628.medtrack.data.medication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    // Inserts one medication into the database.
    // If a medication with the same primary key already exists, it will be replaced.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication)

    // Inserts multiple medications into the database at once.
    // This is useful during CSV seeding when many medications are loaded on first launch.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<Medication>)

    // Observes all medications for one patient, ordered by scheduled time.
    // Flow is used so the Home screen can update automatically when medication data changes.
    @Query("SELECT * FROM medications WHERE patientId = :patientId ORDER BY scheduledTime")
    fun observeMedicationsForPatient(patientId: String): Flow<List<Medication>>

    // Gets all medications for one patient as a normal List.
    // This is useful when the data is needed once, such as for GenAI prompts or interaction reviews.
    @Query("SELECT * FROM medications WHERE patientId = :patientId ORDER BY scheduledTime")
    suspend fun getMedicationsForPatient(patientId: String): List<Medication>

    // Counts how many medications a specific patient has.
    // This can be used for summaries or validation.
    @Query("SELECT COUNT(*) FROM medications WHERE patientId = :patientId")
    suspend fun getMedicationCountForPatient(patientId: String): Int

    // Updates the taken status for one medication.
    // takenDate stores the date when the medication was marked as taken.
    @Query(
        """
            UPDATE medications
            SET isTaken = :isTaken,
                takenDate = :takenDate
            WHERE medicationId = :medicationId
        """
    )
    suspend fun updateTakenStatus(
        medicationId: Int,
        isTaken: Boolean,
        takenDate: String?
    )

    // Resets old taken toggles for one patient.
    // If the stored takenDate is not today's date, the medication is marked as not taken again.
    @Query(
        """
            UPDATE medications
            SET isTaken = 0,
                takenDate = NULL
            WHERE patientId = :patientId
                AND takenDate IS NOT NULL
                AND takenDate != :todayDate
        """
    )
    suspend fun resetOldTakenStatuses(
        patientId: String,
        todayDate: String
    )

    // Calculates the average number of medications per patient.
    // LEFT JOIN is used so patients with zero medications are still included in the average.
    @Query(
        """
            SELECT 
                CASE 
                    WHEN COUNT(DISTINCT p.patientId) = 0 THEN 0.0
                    ELSE CAST(COUNT(m.medicationId) AS REAL) / COUNT(DISTINCT p.patientId)
                END
            FROM patients p
            LEFT JOIN medications m
                ON p.patientId = m.patientId
        """
    ) suspend fun getAverageMedicationsPerPatient(): Double
}