package com.surya.s35651628.medtrack.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.surya.s35651628.medtrack.data.clinician.ClinicianViewModel
import java.util.Locale

@Composable
fun ClinicianDashboardScreen(
    clinicianViewModel: ClinicianViewModel,
    onBackClick: () -> Unit
) {
    val uiState by clinicianViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        clinicianViewModel.loadDashboardStats()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Clinician Dashboard",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Aggregate patient statistics and GenAI-powered insights.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Aggregate Statistics",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoadingStats) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            StatisticCard(
                title = "Total Patients",
                value = uiState.stats.totalPatients.toString()
            )

            StatisticCard(
                title = "Average Medications per Patient",
                value = formatDouble(uiState.stats.averageMedicationsPerPatient)
            )

            StatisticCard(
                title = "Most Common Symptom Category",
                value = uiState.stats.mostCommonSymptomCategory
            )

            StatisticCard(
                title = "Average Symptom Severity",
                value = "${formatDouble(uiState.stats.averageSymptomSeverity)} / 10"
            )

            StatisticCard(
                title = "Highest Average Severity Category",
                value = uiState.stats.highestAverageSeverityCategory
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "GenAI-Powered Insights",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                clinicianViewModel.findPatterns()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isGeneratingInsights
        ) {
            Text(
                text = if (uiState.isGeneratingInsights) {
                    "Finding Patterns..."
                } else {
                    "Find Patterns"
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isGeneratingInsights) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }

        if (uiState.errorMessage.isNotBlank()) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (uiState.insights.isNotEmpty()) {
            uiState.insights.forEachIndexed { index, insight ->
                InsightCard(
                    number = index + 1,
                    insight = insight
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Settings")
        }
    }
}

@Composable
fun StatisticCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun InsightCard(
    number: Int,
    insight: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$number.",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.padding(horizontal = 4.dp))

            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatDouble(value: Double): String {
    return String.format(Locale.getDefault(), "%.2f", value)
}