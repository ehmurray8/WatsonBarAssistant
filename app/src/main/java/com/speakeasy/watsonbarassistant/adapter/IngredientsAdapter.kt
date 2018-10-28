package com.speakeasy.watsonbarassistant.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.speakeasy.watsonbarassistant.Ingredient
import com.speakeasy.watsonbarassistant.R
import java.util.*

class IngredientsAdapter(private val ingredientsSet: TreeSet<Ingredient>):
        RecyclerView.Adapter<IngredientsAdapter.ViewHolder>() {

    private val ingredientsList: MutableList<Ingredient>
    get() {
        return ingredientsSet.toMutableList()
    }

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.ingredient_view,
                parent, false) as TextView
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = ingredientsList[position].name.capitalize()
    }

    override fun getItemCount(): Int = ingredientsList.count()

    fun removeAt(position: Int) {
        val ingredient = ingredientsList[position]
        ingredientsSet.removeIf { it.name == ingredient.name }
        ingredientsSet.remove(ingredient)
        notifyDataSetChanged()
    }
}
