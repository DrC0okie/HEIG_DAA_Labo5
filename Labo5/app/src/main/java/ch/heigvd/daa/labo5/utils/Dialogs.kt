package ch.heigvd.daa.labo5.utils

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import ch.heigvd.daa.labo5.R

object Dialogs {
    fun showNoConnectionDialog(context: Context, inflater: LayoutInflater) {
        val dialogView = inflater.inflate(R.layout.dialog_no_connection, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView).setNegativeButton("Ok") { dialog, _ -> dialog.dismiss()}.create()

        dialog.show()
    }
}