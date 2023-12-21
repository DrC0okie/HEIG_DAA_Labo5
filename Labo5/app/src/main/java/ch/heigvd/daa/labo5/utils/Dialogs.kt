package ch.heigvd.daa.labo5.utils

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import ch.heigvd.daa.labo5.R

/**
 * Utility object containing methods for displaying common dialogs in the application.
 * @author Timothée Van Hove, Léo Zmoos
 */
object Dialogs {

    /**
     * Shows a dialog indicating that there is no internet connection available.
     * @param context The context in which the dialog should be shown.
     * @param inflater The LayoutInflater used to inflate the dialog's layout.
     */
    fun showNoConnectionDialog(context: Context, inflater: LayoutInflater) {
        val dialogView = inflater.inflate(R.layout.dialog_no_connection, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView).setNegativeButton("Ok") { dialog, _ -> dialog.dismiss()}.create()

        dialog.show()
    }
}