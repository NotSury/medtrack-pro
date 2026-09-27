package com.surya.s35651628.medtrack.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.surya.s35651628.medtrack.data.genai.GenAIViewModel
import com.surya.s35651628.medtrack.data.medication.MedicationViewModel
import com.surya.s35651628.medtrack.data.openfda.OpenFdaViewModel

@Composable
fun MedCoachScreen(
    patientId: String,
    medicationViewModel: MedicationViewModel,
    openFdaViewModel: OpenFdaViewModel,
    genAIViewModel: GenAIViewModel
) {
    val medications by medicationViewModel.medications.collectAsState()
    val drugInfoState by openFdaViewModel.uiState.collectAsState()
    val genAIState by genAIViewModel.uiState.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }
    var showTipsDialog by remember { mutableStateOf(false) }

    // Load medications for dropdown and GenAI tip history for this user.
    LaunchedEffect(patientId) {
        openFdaViewModel.resetDrugInfoState()
        medicationViewModel.loadMedications(patientId)
        genAIViewModel.loadTipHistory(patientId)
    }

    val medicationNames = medications
        .map { it.medicationName }
        .distinct()
        .sorted()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 36.dp,
                bottom = 20.dp
            ),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "MedCoach",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Drug Information",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Search for a medication using OpenFDA drug label data.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = drugInfoState.drugName,
                onValueChange = openFdaViewModel::updateDrugName,
                label = { Text("Medication name, e.g. ibuprofen") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (medicationNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose from my medications")
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        medicationNames.forEach { medicationName ->
                            DropdownMenuItem(
                                text = { Text(medicationName) },
                                onClick = {
                                    openFdaViewModel.selectDrugFromDropdown(medicationName)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    openFdaViewModel.searchDrug()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search Drug Info")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (drugInfoState.isLoading) {
                CircularProgressIndicator()
            }

            if (drugInfoState.errorMessage.isNotBlank()) {
                Text(
                    text = drugInfoState.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (
                drugInfoState.purpose.isNotBlank() ||
                drugInfoState.warnings.isNotBlank() ||
                drugInfoState.dosage.isNotBlank()
            ) {
                DrugInfoCard(
                    purpose = drugInfoState.purpose,
                    warnings = drugInfoState.warnings,
                    dosage = drugInfoState.dosage
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "GenAI Medication Tips",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Generate a personalised medication tip based on your medication list and recent symptoms.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    genAIViewModel.generateTip(patientId)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Tip")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    showTipsDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show All Tips")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (genAIState.isLoading) {
                CircularProgressIndicator()
            }

            if (genAIState.errorMessage.isNotBlank()) {
                Text(
                    text = genAIState.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (genAIState.generatedTip.isNotBlank()) {
                GeneratedTipCard(
                    tipText = genAIState.generatedTip
                )
            }
        }
    }

    if (showTipsDialog) {
        AlertDialog(
            onDismissRequest = {
                showTipsDialog = false
            },
            title = {
                Text("Previous MedCoach Tips")
            },
            text = {
                if (genAIState.tipHistory.isEmpty()) {
                    Text("No tips generated yet.")
                } else {
                    LazyColumn(
                        modifier = Modifier.height(360.dp)
                    ) {
                        items(genAIState.tipHistory.size) { index ->
                            val tip = genAIState.tipHistory[index]

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = tip.timestamp,
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = tip.tipText,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTipsDialog = false
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun DrugInfoCard(
    purpose: String,
    warnings: String,
    dosage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Purpose",
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = purpose)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Warnings",
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = warnings)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Dosage and Administration",
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = dosage)
        }
    }
}

@Composable
fun GeneratedTipCard(
    tipText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Generated Tip",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tipText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}