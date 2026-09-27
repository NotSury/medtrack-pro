package com.surya.s35651628.medtrack.session

import android.content.Context

class SessionManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(
        "medtrack_prefs",
        Context.MODE_PRIVATE
    )

    // Save the logged-in patient ID so the app remembers the user after restart.
    fun saveLoggedInPatientId(patientId: String) {
        sharedPreferences.edit()
            .putString("logged_in_patient_id", patientId)
            .apply()
    }

    // Read saved patient ID. If null, no user is logged in.
    fun getLoggedInPatientId(): String? {
        return sharedPreferences.getString("logged_in_patient_id", null)
    }

    // Clear session during logout.
    fun clearSession() {
        sharedPreferences.edit()
            .remove("logged_in_patient_id")
            .apply()
    }

    // Helper function for checking login state.
    fun isLoggedIn(): Boolean {
        return getLoggedInPatientId() != null
    }
}