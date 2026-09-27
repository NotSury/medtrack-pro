package com.surya.s35651628.medtrack.data.symptom

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.surya.s35651628.medtrack.data.patient.Patient

@Entity(
    tableName = "symptoms",
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
data class Symptom(
    @PrimaryKey(autoGenerate = true)
    val symptomId: Int = 0,

    val patientId: String,
    val category: String,
    val severity: Int,
    val notes: String,
    val dateTime: String
)
