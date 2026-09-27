package com.surya.s35651628.medtrack.data.interaction

import kotlinx.coroutines.flow.Flow

class InteractionReviewRepository(
    private val interactionReviewDao: InteractionReviewDao
) {
    suspend fun insertReviews(reviews: List<InteractionReview>) {
        interactionReviewDao.insertReviews(reviews)
    }

    fun observeReviewsForPatient(patientId: String): Flow<List<InteractionReview>> {
        return interactionReviewDao.observeReviewsForPatient(patientId)
    }

    suspend fun clearReviewsForPatient(patientId: String) {
        interactionReviewDao.clearReviewsForPatient(patientId)
    }
}