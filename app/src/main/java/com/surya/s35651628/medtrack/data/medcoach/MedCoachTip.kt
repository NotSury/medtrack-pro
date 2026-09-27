package com.surya.s35651628.medtrack.data.medcoach

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.surya.s35651628.medtrack.data.patient.Patient

@Entity(
    tableName = "medcoach_tips",
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
data class MedCoachTip(
    @PrimaryKey(autoGenerate = true)
    val tipId: Int = 0,

    val patientId: String,
    val tipText: String,
    val timestamp: String
)