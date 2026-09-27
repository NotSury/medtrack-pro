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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.surya.s35651628.medtrack.data.medication.Medication
import com.surya.s35651628.medtrack.data.medication.MedicationViewModel
import com.surya.s35651628.medtrack.data.patient.PatientViewModel


@Composable
fun HomeScreen(
    patientId: String,
    patientViewModel: PatientViewModel,
    medicationViewModel: MedicationViewModel,
    onAddMedicationClick: () -> Unit,
    onInteractionReviewClick: () -> Unit
) {
    val currentPatient by patientViewModel.currentPatient.collectAsState()
    val medications by medicationViewModel.medications.collectAsState()

    // Load the logged-in patient's details and medication list from Room.
    LaunchedEffect(patientId) {
        patientViewModel.loadPatient(patientId)
        medicationViewModel.loadMedications(patientId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Home",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Hello, ${currentPatient?.name ?: "Patient"}",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Patient ID: $patientId",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddMedicationClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Medication")
        }

        Spacer(modifier = Modifier.height(20.dp))

        AiInteractionSummaryCard(
            medications = medications,
            onOpenClick = onInteractionReviewClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Your Medications",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (medications.isEmpty()) {
            Text(
                text = "No medications found.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = medications,
                    key = { medication -> medication.medicationId }
                ) { medication ->
                    MedicationCard(
                        medication = medication,
                        onTakenChange = { isTaken ->
                            medicationViewModel.updateTakenStatus(
                                medicationId = medication.medicationId,
                                isTaken = isTaken
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MedicationCard(
    medication: Medication,
    onTakenChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = medication.medicationName,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("Dosage: ${medication.dosage}")
            Text("Frequency: ${medication.frequency}")
            Text("Scheduled Time: ${medication.scheduledTime}")
            Text("Type: ${medication.medicationType}")

            if (medication.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Notes: ${medication.notes}")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Taken today",
                        style = MaterialTheme.typography.titleSmall
                    )

                    if (medication.isTaken && medication.takenDate != null) {
                        Text(
                            text = "Marked on ${medication.takenDate}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Switch(
                    checked = medication.isTaken,
                    onCheckedChange = onTakenChange
                )
            }
        }
    }
}

@Composable
fun AiInteractionSummaryCard(
    medications: List<Medication>,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pairCount = if (medications.size < 2) {
        0
    } else {
        medications.size * (medications.size - 1) / 2
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "AI Drug Interaction Review",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Review possible interaction levels between your current medications.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Medication combinations: $pairCount",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpenClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = medications.size >= 2
            ) {
                Text("Open Interaction Review")
            }
        }
    }
}



