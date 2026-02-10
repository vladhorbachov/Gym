package com.gymshark.data.training.mapper

import com.gymshark.domain.models.PrUpdate
import com.gymshark.ui.home.training.drafts.TrainingDraft
import com.gymshark.ui.home.training.drafts.isPerformed
import com.gymshark.ui.home.training.drafts.normalizedWeightOrNull

class PrUpdateMapper {

    fun buildPrUpdates(draft: TrainingDraft): List<PrUpdate> {
        return draft.exercises.map { ex ->
            val performed = ex.sets.filter { it.isPerformed() }
            val addedSets = performed.size

            val sessionMaxWeight: Float? = performed
                .mapNotNull { it.normalizedWeightOrNull() }
                .maxOrNull()

            PrUpdate(
                exerciseId = ex.exerciseId.toInt(),
                exerciseName = ex.title,
                addedSets = addedSets,
                sessionMaxWeight = sessionMaxWeight
            )
        }.filter { it.addedSets > 0 }
    }
}
