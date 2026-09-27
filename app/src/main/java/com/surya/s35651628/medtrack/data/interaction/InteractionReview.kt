package com.surya.s35651628.medtrack.data.interaction

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.surya.s35651628.medtrack.data.patient.Patient

// This table stores AI-generated drug interaction reviews for each patient.
@Entity(
    tableName = "interaction_reviews",
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
data class InteractionReview(
    @PrimaryKey(autoGenerate = true)
    val reviewId: Int = 0,
    val patientId: String,
    val firstMedication: String,
    val secondMedication: String,
    val riskLevel: String,
    val reason: String,
    val createdAt: String
)