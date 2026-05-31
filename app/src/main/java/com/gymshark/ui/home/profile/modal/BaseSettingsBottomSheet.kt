package com.gymshark.ui.home.profile.modal

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gymshark.R

abstract class BaseSettingsBottomSheet : BottomSheetDialogFragment() {

    protected open val sheetHeightRatio: Float = 0.78f

    override fun getTheme(): Int = R.style.ThemeOverlay_Gym_BottomSheet

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)

        dialog.setOnShowListener { d ->

            val sheet =
                (d as BottomSheetDialog)
                    .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    ?: return@setOnShowListener

            sheet.setBackgroundColor(Color.TRANSPARENT)

            val behavior = BottomSheetBehavior.from(sheet)

            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = true

            sheet.layoutParams.height =
                if (sheetHeightRatio > 0f) {
                    (resources.displayMetrics.heightPixels * sheetHeightRatio).toInt()
                } else {
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                }
            sheet.requestLayout()

            ViewCompat.setOnApplyWindowInsetsListener(sheet) { v, insets ->
                val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                v.updatePadding(bottom = nav.bottom)
                insets
            }
            ViewCompat.requestApplyInsets(sheet)
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {

            setBackgroundDrawableResource(android.R.color.transparent)

            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.45f)
        }
    }
}
