package com.speakeasy.watsonbarassistant

import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.favorites

class MyFavoritesAdapter(private val favoritesList: MutableList<DiscoveryRecipe?>, private val activity: Activity):
        RecyclerView.Adapter<MyFavoritesAdapter.ViewHolder>() {

    class ViewHolder(val layout: ConstraintLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyFavoritesAdapter.ViewHolder {
        val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.my_recipe_view, parent,false) as ConstraintLayout
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val imageView = holder.layout.getChildAt(0) as SimpleDraweeView
        val name = holder.layout.getChildAt(2) as TextView
        val description = holder.layout.getChildAt(1) as TextView
        //val tagContainer = holder.layout.getChildAt(3) as LinearLayout
        val favorite = favoritesList[position]

        //addTags(tagContainer, favorite!!)

        loadImage(activity.baseContext, imageView, favorite)

        name.text = favorite?.title
        description.text = favorite?.description
    }

    private fun addTags(tagContainer: LinearLayout, recipe: DiscoveryRecipe) {
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        tagContainer.removeAllViews()
        var tags = recipe.getTags()
        if(tags.count() > 2) tags = recipe.getTags().subList(0, 2)
        tags.forEach {
            val tag = inflater.inflate(R.layout.recipe_tag, tagContainer, false) as TextView
            val drawable = tag.background as GradientDrawable
            drawable.setColor(it.getColor())
            tag.text = it.title
            tagContainer.addView(tag)
        }
    }

    override fun getItemCount(): Int = favoritesList.count()
}
