package com.surya.s35651628.medtrack

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.surya.s35651628.medtrack.data.AppContainer
import com.surya.s35651628.medtrack.data.CsvSeeder
import com.surya.s35651628.medtrack.data.clinician.ClinicianViewModel
import com.surya.s35651628.medtrack.data.clinician.ClinicianViewModelFactory
import com.surya.s35651628.medtrack.data.genai.GenAIViewModel
import com.surya.s35651628.medtrack.data.genai.GenAIViewModelFactory
import com.surya.s35651628.medtrack.data.interaction.InteractionReviewViewModel
import com.surya.s35651628.medtrack.data.interaction.InteractionReviewViewModelFactory
import com.surya.s35651628.medtrack.data.medication.MedicationViewModel
import com.surya.s35651628.medtrack.data.medication.MedicationViewModelFactory
import com.surya.s35651628.medtrack.data.openfda.OpenFdaViewModel
import com.surya.s35651628.medtrack.data.openfda.OpenFdaViewModelFactory
import com.surya.s35651628.medtrack.data.patient.PatientViewModel
import com.surya.s35651628.medtrack.data.patient.PatientViewModelFactory
import com.surya.s35651628.medtrack.data.symptom.SymptomViewModel
import com.surya.s35651628.medtrack.data.symptom.SymptomViewModelFactory
import com.surya.s35651628.medtrack.session.SessionManager
import com.surya.s35651628.medtrack.ui.screens.AddMedicationScreen
import com.surya.s35651628.medtrack.ui.screens.ClaimAccountScreen
import com.surya.s35651628.medtrack.ui.screens.ClinicianDashboardScreen
import com.surya.s35651628.medtrack.ui.screens.ClinicianLoginScreen
import com.surya.s35651628.medtrack.ui.screens.HomeScreen
import com.surya.s35651628.medtrack.ui.screens.InteractionReviewScreen
import com.surya.s35651628.medtrack.ui.screens.LoginScreen
import com.surya.s35651628.medtrack.ui.screens.MedCoachScreen
import com.surya.s35651628.medtrack.ui.screens.SettingsScreen
import com.surya.s35651628.medtrack.ui.screens.SignUpScreen
import com.surya.s35651628.medtrack.ui.screens.SymptomsScreen
import com.surya.s35651628.medtrack.ui.screens.WelcomeScreen
import com.surya.s35651628.medtrack.ui.theme.MedTrackProTheme

class MainActivity : ComponentActivity() {

    private val patientViewModel: PatientViewModel by viewModels {
        PatientViewModelFactory(
            patientRepository = AppContainer.getPatientRepository(applicationContext),
            sessionManager = SessionManager(applicationContext)
        )
    }

    private val medicationViewModel: MedicationViewModel by viewModels {
        MedicationViewModelFactory(
            medicationRepository = AppContainer.getMedicationRepository(applicationContext)
        )
    }

    private val symptomViewModel: SymptomViewModel by viewModels {
        SymptomViewModelFactory(
            symptomRepository = AppContainer.getSymptomRepository(applicationContext)
        )
    }

    private val openFdaViewModel: OpenFdaViewModel by viewModels {
        OpenFdaViewModelFactory(
            openFdaRepository = AppContainer.getOpenFdaRepository()
        )
    }

    private val genAIViewModel: GenAIViewModel by viewModels {
        GenAIViewModelFactory(
            medCoachTipRepository = AppContainer.getMedCoachTipRepository(applicationContext),
            medicationRepository = AppContainer.getMedicationRepository(applicationContext),
            symptomRepository = AppContainer.getSymptomRepository(applicationContext)
        )
    }

    private val clinicianViewModel: ClinicianViewModel by viewModels {
        ClinicianViewModelFactory(
            patientRepository = AppContainer.getPatientRepository(applicationContext),
            medicationRepository = AppContainer.getMedicationRepository(applicationContext),
            symptomRepository = AppContainer.getSymptomRepository(applicationContext)
        )
    }

