package com.speakeasy.watsonbarassistant

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView

class MyRecipeAdapter(private var recipes: MutableList<DiscoveryRecipe>, private val activity: Activity):
        RecyclerView.Adapter<MyRecipeAdapter.ViewHolder>() {

    class ViewHolder(val layout: ConstraintLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRecipeAdapter.ViewHolder {
        val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.my_recipe_view, parent,false) as ConstraintLayout
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val imageView = holder.layout.getChildAt(0) as SimpleDraweeView
        val name = holder.layout.getChildAt(2) as TextView
        val description = holder.layout.getChildAt(1) as TextView

        val recipe = recipes[position]
        loadImage(activity.baseContext, imageView, recipe)

        name.text = recipe.title
        description.text = recipe.description
    }

    override fun getItemCount(): Int = recipes.count()
}
