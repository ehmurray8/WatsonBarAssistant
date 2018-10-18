package com.speakeasy.watsonbarassistant

import android.app.Activity
import android.graphics.Paint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class ShoppingCartAdapter(private val shoppingCartItems:  MutableMap<Ingredient, Boolean>,
                          private val orderedItems: MutableList<Ingredient>,
                          private val activity: Activity):
        RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder>() {

    class ViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingCartAdapter.ViewHolder {
        val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.shopping_cart_item_view, parent,false) as LinearLayout
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val button = holder.layout.getChildAt(0) as Button
        val itemText = holder.layout.getChildAt(1) as TextView

        val ingredient = orderedItems[position]
        itemText.text = ingredient.name.capitalize()

        val selected = shoppingCartItems[ingredient]
        if(selected != null) {
            if(selected) {
                setNeeded(button, itemText)
            } else {
                setNotNeeded(button, itemText)
            }
        }

        button.setOnClickListener {
            val isSelected = shoppingCartItems[ingredient]
            if(isSelected != null) {
                shoppingCartItems[ingredient] = !isSelected
                if (isSelected) {
                    setNotNeeded(button, itemText)
                } else {
                    setNeeded(button, itemText)
                }
            }
        }
    }

    private fun setNeeded(button: Button, itemText: TextView) {
        button.background = activity.getDrawable(R.drawable.empty_circle)
        itemText.paintFlags = itemText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }

    private fun setNotNeeded(button: Button, itemText: TextView) {
        button.background = activity.getDrawable(R.drawable.full_circle)
        itemText.paintFlags = itemText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }

    override fun getItemCount(): Int = orderedItems.count()
}
