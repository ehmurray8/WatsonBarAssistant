package com.speakeasy.watsonbarassistant

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.app.DialogFragment


class PictureDialog: DialogFragment() {

    companion object {
        var cameraHandler: (() -> Unit)? = null
        var fileHandler: (() -> Unit)? = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        val dialog = AlertDialog.Builder(activity)
                .setTitle("Recipe Image")
                .setCancelable(true)
                .setNegativeButton("Upload Image") { _, _ ->
                    fileHandler?.invoke()
                }.setPositiveButton("Take Picture") { _, _ ->
                    cameraHandler?.invoke()
                }.create()
        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val fileButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            // if you do the following it will be left aligned, doesn't look
            // correct
            // button.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play,
            // 0, 0, 0);

            val drawable = activity?.resources?.getDrawable(R.drawable.ic_camera_add)
            val fileDrawable = activity?.resources?.getDrawable(R.drawable.ic_insert_drive_file_black_24dp)

            // set the bounds to place the drawable a bit right
            drawable?.setBounds((drawable.intrinsicWidth * 0.5).toInt(), 0,
                    (drawable.intrinsicWidth * 1.5).toInt(), drawable.intrinsicHeight)
            button.setCompoundDrawables(drawable, null, null, null)

            fileDrawable?.setBounds((fileDrawable.intrinsicWidth * 0.5).toInt(), 0,
                    (fileDrawable.intrinsicWidth * 1.5).toInt(), fileDrawable.intrinsicHeight)
            fileButton.setCompoundDrawables(fileDrawable, null, null, null)


            // could modify the placement more here if desired
            // button.setCompoundDrawablePadding();
        }
        return dialog
    }
}