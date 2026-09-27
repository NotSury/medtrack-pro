package com.surya.s35651628.medtrack.data.patient

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {

    // Inserts one patient into the database.
    // This is mainly used when a new user signs up.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient)

    // Inserts multiple patients into the database at once.
    // This is used during first-launch CSV seeding.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatients(patients: List<Patient>)

    // Gets one patient using their PatientID.
    // Returns null if no patient with that ID exists.
    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    suspend fun getPatientById(patientId: String): Patient?

    // Observes one patient using their PatientID.
    // Flow is used so screens like Home or Settings can update automatically
    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    fun observePatientById(patientId: String): Flow<Patient?>

    // Finds a seeded patient during first-time account claiming.
    @Query("""
        SELECT * FROM patients 
        WHERE patientId = :patientId 
        AND phoneNumber = :phoneNumber
    """)
    suspend fun getPatientForClaiming(patientId: String, phoneNumber: String): Patient?

    // Used for normal login after the account has already been claimed.
    // Login is successful only if the PatientID and password match a row in Room.
    @Query("""
        SELECT * FROM patients 
        WHERE patientId = :patientId 
        AND password = :password
    """)
    suspend fun login(patientId: String, password: String): Patient?

    // Saves the new password during account claiming.
    @Query("UPDATE patients SET password = :password WHERE patientId = :patientId")
    suspend fun updatePassword(patientId: String, password: String)

    // Counts all patients in the database.
    // This is used for the clinician dashboard aggregate statistics.
    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int

    // Gets the latest PatientID so the app can generate the next ID during Sign Up.
    @Query("""
    SELECT patientId FROM patients
    ORDER BY CAST(SUBSTR(patientId, 2) AS INTEGER) DESC
    LIMIT 1
    """)
    suspend fun getLastPatientId(): String?
}