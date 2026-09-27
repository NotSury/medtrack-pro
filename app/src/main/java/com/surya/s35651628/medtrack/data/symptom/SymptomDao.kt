package com.surya.s35651628.medtrack.data.symptom

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Holds the average severity for each symptom category.
// This is used by the clinician dashboard.
data class SymptomSeverityAverage(
    val category: String,
    val averageSeverity: Double
)

// Holds symptom details that are safe to send to GenAI.
// It does not include patient identity information.
data class SymptomAiContext(
    val category: String,
    val severity: Int,
    val dateTime: String,
    val notes: String
)

// Holds the number of symptom reports for each category.
data class SymptomCategoryCount(
    val category: String,
    val count: Int
)

@Dao
interface SymptomDao {

    // Inserts one symptom record into the database.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: Symptom)

    // Inserts multiple symptom records into the database at once.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptoms(symptoms: List<Symptom>)

    // Observes all symptoms for one patient, newest first.
    @Query("SELECT * FROM symptoms WHERE patientId = :patientId ORDER BY dateTime DESC")
    fun observeSymptomsForPatient(patientId: String): Flow<List<Symptom>>

    // Gets all symptoms for one patient as a normal List.
    @Query("SELECT * FROM symptoms WHERE patientId = :patientId ORDER BY dateTime DESC")
    suspend fun getSymptomsForPatient(patientId: String): List<Symptom>

    // Finds the most common symptom category across all patients.
    // It groups symptoms by category, counts them, and returns the category with the highest count.
    @Query(
        """
            SELECT category
            FROM symptoms
            GROUP BY category
            ORDER BY COUNT(*) DESC
            LIMIT 1
        """
    )
    suspend fun getMostCommonSymptomCategory(): String?

    // Calculates the average symptom severity across all patients.
    @Query("SELECT AVG(severity) FROM symptoms")
    suspend fun getAverageSymptomSeverity(): Double?

    // Counts how many symptom reports exist for each category.
    // This gives extra aggregate data for the clinician dashboard and GenAI pattern finding.
    @Query(
        """
        SELECT category, COUNT(*) AS count
        FROM symptoms
        GROUP BY category
        ORDER BY count DESC
        """
    )
    suspend fun getSymptomCategoryCounts(): List<SymptomCategoryCount>

    // Finds the symptom category with the highest average severity.
    @Query(
        """
    SELECT category, AVG(severity) AS averageSeverity
    FROM symptoms
    GROUP BY category
    ORDER BY averageSeverity DESC
    LIMIT 1
    """
    )
    suspend fun getHighestAverageSeverityCategory(): SymptomSeverityAverage?

    // Gets recent symptom records for GenAI pattern finding.
    // LIMIT 15 keeps the prompt shorter and avoids sending too much data to Gemini.
    @Query(
        """
    SELECT category, severity, dateTime, notes
    FROM symptoms
    ORDER BY dateTime DESC
    LIMIT 15
    """
    )
    suspend fun getRecentSymptomsForAi(): List<SymptomAiContext>
}