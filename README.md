# MedTrack Pro - README

## GenAI Declaration

This app uses the Google Gemini API through Google AI Studio:

https://aistudio.google.com/

Gemini is used in the following app features:

1. MedCoach Medication Tips  
   File: `app/src/main/java/com/surya/s35651628/medtrack/data/genai/GenAIViewModel.kt`  
   Purpose: Generates a personalized medication adherence tip using the logged-in patient's medication list and symptom history.

2. Clinician Dashboard Insights  
   File: `app/src/main/java/com/surya/s35651628/medtrack/data/clinician/ClinicianViewModel.kt`  
   Purpose: Generates three aggregate, data-driven observations for the clinician dashboard using medication and symptom statistics.

3. AI Drug Interaction Review  
   File: `app/src/main/java/com/surya/s35651628/medtrack/data/interaction/InteractionReviewViewModel.kt`  
   Purpose: Reviews the logged-in patient's medication combinations and generates possible interaction risk levels with short explanations.


## API Key

To run the GenAI features, place a Gemini API key in local.properties using this format:

apiKey=YOUR_API_KEY