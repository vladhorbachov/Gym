package com.gymshark.ui.home.training

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gymshark.databinding.BsExerciseInfoBinding

class ExerciseInfoBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BsExerciseInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = BsExerciseInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.45f)
        }
        val sheet = (dialog as? BottomSheetDialog)
            ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        sheet?.setBackgroundColor(Color.TRANSPARENT)
        sheet?.layoutParams?.height = (resources.displayMetrics.heightPixels * 0.82f).toInt()
        sheet?.requestLayout()
        sheet?.let {
            BottomSheetBehavior.from(it).apply {
                isDraggable = false
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        applyBottomSheetInsets()

        val title = requireArguments().getString(ARG_TITLE).orEmpty()
        val category = requireArguments().getString(ARG_CATEGORY).orEmpty()
        val difficulty = requireArguments().getInt(ARG_DIFFICULTY, 1).coerceIn(1, 3)
        val guide = TechniqueGuide.create(title, category)

        binding.tvTitle.text = title.ifBlank { "Exercise" }
        binding.tvMeta.text = listOfNotNull(
            category.takeIf { it.isNotBlank() },
            "Difficulty $difficulty/3"
        ).joinToString(" - ")
        binding.tvSetup.text = "Setup"
        binding.tvExecution.text = guide.execution
        binding.tvCues.text = guide.cues.joinToString(separator = "\n") { "- $it" }
        binding.tvMistakes.text = guide.mistakes.joinToString(
            prefix = "Avoid\n",
            separator = "\n"
        ) { "- $it" }
        binding.btnClose.setOnClickListener { dismiss() }
    }

    private fun applyBottomSheetInsets() {
        val baseBottomPadding = (14 * resources.displayMetrics.density).toInt()

        ViewCompat.setOnApplyWindowInsetsListener(binding.infoSheetContent) { content, insets ->
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            content.updatePadding(bottom = baseBottomPadding + nav.bottom)

            insets
        }

        ViewCompat.requestApplyInsets(binding.infoSheetContent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class Guide(
        val execution: String,
        val cues: List<String>,
        val mistakes: List<String>
    )

    private object TechniqueGuide {
        fun create(title: String, category: String): Guide {
            val normalized = "$title $category".lowercase()
            return when {
                normalized.hasAny("squat", "прис", "leg", "ног") -> Guide(
                    execution = "Stand tall, brace your trunk, and keep the whole foot planted. Lower under control until the working joints reach a strong, stable range, then drive back up without losing posture.",
                    cues = listOf(
                        "Brace before every rep and keep the ribs stacked over the pelvis.",
                        "Track knees in the same direction as the toes.",
                        "Control the descent; accelerate only after you own the bottom position."
                    ),
                    mistakes = listOf(
                        "Letting the knees collapse inward.",
                        "Lifting the heels or shifting all weight to the toes.",
                        "Rushing reps before the torso is stable."
                    )
                )
                normalized.hasAny("press", "жим", "груд", "chest", "shoulder", "плеч") -> Guide(
                    execution = "Set the shoulder blades, keep wrists stacked over elbows, and press through a controlled path. Lower smoothly to the start position, pause briefly if needed, then press without bouncing.",
                    cues = listOf(
                        "Keep shoulders packed and avoid shrugging into the ears.",
                        "Use a full controlled range that does not cause joint pain.",
                        "Exhale through the hard part while keeping the torso braced."
                    ),
                    mistakes = listOf(
                        "Flaring elbows aggressively from the first rep.",
                        "Bouncing the weight or losing wrist alignment.",
                        "Turning the movement into a back arch instead of a press."
                    )
                )
                normalized.hasAny("row", "pull", "тяга", "спин", "back") -> Guide(
                    execution = "Start from a long, stable position, then pull by driving the elbows back. Keep the neck neutral and finish with the shoulder blades moving naturally, not by jerking the torso.",
                    cues = listOf(
                        "Initiate with the back and elbows, not the hands.",
                        "Pause briefly at the strongest contracted position.",
                        "Return the weight under control until the target muscles stretch."
                    ),
                    mistakes = listOf(
                        "Using momentum from the hips or lower back.",
                        "Shrugging instead of pulling the elbows back.",
                        "Cutting the controlled stretch short."
                    )
                )
                normalized.hasAny("curl", "extension", "біц", "триц", "arm", "рук") -> Guide(
                    execution = "Lock in a stable upper arm position, move through the elbow, and keep the wrist neutral. Use a weight you can control without swinging.",
                    cues = listOf(
                        "Keep the upper arm quiet so the target muscle does the work.",
                        "Control both the lifting and lowering phase.",
                        "Stop the set when form turns into body swing."
                    ),
                    mistakes = listOf(
                        "Throwing the weight with the torso.",
                        "Letting wrists bend under load.",
                        "Shortening the range to chase heavier weight."
                    )
                )
                else -> Guide(
                    execution = "Set a stable position, brace, and move through a controlled pain-free range. Keep the target area loaded while the rest of the body stays quiet.",
                    cues = listOf(
                        "Own the start position before the first rep.",
                        "Use smooth tempo and consistent range on every rep.",
                        "Stop one or two reps before technique breaks."
                    ),
                    mistakes = listOf(
                        "Changing body position to make the rep easier.",
                        "Letting speed replace control.",
                        "Ignoring discomfort in joints or lower back."
                    )
                )
            }
        }

        private fun String.hasAny(vararg needles: String): Boolean =
            needles.any { contains(it) }
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_CATEGORY = "arg_category"
        private const val ARG_DIFFICULTY = "arg_difficulty"

        fun newInstance(title: String, category: String, difficulty: Int = 1) =
            ExerciseInfoBottomSheet().apply {
                arguments = bundleOf(
                    ARG_TITLE to title,
                    ARG_CATEGORY to category,
                    ARG_DIFFICULTY to difficulty
                )
            }
    }
}
