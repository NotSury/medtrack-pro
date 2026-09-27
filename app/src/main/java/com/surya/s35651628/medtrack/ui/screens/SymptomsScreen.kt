package com.surya.s35651628.medtrack.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.surya.s35651628.medtrack.data.symptom.Symptom
import com.surya.s35651628.medtrack.data.symptom.SymptomViewModel

@Composable
fun SymptomsScreen(
    patientId: String,
    symptomViewModel: SymptomViewModel
) {
    val symptoms by symptomViewModel.symptoms.collectAsState()
    val message by symptomViewModel.message.collectAsState()

    var category by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var severity by remember { mutableFloatStateOf(5f) }

    // Load symptom history for the logged-in patient from Room.
    LaunchedEffect(patientId) {
        symptomViewModel.clearMessage()
        symptomViewModel.loadSymptoms(patientId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Symptoms",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Log New Symptom",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = category,
            onValueChange = {
                category = it
                symptomViewModel.clearMessage()
            },
            label = { Text("Category, e.g. Headache") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Severity: ${severity.toInt()} / 10")

        Slider(
            value = severity,
            onValueChange = {
                severity = it
                symptomViewModel.clearMessage()
            },
            valueRange = 1f..10f,
            steps = 8
        )

        OutlinedTextField(
            value = notes,
            onValueChange = {
                notes = it
                symptomViewModel.clearMessage()
            },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                // Save the symptom into Room with the logged-in patient's ID.
                symptomViewModel.addSymptom(
                    patientId = patientId,
                    category = category,
                    severity = severity.toInt(),
                    notes = notes
                )

                category = ""
                notes = ""
                severity = 5f
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Symptom")
        }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Symptom History",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (symptoms.isEmpty()) {
            Text("No symptoms recorded.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(symptoms) { symptom ->
                    SymptomCard(symptom = symptom)
                }
            }
        }
    }
}

@Composable
fun SymptomCard(
    symptom: Symptom
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = symptom.category,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "${symptom.severity}/10",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text("Date: ${symptom.dateTime}")

            if (symptom.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Notes: ${symptom.notes}")
            }
        }
    }
}