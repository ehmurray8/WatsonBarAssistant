package com.speakeasy.watsonbarassistant.Recipe

import android.content.res.AssetManager
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.loadImage
import com.squareup.picasso.Picasso

class MyRecipeAdapter(private var recipes: MutableList<DiscoveryRecipe>, private val assets: AssetManager):
        RecyclerView.Adapter<MyRecipeAdapter.ViewHolder>() {

        class ViewHolder(val layout: ConstraintLayout) : RecyclerView.ViewHolder(layout)

    private val picasso = Picasso.get()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.my_recipe_view, parent,false) as ConstraintLayout
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val imageView = holder.layout.getChildAt(0) as ImageView
        val name = holder.layout.getChildAt(2) as TextView
        val description = holder.layout.getChildAt(1) as TextView

        val recipe = recipes[position]
        loadImage(assets, imageView, recipe, picasso)

        name.text = recipe.title
        description.text = recipe.description
    }

    override fun getItemCount(): Int = recipes.count()
}
