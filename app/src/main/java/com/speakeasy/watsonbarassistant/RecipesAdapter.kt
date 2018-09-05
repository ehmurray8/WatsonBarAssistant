package com.speakeasy.watsonbarassistant

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

class RecipesAdapter(private var dataSet: MutableList<Recipe>):
        RecyclerView.Adapter<RecipesAdapter.ViewHolder>() {

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipesAdapter.ViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.recipe_view,
                parent, false) as TextView
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = dataSet[position].name
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(dataSet[position].imageId, 0, 0, 0)
    }

    override fun getItemCount(): Int = dataSet.count()
}
