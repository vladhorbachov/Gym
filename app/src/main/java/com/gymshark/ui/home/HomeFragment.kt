package com.gymshark.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.R
import com.gymshark.data.db.entity.ExercisesEntity
import com.gymshark.databinding.FragmentHomeBinding
import com.gymshark.ui.home.adapter.ExerciseListItem
import com.gymshark.ui.home.adapter.RecommendedExercisesAdapter
import com.gymshark.ui.home.training.ExerciseInfoBottomSheet
import com.gymshark.ui.home.training.TrainingViewModel
import com.gymshark.utils.BaseAlert
import com.gymshark.utils.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val trainingVm: TrainingViewModel by activityViewModel()

    private var lastSelectedTypes: List<String> = emptyList()
    private var selectedDate: LocalDate = LocalDate.now()
    private val recommendedAdapter by lazy {
        RecommendedExercisesAdapter { exercise ->
            ExerciseInfoBottomSheet
                .newInstance(
                    title = exercise.name,
                    category = exercise.baseCategory,
                    difficulty = exercise.difficulty
                )
                .show(parentFragmentManager, "exercise_info")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExercises.adapter = recommendedAdapter

        attachTouchHelper()
        binding.todayCard.root.setOnClickListener {
            if (trainingVm.draft.value.exercises.isEmpty()) return@setOnClickListener
            if (!hasTrainingPlan()) {
                showTrainingPlanMissingAlert()
                return@setOnClickListener
            }

            findNavController().navigate(R.id.action_navHome_to_prepareFragment)
        }
        binding.btnRestSetup.setOnClickListener {
            findNavController().navigate(R.id.action_navHome_to_navProfile)
        }

        binding.cvCalendar.onDayClick = label@{ date, _, types ->
            if (date == selectedDate && types == lastSelectedTypes) return@label
            selectedDate = date
            lastSelectedTypes = types
            renderRestTipsIfNeeded()
            trainingVm.loadSuggestedExercises(types)
        }
        binding.cvCalendar.onTrainingDropped = { sourceDate, targetDate ->
            trainingVm.moveTrainingSlot(sourceDate, targetDate)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    trainingVm.suggestedExercisesFlow.collect { list ->
                        trainingVm.setExercisesFromSuggested(list)
                    }
                }
                launch {
                    trainingVm.trainingDaysFlow.collect { calendarDays ->
                        binding.cvCalendar.setTrainingDays(calendarDays)
                    }
                }
                launch {
                    trainingVm.trainingSlotsFlow.collect { slots ->
                        binding.cvCalendar.setTrainingSlots(slots)
                    }
                }
                launch {
                    trainingVm.draft.collect { draft ->
                        val sectionedItems = buildList {
                            var lastCategory: String? = null
                            draft.exercises.forEach { draftExercise ->
                                if (draftExercise.baseCategory != lastCategory) {
                                    add(ExerciseListItem.Header(draftExercise.baseCategory))
                                    lastCategory = draftExercise.baseCategory
                                }
                                add(
                                    ExerciseListItem.ExerciseRow(
                                        ExercisesEntity(
                                            id = draftExercise.exerciseId.toInt(),
                                            name = draftExercise.title,
                                            baseCategory = draftExercise.baseCategory,
                                            subCategory = "",
                                            difficulty = 1
                                        )
                                    )
                                )
                            }
                        }
                        updateTodayCard(draft.exercises.size)
                        binding.rvExercises.isVisible = draft.exercises.isNotEmpty()
                        binding.restTipsPanel.isVisible = draft.exercises.isEmpty() && lastSelectedTypes.isEmpty()
                        renderRestTipsIfNeeded()
                        recommendedAdapter.submitList(sectionedItems)
                    }
                }
                launch {
                    trainingVm.trainsFlow.collect { trains ->
                        binding.cvCalendar.setTrains(trains)
                    }
                }
                launch {
                    trainingVm.completedExerciseIdsFlow.collect { ids ->
                        recommendedAdapter.setCompletedIds(ids)
                    }
                }
            }
        }
    }

    private fun updateTodayCard(exCount: Int) {
        val hasSetup = hasTrainingPlan()
        binding.todayCard.root.alpha = 1f
        binding.todayCard.root.isClickable = exCount > 0
        binding.todayCard.root.setBackgroundResource(
            when {
                exCount > 0 -> R.drawable.bg_home_today_workout
                !hasSetup -> R.drawable.bg_home_today_setup
                else -> R.drawable.bg_home_today_rest
            }
        )

        val title = if (lastSelectedTypes.isEmpty()) {
            "Rest day"
        } else {
            lastSelectedTypes.joinToString(", ")
                .replaceFirstChar { it.uppercase() }
        }

        binding.todayCard.tvToday.text = selectedDateLabel()
        binding.todayCard.tvTrainingName.text = title
        binding.todayCard.tvMeta.text = when {
            exCount > 0 -> "$exCount exercises"
            !hasSetup -> "Set days and muscle groups"
            lastSelectedTypes.isEmpty() -> "Recovery tips"
            else -> "No exercises"
        }
    }

    private fun selectedDateLabel(): String {
        val today = LocalDate.now()
        return when (selectedDate) {
            today -> "Today"
            today.plusDays(1) -> "Tomorrow"
            today.minusDays(1) -> "Yesterday"
            else -> selectedDate.dayOfWeek
                .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                .replaceFirstChar { it.uppercase() } + " · ${selectedDate.dayOfMonth}.${selectedDate.monthValue}"
        }
    }

    private fun renderRestTipsIfNeeded() {
        if (lastSelectedTypes.isNotEmpty()) return

        val tips = restTipsForDate(selectedDate)
        binding.tvRestTitle.text = if (selectedDate == LocalDate.now()) {
            "Rest day"
        } else {
            "Rest day · ${selectedDate.dayOfMonth}.${selectedDate.monthValue}"
        }
        binding.tvRestSubtitle.text = "Recovery is part of the plan. Keep the day light, useful, and consistent."
        val hasSetup = hasTrainingPlan()
        binding.btnRestSetup.isVisible = !hasSetup
        if (!hasSetup) {
            binding.tvRestSubtitle.text = "Set your weekly rhythm first, then rest days and workout days become clearer."
        }
        binding.tvRestTipOne.text = tips[0].format()
        binding.tvRestTipTwo.text = tips[1].format()
        binding.tvRestTipThree.text = tips[2].format()
        binding.tvRestTipFour.text = tips[3].format()
    }

    private fun restTipsForDate(date: LocalDate): List<RestTip> {
        val categories = listOf(
            nutritionTips,
            recoveryTips,
            dayStyleTips,
            mindsetTips
        )
        return categories.mapIndexed { index, list ->
            val tipIndex = abs((date.toEpochDay() + index * 11).toInt()) % list.size
            list[tipIndex]
        }
    }

    private fun RestTip.format(): String = "$label\n$text"

    private data class RestTip(
        val label: String,
        val text: String
    )

    private val nutritionTips = listOf(
        RestTip("Nutrition", "Keep protein steady today. Recovery is easier when every meal has a clear protein source."),
        RestTip("Nutrition", "Add one colorful fruit or vegetable to two meals. Micronutrients matter on rest days too."),
        RestTip("Nutrition", "Do not slash calories just because you are not training. Your body is still repairing tissue."),
        RestTip("Nutrition", "Drink water early in the day, then keep sipping. Hydration helps joints and training quality tomorrow."),
        RestTip("Nutrition", "Choose slow carbs if you feel flat: oats, rice, potatoes, beans, or whole-grain bread."),
        RestTip("Nutrition", "Keep salt reasonable, especially if you sweat a lot. Low sodium can feel like low energy."),
        RestTip("Nutrition", "Make one simple recovery plate: protein, carbs, vegetables, and a small fat source."),
        RestTip("Nutrition", "If cravings hit, start with a real meal first. Rest days are easier when hunger is handled."),
        RestTip("Nutrition", "Prep one meal for tomorrow. Future you starts training with less friction."),
        RestTip("Nutrition", "A rest day is not a punishment day. Eat like someone who wants to perform tomorrow.")
    )

    private val recoveryTips = listOf(
        RestTip("Recovery", "Take a 20-30 minute easy walk. Keep it light enough that breathing stays calm."),
        RestTip("Recovery", "Do 5 minutes of mobility for the tightest area, not a full workout in disguise."),
        RestTip("Recovery", "Sleep is the main lift today. Set a simple bedtime target and protect it."),
        RestTip("Recovery", "If soreness is high, use gentle movement and warm showers instead of forcing intensity."),
        RestTip("Recovery", "Check your posture once today: ribs down, shoulders relaxed, jaw unclenched."),
        RestTip("Recovery", "Spend a few minutes outside. Daylight helps energy and sleep rhythm."),
        RestTip("Recovery", "Keep steps moderate. Recovery does not mean frozen, but it also does not mean cardio day."),
        RestTip("Recovery", "Stretch lightly after a walk, when tissues are warm. Skip aggressive pain-range stretching."),
        RestTip("Recovery", "If a joint feels irritated, note it now. Adjust tomorrow before it becomes a problem."),
        RestTip("Recovery", "Use rest to refill, not to test yourself. Leave the heavy effort for training days.")
    )

    private val dayStyleTips = listOf(
        RestTip("Day style", "Make the day clean and simple: meals, walk, sleep. No need to optimize everything."),
        RestTip("Day style", "Do one small chore that makes tomorrow easier: pack clothes, fill a bottle, plan breakfast."),
        RestTip("Day style", "Keep caffeine earlier than usual if sleep has been weak."),
        RestTip("Day style", "Take 10 quiet minutes without scrolling. Recovery is also nervous-system work."),
        RestTip("Day style", "Review your last workout and pick one thing to improve next time."),
        RestTip("Day style", "Avoid turning rest into guilt. The plan includes this day for a reason."),
        RestTip("Day style", "If you want movement, choose easy pace and stop while it still feels easy."),
        RestTip("Day style", "Clean up your training playlist or notes. Keep the ritual alive without extra fatigue."),
        RestTip("Day style", "Put tomorrow's workout time on the calendar. A clear slot beats motivation."),
        RestTip("Day style", "Keep the evening boring in a good way: food, shower, low light, sleep.")
    )

    private val mindsetTips = listOf(
        RestTip("Mindset", "You do not get stronger only while lifting. You adapt when you recover."),
        RestTip("Mindset", "Consistency includes restraint. Skipping unnecessary work today protects tomorrow."),
        RestTip("Mindset", "A good rest day should make you want to train, not prove you are tough."),
        RestTip("Mindset", "Your job today is to show up for recovery with the same respect as training."),
        RestTip("Mindset", "Progress is not always more. Sometimes progress is better timing."),
        RestTip("Mindset", "If you feel impatient, that is normal. Let the plan do its work."),
        RestTip("Mindset", "Rest is not falling behind. It is paying down fatigue."),
        RestTip("Mindset", "Small choices count today: water, walk, meal, sleep. That is enough."),
        RestTip("Mindset", "Strong athletes know when to press and when to back off."),
        RestTip("Mindset", "Tomorrow's session starts with how you treat yourself today.")
    )

    private fun hasTrainingPlan(): Boolean {
        val hasDays = trainingVm.trainingDaysFlow.value.isNotEmpty()
        val hasMuscleGroups = trainingVm.trainingSlotsFlow.value.any { it.types.isNotEmpty() }
        return hasDays && hasMuscleGroups
    }

    private fun showTrainingPlanMissingAlert() {
        val hasDays = trainingVm.trainingDaysFlow.value.isNotEmpty()
        val hasMuscleGroups = trainingVm.trainingSlotsFlow.value.any { it.types.isNotEmpty() }

        val missingParts = buildList {
            if (!hasDays) add("training days")
            if (!hasMuscleGroups) add("muscle groups")
        }

        BaseAlert(requireContext())
            .title("Training setup required")
            .message("Select ${missingParts.joinToString(" and ")} in profile before starting the workout.")
            .positiveButton("Open profile", action = {
                findNavController().navigate(R.id.action_navHome_to_navProfile)
            })
            .show()
    }

    private fun attachTouchHelper() {
        val callback = object : androidx.recyclerview.widget.ItemTouchHelper.Callback() {

            override fun isLongPressDragEnabled() = true
            override fun isItemViewSwipeEnabled() = true

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val pos = viewHolder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return 0

                return when (recommendedAdapter.getItemViewType(pos)) {
                    0 -> makeMovementFlags(0, 0)
                    else -> makeMovementFlags(
                        androidx.recyclerview.widget.ItemTouchHelper.UP or
                            androidx.recyclerview.widget.ItemTouchHelper.DOWN,
                        androidx.recyclerview.widget.ItemTouchHelper.RIGHT
                    )
                }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition

                if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION) return false
                if (recommendedAdapter.getItemViewType(toPos) == 0) return false

                val fromExerciseIndex = getExerciseIndex(fromPos)
                val toExerciseIndex = getExerciseIndex(toPos)

                if (fromExerciseIndex == -1 || toExerciseIndex == -1) return false

                trainingVm.reorderExercises(fromExerciseIndex, toExerciseIndex)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return
                if (recommendedAdapter.getItemViewType(pos) == 0) return

                val exerciseIndex = getExerciseIndex(pos)
                if (exerciseIndex == -1) return

                trainingVm.removeExerciseAt(exerciseIndex)
            }

            private fun getExerciseIndex(adapterPosition: Int): Int {
                val currentList = recommendedAdapter.currentList

                var exerciseIndex = -1
                var counter = -1

                currentList.forEachIndexed { index, item ->
                    if (item is ExerciseListItem.ExerciseRow) {
                        counter++
                    }
                    if (index == adapterPosition) {
                        exerciseIndex = counter
                    }
                }

                return exerciseIndex
            }
        }

        androidx.recyclerview.widget.ItemTouchHelper(callback)
            .attachToRecyclerView(binding.rvExercises)
    }
}
