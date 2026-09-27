package com.surya.s35651628.medtrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.surya.s35651628.medtrack.data.interaction.InteractionReview
import com.surya.s35651628.medtrack.data.interaction.InteractionReviewDao
import com.surya.s35651628.medtrack.data.medcoach.MedCoachTip
import com.surya.s35651628.medtrack.data.medcoach.MedCoachTipDao
import com.surya.s35651628.medtrack.data.medication.Medication
import com.surya.s35651628.medtrack.data.medication.MedicationDao
import com.surya.s35651628.medtrack.data.patient.Patient
import com.surya.s35651628.medtrack.data.patient.PatientDao
import com.surya.s35651628.medtrack.data.symptom.Symptom
import com.surya.s35651628.medtrack.data.symptom.SymptomDao

@Database(
    entities = [
        Patient::class,
        Medication::class,
        Symptom::class,
        MedCoachTip::class,
        InteractionReview::class
    ],
    version = 4,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun medicationDao(): MedicationDao
    abstract fun symptomDao(): SymptomDao
    abstract fun medCoachTipDao(): MedCoachTipDao
    abstract fun interactionReviewDao(): InteractionReviewDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medtrack_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}