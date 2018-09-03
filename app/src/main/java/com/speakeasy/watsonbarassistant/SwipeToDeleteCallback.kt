package com.speakeasy.watsonbarassistant

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View

abstract class SwipeToDeleteCallback(context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24dp)
    private val intrinsicWidth = deleteIcon?.intrinsicWidth
    private val intrinsicHeight = deleteIcon?.intrinsicHeight
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#f44336")

    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        return false
    }

    override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val itemView = viewHolder?.itemView ?: return

        background.color = backgroundColor
        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right,
                itemView.bottom)
        background.draw(c)

        setIconBounds(itemView)
        deleteIcon?.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun setIconBounds(itemView: View) {
        val itemHeight = itemView.bottom - itemView.top

        val iconHeight = intrinsicHeight ?: 0
        val iconWidth = intrinsicWidth ?: 0

        val iconTop = itemView.top + (itemHeight - iconHeight) / 2
        val iconMargin = (itemHeight - iconWidth) / 2
        val iconLeft = itemView.right - iconMargin - iconWidth
        val iconRight = itemView.right - iconMargin
        val iconBottom = iconTop + iconHeight

        deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
    }
}