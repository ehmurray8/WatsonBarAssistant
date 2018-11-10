package com.speakeasy.watsonbarassistant.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.speakeasy.watsonbarassistant.Ingredient
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.loadIngredientImage
import java.util.*

class IngredientAdapter(private val ingredientsSet: TreeSet<Ingredient>, private val context: Context):
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
        val ingredient = ingredientsList[position]
        val textView = holder.layout.getChildAt(1) as? TextView
        textView?.text = ingredient.name.capitalize()

        val imageView = holder.layout.getChildAt(0) as? SimpleDraweeView
        imageView?.let {
            loadIngredientImage(context, it, ingredient)
        }
    }

    override fun getItemCount(): Int = ingredientsList.count()

    fun removeAt(position: Int) {
        val ingredient = ingredientsList[position]
        ingredientsSet.removeIf { it.name == ingredient.name }
        ingredientsSet.remove(ingredient)
        notifyDataSetChanged()
    }
}
