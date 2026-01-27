package com.gymshark.data.training.mapper

import com.gymshark.data.db.entity.TrainingExerciseEntity
import com.gymshark.data.db.entity.TrainingSetEntity
import com.gymshark.ui.home.training.drafts.TrainingDraft
import com.gymshark.ui.home.training.drafts.isPerformed
import com.gymshark.ui.home.training.drafts.normalizedWeightOrNull

class TrainingDraftMapper {

    fun buildDetails(
        draft: TrainingDraft
    ): List<Pair<TrainingExerciseEntity, List<TrainingSetEntity>>> {

        return draft.exercises.mapIndexed { index, ex ->
            val exEntity = TrainingExerciseEntity(
                trainingId = 0,
                exerciseId = ex.exerciseId.toInt(),
                orderIndex = index
            )

            val sets = ex.sets
                .filter { it.isPerformed() }
                .map { s ->
                    TrainingSetEntity(
                        trainingExerciseId = 0,
                        reps = s.reps,
                        weight = s.normalizedWeightOrNull()
                    )
                }

            exEntity to sets
        }
    }
}
