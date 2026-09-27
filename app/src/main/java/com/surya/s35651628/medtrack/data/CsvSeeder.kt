package com.surya.s35651628.medtrack.data

import android.content.Context
import com.surya.s35651628.medtrack.data.medication.Medication
import com.surya.s35651628.medtrack.data.patient.Patient
import com.surya.s35651628.medtrack.data.symptom.Symptom

// Helper class used to read the CSV files from the assets folder.
// This is only used during first-launch database seeding.
class CsvSeeder(private val context: Context) {

    // Reads patients.csv and converts each row into a Patient object.
    fun loadPatients(): List<Patient> {
        val rows = readCsv("patients.csv")
        val patients = mutableListOf<Patient>()

        // Start from index 1 because index 0 is the CSV header row.
        for (i in 1 until rows.size) {
            val columns = parseCsvLine(rows[i])

            if (columns.size >= 3) {
                patients.add(
                    Patient(
                        patientId = columns[0],
                        phoneNumber = columns[1],
                        name = columns[2],

                        // Seeded CSV users do not have passwords yet.
                        // They will set a password later through account claiming.
                        password = null
                    )
                )
            }
        }

        return patients
    }

    // Reads medications.csv and converts each row into a Medication object.
    fun loadMedications(): List<Medication> {
        val rows = readCsv("medications.csv")
        val medications = mutableListOf<Medication>()

        for (i in 1 until rows.size) {
            val columns = parseCsvLine(rows[i])

            if (columns.size >= 7) {
                medications.add(
                    Medication(
                        patientId = columns[0],
                        medicationName = columns[1],
                        dosage = columns[2],
                        frequency = columns[3],
                        scheduledTime = columns[4],
                        medicationType = columns[5],
                        notes = columns[6]
                    )
                )
            }
        }

        return medications
    }

    // Reads symptoms.csv and converts each row into a Symptom object.
    fun loadSymptoms(): List<Symptom> {
        val rows = readCsv("symptoms.csv")
        val symptoms = mutableListOf<Symptom>()

        for (i in 1 until rows.size) {
            val columns = parseCsvLine(rows[i])

            if (columns.size >= 5) {
                symptoms.add(
                    Symptom(
                        patientId = columns[0],
                        category = columns[1],
                        severity = columns[2].toIntOrNull() ?: 0,
                        notes = columns[3],
                        dateTime = columns[4]
                    )
                )
            }
        }

        return symptoms
    }

    private fun readCsv(fileName: String): List<String> {
        return context.assets.open(fileName)
            .bufferedReader()
            .useLines { lines ->
                lines.filter { it.isNotBlank() }.toList()
            }
    }

    private fun parseCsvLine(line: String): List<String> {
        return line.split(",").map { it.trim() }
    }
}