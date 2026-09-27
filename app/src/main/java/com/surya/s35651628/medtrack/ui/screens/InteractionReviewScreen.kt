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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.surya.s35651628.medtrack.data.medication.Medication
import com.surya.s35651628.medtrack.data.interaction.InteractionReviewViewModel
import com.surya.s35651628.medtrack.data.interaction.InteractionReviewItem

@Composable
fun InteractionReviewScreen(
    patientId: String,
    interactionReviewViewModel: InteractionReviewViewModel,
    onBackClick: () -> Unit
) {
    val medications by interactionReviewViewModel.medications.collectAsState()
    val reviewItems by interactionReviewViewModel.reviewItems.collectAsState()
    val isLoading by interactionReviewViewModel.isLoading.collectAsState()
    val message by interactionReviewViewModel.message.collectAsState()

    LaunchedEffect(patientId) {
        interactionReviewViewModel.loadInteractionReviewData(patientId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "AI Drug Interaction Review",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This AI review may be incomplete or inaccurate and is not medical advice. Always check with a doctor or pharmacist before changing medication.",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        InteractionReviewActionCard(
            medications = medications,
            isLoading = isLoading,
            message = message,
            onReviewClick = {
                interactionReviewViewModel.reviewMedicationInteractions(patientId)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (reviewItems.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = reviewItems,
                    key = { item ->
                        "${item.firstMedication}-${item.secondMedication}"
                    }
                ) { item ->
                    InteractionReviewResultCard(item = item)
                }
            }
        } else {
            Text(
                text = "No interaction review results yet.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }
    }
}

@Composable
fun InteractionReviewActionCard(
    medications: List<Medication>,
    isLoading: Boolean,
    message: String,
    onReviewClick: () -> Unit
) {
    val pairCount = if (medications.size < 2) {
        0
    } else {
        medications.size * (medications.size - 1) / 2
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Medication combinations to review: $pairCount",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onReviewClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && medications.size >= 2
            ) {
                Text(
                    text = if (isLoading) {
                        "Checking Interactions..."
                    } else {
                        "Check AI Review Level"
                    }
                )
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun InteractionReviewResultCard(
    item: InteractionReviewItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "${item.firstMedication} + ${item.secondMedication}",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "AI Review Level: ${item.riskLevel}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Possible concern: ${item.reason}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}