package com.speakeasy.watsonbarassistant.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.speakeasy.watsonbarassistant.Ingredient
import com.speakeasy.watsonbarassistant.R
import java.util.*

class IngredientAdapter(private val ingredientsSet: TreeSet<Ingredient>):
        RecyclerView.Adapter<IngredientAdapter.ViewHolder>() {

    private val ingredientsList: MutableList<Ingredient>
    get() {
        return ingredientsSet.toMutableList()
    }

    class ViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.ingredient_view,
                parent, false) as LinearLayout
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val textView = holder.layout.getChildAt(1) as? TextView
        textView?.text = ingredientsList[position].name.capitalize()
    }

    override fun getItemCount(): Int = ingredientsList.count()

    fun removeAt(position: Int) {
        val ingredient = ingredientsList[position]
        ingredientsSet.removeIf { it.name == ingredient.name }
        ingredientsSet.remove(ingredient)
        notifyDataSetChanged()
    }
}
