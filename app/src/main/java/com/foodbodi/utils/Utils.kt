package com.foodbodi.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import androidx.fragment.app.FragmentActivity
import android.view.inputmethod.InputMethodManager


class Utils {

    companion object {
        fun showAlert(message: String, context: FragmentActivity, completion: () -> Unit) {
            val dialogBuilder = AlertDialog.Builder(context)
            // set message of alert dialog
            dialogBuilder.setMessage(message)
                // if the dialog is cancelable
                .setCancelable(false)
                // positive button text and action
                .setPositiveButton("Ok", DialogInterface.OnClickListener {
                        dialog, id -> completion.invoke()
                })
                // negative button text and action
                .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                })

            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("Warning")
            // show alert dialog
            alert.show()
        }

        fun hideSoftKeyboard(activity: Activity) {

            val inputMethodManager = activity.getSystemService(
                Activity.INPUT_METHOD_SERVICE
            ) as? InputMethodManager

            val inputMethodManagerUr = inputMethodManager ?: return
            var currentFocus = activity.currentFocus ?: return

            inputMethodManagerUr.hideSoftInputFromWindow(
                currentFocus.windowToken, 0
            )
        }
    }
}