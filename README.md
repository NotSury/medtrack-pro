# MedTrack Pro

MedTrack Pro is an Android medication management application built with Kotlin and Jetpack Compose.

The app combines local data persistence, API integration, MVVM architecture, and GenAI features to help users manage medications, symptoms, and personalised health-related information.

## Features

- Multi-user login and account management
- Persistent user sessions
- Medication tracking and management
- Symptom logging and history
- Room database for local data storage
- OpenFDA drug information lookup
- Personalised GenAI medication tips
- Clinician dashboard with aggregate patient statistics
- AI-generated clinician insights
- AI-assisted drug interaction review
- Persistent medication "taken" status

## Tech Stack

- Kotlin
- Jetpack Compose
- MVVM Architecture
- Room Database
- Retrofit
- Coroutines
- StateFlow
- Google Gemini API
- OpenFDA Drug Label API
- Android Studio

## Architecture

The application follows the MVVM architecture pattern:

```text
Composable UI
    ↓
ViewModel
    ↓
Repository
    ↓
DAO
    ↓
Room Database
```

The UI observes state from ViewModels, while database and network operations are handled through repositories and asynchronous coroutines.
Core Functionality
Medication Management
Users can view, add, and manage medications associated with their account.
Medication data is stored locally using Room and linked to individual users.
Symptom Tracking
Users can record symptoms, severity, notes, and timestamps.
Symptom history is stored in the local database and linked to the logged-in user.
MedCoach
MedCoach provides:
- Drug information lookup using the OpenFDA Drug Label API
- Personalised medication tips generated using Google Gemini
Patient medication and symptom information can be included in the prompt to generate more relevant responses.
Clinician Dashboard
The clinician dashboard provides aggregate patient information such as:
- Total number of patients
- Average medications per patient
- Most common symptom category
- Average symptom severity
The dashboard can also send aggregated statistics to Gemini to generate data-driven observations.
AI Drug Interaction Review
The application can review a user's current medication combination and generate possible interaction risk levels and short explanations using Gemini.
Database
The application uses Room for structured local storage.
Main entities include:
- Patient
- Medication
- Symptom
- MedCoachTips
Relationships are maintained between users and their associated medication, symptom, and AI-generated data.
API Integration
OpenFDA
The OpenFDA Drug Label API is used to retrieve medication information such as:
- Purpose
- Warnings
- Dosage and administration
Retrofit and coroutines are used for asynchronous network requests.
Google Gemini
Gemini is used for:
- Personalised medication adherence tips
- Clinician dashboard insights
- Drug interaction review


### Home Screen
<img width="1080" height="2424" alt="Screenshot1" src="https://github.com/user-attachments/assets/a11f76e8-56d1-4b5d-8eb4-d70510f7bc5f" />

### Drug Interaction Review
<img width="1080" height="2424" alt="Screenshot2" src="https://github.com/user-attachments/assets/44e10884-fbac-4c17-9d96-ddfd910a5eb3" />

