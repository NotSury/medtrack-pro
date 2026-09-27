package com.surya.s35651628.medtrack.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.surya.s35651628.medtrack.data.patient.PatientViewModel

@Composable
fun SettingsScreen(
    patientId: String,
    patientViewModel: PatientViewModel,
    onLogoutClick: () -> Unit,
    onClinicianLoginClick: () -> Unit
) {
    val currentPatient by patientViewModel.currentPatient.collectAsState()

    // Load current patient info from Room for the Settings screen.
    LaunchedEffect(patientId) {
        patientViewModel.loadPatient(patientId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Account Information",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Name: ${currentPatient?.name ?: "Loading..."}")
                Text("Phone Number: ${currentPatient?.phoneNumber ?: "Loading..."}")
                Text("Patient ID: $patientId")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onClinicianLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clinician Login")
        }
    }
}