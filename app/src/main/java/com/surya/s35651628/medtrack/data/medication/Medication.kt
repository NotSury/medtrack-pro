package com.surya.s35651628.medtrack.data.medication

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.surya.s35651628.medtrack.data.patient.Patient

@Entity(
    tableName = "medications",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["patientId"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patientId"])]
)
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val medicationId: Int = 0,

    val patientId: String,
    val medicationName: String,
    val dosage: String,
    val frequency: String,
    val scheduledTime: String,
    val medicationType: String,
    val notes: String,

    // Taken toggle persistence
    val isTaken: Boolean = false,
    val takenDate: String? = null
)