    private val interactionReviewViewModel: InteractionReviewViewModel by viewModels {
        InteractionReviewViewModelFactory(
            medicationRepository = AppContainer.getMedicationRepository(applicationContext),
            interactionReviewRepository = AppContainer.getInteractionReviewRepository(
                applicationContext
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MedTrackProTheme {
                var databaseReady by remember { mutableStateOf(false) }

                // Seed CSV files into Room only once before showing the real app.
                LaunchedEffect(Unit) {
                    seedDatabaseIfNeeded()
                    databaseReady = true
                }

                if (!databaseReady) {
                    Text(text = "Setting up database...")
                } else {
                    val navController = rememberNavController()
                    val sessionManager = SessionManager(applicationContext)

                    val loggedInPatientId by patientViewModel.loggedInPatientId.collectAsState()

                    val startDestination = if (sessionManager.isLoggedIn()) {
                        "home"
                    } else {
                        "welcome"
                    }

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val bottomNavRoutes = listOf(
                        "home",
                        "symptoms",
                        "medcoach",
                        "settings"
                    )

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            // Show bottom navigation only after login.
                            if (currentRoute in bottomNavRoutes) {
                                BottomNavigationBar(
                                    currentRoute = currentRoute,
                                    onNavigate = { route ->
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    ) { paddingValues ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = startDestination
                            ) {
                                composable("welcome") {
                                    WelcomeScreen(
                                        onLoginClick = {
                                            navController.navigate("login")
                                        },
                                        onClaimAccountClick = {
                                            navController.navigate("claim_account")
                                        },
                                        onSignUpClick = {
                                            navController.navigate("signup")
                                        }
                                    )
                                }

                                composable("login") {
                                    LoginScreen(
                                        patientViewModel = patientViewModel,
                                        onLoginSuccess = {
                                            navController.navigate("home") {
                                                popUpTo("welcome") {
                                                    inclusive = true
                                                }
                                                launchSingleTop = true
                                            }
                                        },
                                        onClaimAccountClick = {
                                            navController.navigate("claim_account")
                                        },
                                        onBackClick = {
                                            navController.popBackStack()
                                        }
                                    )
                                }

                                composable("claim_account") {
                                    ClaimAccountScreen(
                                        patientViewModel = patientViewModel,
                                        onBackToLoginClick = {
                                            // If Login already exists in the back stack, return to it.
                                            val returnedToLogin = navController.popBackStack(
                                                route = "login",
                                                inclusive = false
                                            )

                                            // If the user came directly from Welcome → Claim Account,
                                            // there may be no Login screen behind it, so navigate to Login.
                                            if (!returnedToLogin) {
                                                navController.navigate("login") {
                                                    popUpTo("welcome") {
                                                        inclusive = false
                                                    }
                                                    launchSingleTop = true
                                                }
                                            }
                                        }
                                    )
                                }

                                composable("signup") {
                                    SignUpScreen(
                                        patientViewModel = patientViewModel,
                                        onBackToLoginClick = {
                                            navController.navigate("login") {
                                                popUpTo("signup") {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                    )
                                }

                                composable("home") {
                                    val patientId = loggedInPatientId
                                        ?: sessionManager.getLoggedInPatientId()

                                    if (patientId != null) {
                                        HomeScreen(
                                            patientId = patientId,
                                            patientViewModel = patientViewModel,
                                            medicationViewModel = medicationViewModel,
                                            onAddMedicationClick = {
                                                navController.navigate("add_medication")
                                            },
                                            onInteractionReviewClick = {
                                                navController.navigate("interaction_review")
                                            }
                                        )
                                    }
                                }

                                composable("add_medication") {
                                    val patientId = loggedInPatientId
                                        ?: sessionManager.getLoggedInPatientId()

                                    if (patientId != null) {
                                        AddMedicationScreen(
                                            patientId = patientId,
                                            medicationViewModel = medicationViewModel,
                                            onBackClick = {
                                                navController.popBackStack()
                                            }
                                        )
                                    }
                                }

                                composable("interaction_review") {
                                    val patientId = loggedInPatientId
                                        ?: sessionManager.getLoggedInPatientId()

                                    if (patientId != null) {
                                        InteractionReviewScreen(
                                            patientId = patientId,
                                            interactionReviewViewModel = interactionReviewViewModel,
                                            onBackClick = {
                                                navController.popBackStack()
                                            }
                                        )
                                    }
                                }

                                composable("symptoms") {
                                    val patientId = loggedInPatientId
                                        ?: sessionManager.getLoggedInPatientId()

                                    if (patientId != null) {
                                        SymptomsScreen(
                                            patientId = patientId,
                                            symptomViewModel = symptomViewModel
                                        )
                                    }
                                }

                                composable("medcoach") {
                                    val patientId = loggedInPatientId
                                        ?: sessionManager.getLoggedInPatientId()

                                    if (patientId != null) {
                                        MedCoachScreen(
                                            patientId = patientId,
                                            medicationViewModel = medicationViewModel,
                                            openFdaViewModel = openFdaViewModel,
                                            genAIViewModel = genAIViewModel
                                        )
                                    }
                                }

                                composable("settings") {
                                    val patientId = loggedInPatientId
                                        ?: sessionManager.getLoggedInPatientId()

                                    if (patientId != null) {
                                        SettingsScreen(
                                            patientId = patientId,
                                            patientViewModel = patientViewModel,
                                            onLogoutClick = {
                                                patientViewModel.logout()

                                                navController.navigate("welcome") {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        inclusive = true
                                                    }
                                                    launchSingleTop = true
                                                }
                                            },
                                            onClinicianLoginClick = {
                                                navController.navigate("clinician_login")
                                            }
                                        )
                                    }
                                }

                                composable("clinician_login") {
                                    ClinicianLoginScreen(
                                        onLoginSuccess = {
                                            navController.navigate("clinician_dashboard") {
                                                popUpTo("clinician_login") {
                                                    inclusive = true
                                                }
                                                launchSingleTop = true
                                            }
                                        },
                                        onBackClick = {
                                            navController.popBackStack()
                                        }
                                    )
                                }

                                composable("clinician_dashboard") {
                                    ClinicianDashboardScreen(
                                        clinicianViewModel = clinicianViewModel,
                                        onBackClick = {
                                            navController.navigate("settings") {
                                                popUpTo("settings") {
                                                    inclusive = false
                                                }
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun seedDatabaseIfNeeded() {
        val sharedPreferences = getSharedPreferences(
            "medtrack_prefs",
            Context.MODE_PRIVATE
        )

        val alreadySeeded = sharedPreferences.getBoolean("db_seeded", false)

        if (!alreadySeeded) {
            val csvSeeder = CsvSeeder(applicationContext)

            val patientRepository =
                AppContainer.getPatientRepository(applicationContext)

            val medicationRepository =
                AppContainer.getMedicationRepository(applicationContext)

            val symptomRepository =
                AppContainer.getSymptomRepository(applicationContext)

            // Insert CSV data into Room.
            patientRepository.insertPatients(csvSeeder.loadPatients())
            medicationRepository.insertMedications(csvSeeder.loadMedications())
            symptomRepository.insertSymptoms(csvSeeder.loadSymptoms())

            // Save flag so the CSV files are not read again next time.
            sharedPreferences.edit()
                .putBoolean("db_seeded", true)
                .apply()
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            label = { Text("Home") },
            icon = { Text("H") }
        )

        NavigationBarItem(
            selected = currentRoute == "symptoms",
            onClick = { onNavigate("symptoms") },
            label = { Text("Symptoms") },
            icon = { Text("S") }
        )

        NavigationBarItem(
            selected = currentRoute == "medcoach",
            onClick = { onNavigate("medcoach") },
            label = { Text("MedCoach") },
            icon = { Text("M") }
        )

        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") },
            label = { Text("Settings") },
            icon = { Text("⚙") }
        )
    }
}
