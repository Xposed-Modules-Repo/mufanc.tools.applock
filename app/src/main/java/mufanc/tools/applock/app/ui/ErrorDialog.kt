package mufanc.tools.applock.app.ui

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.widget.TextView
import androidx.cardview.widget.CardView
import mufanc.tools.applock.R
import mufanc.tools.applock.databinding.ErrorDialogBinding

object ErrorDialog {
    private val colorSuccess = Color.parseColor("#7bed9f")
    private val colorFailed = Color.parseColor("#ff6b81")

    private fun setStyle(
        available: Boolean, card: CardView,
        positive: Int, negative: Int
    ) {
        if (available) {
            card.setCardBackgroundColor(colorSuccess)
            (card.getChildAt(0) as TextView).setText(positive)
        } else {
            card.setCardBackgroundColor(colorFailed)
            (card.getChildAt(0) as TextView).setText(negative)
        }
    }

    fun get(activity: Activity, p1: Boolean, p2: Boolean, p3: Boolean): AlertDialog {
        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.error_dialog_title)
            .setPositiveButton(R.string.error_dialog_positive) {
                _, _ -> activity.finish()
            }.create()

        val binding = ErrorDialogBinding.inflate(dialog.layoutInflater)
        dialog.setView(binding.root)
        dialog.setCanceledOnTouchOutside(false)
        with (binding) {
            setStyle(p1, moduleActivatedCard, R.string.module_activated_yes, R.string.module_activated_no)
            setStyle(p2, serviceFoundCard, R.string.service_found_yes, R.string.service_found_no)
            setStyle(p3, connectionTestCard, R.string.connect_test_yes, R.string.connect_test_no)
        }

        return dialog
    }
}