package com.surya.s35651628.medtrack.data.interaction

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InteractionReviewDao {

    // Inserts a list of interaction reviews into the database.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<InteractionReview>)

    // Observes all saved interaction reviews for one patient.
    // Flow is used so the UI can update automatically when the data changes.
    @Query(
        """
        SELECT * FROM interaction_reviews
        WHERE patientId = :patientId
        ORDER BY reviewId ASC
        """
    )
    fun observeReviewsForPatient(patientId: String): Flow<List<InteractionReview>>

    // Deletes old reviews for the patient before saving the latest AI review.
    @Query("DELETE FROM interaction_reviews WHERE patientId = :patientId")
    suspend fun clearReviewsForPatient(patientId: String)
}