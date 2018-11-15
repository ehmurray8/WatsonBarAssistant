package com.speakeasy.watsonbarassistant.adapter

import android.app.Activity
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import com.facebook.drawee.view.SimpleDraweeView
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.activity.RecipeDetail
import com.speakeasy.watsonbarassistant.loadImage


class FeedAdapter(private var activity: Activity): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class FeedViewHolder(val layout: ConstraintLayout): RecyclerView.ViewHolder(layout)
    var clicked = false

    private var favoriteIds = listOf<String>()
    get() {
        return synchronized(BarAssistant.favoritesList) {BarAssistant.favoritesList.map { it.imageId }}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.large_recipe_card,
                parent, false) as ConstraintLayout
        return FeedViewHolder(layout)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindFeedViewHolder(holder as FeedViewHolder, position)
    }

    private fun bindFeedViewHolder(holder: FeedViewHolder?, position: Int) {
        if(BarAssistant.feed.count() == 0) {
            val layout = holder?.layout
            val cardView  = layout?.getChildAt(0) as? CardView
            val relativeLayout = cardView?.getChildAt(0) as? ConstraintLayout
            val imageView = relativeLayout?.getChildAt(1) as? SimpleDraweeView
            imageView?.hierarchy?.setPlaceholderImage(R.mipmap.ic_old_fashioned)
        } else {
            val layout = holder?.layout
            val cardView = layout?.getChildAt(0) as? CardView
            val relativeLayout = cardView?.getChildAt(0) as? ConstraintLayout

            val title = relativeLayout?.findViewById(R.id.cardTitle) as? TextView
            val imageView = relativeLayout?.findViewById(R.id.home_recipe_card) as? SimpleDraweeView
            val description = relativeLayout?.findViewById(R.id.recipeCardDescription) as? TextView
            val numberOfFavoritesView = relativeLayout?.findViewById(R.id.numberOfFavorites) as? TextView
            val favoriteButton = relativeLayout?.findViewById(R.id.buttonFavoriteHome) as? ToggleButton

            val element = BarAssistant.feed[position]

            if(favoriteIds.contains(element.recipe.imageId)) {
                favoriteButton?.isChecked = true
            }
            favoriteButton?.setOnClickListener {
                if (favoriteIds.contains(element.recipe.imageId)) {
                    synchronized(BarAssistant.favoritesList) {
                        BarAssistant.favoritesList.removeIf { favorite ->
                            favorite.imageId == element.recipe.imageId
                        }
                    }
                } else {
                    if (!favoriteIds.contains(element.recipe.imageId)) {
                        synchronized(BarAssistant.favoritesList) {
                            BarAssistant.favoritesList.add(element.recipe)
                        }
                    }
                }
            }

            imageView?.setOnClickListener {
                if(clicked) return@setOnClickListener
                clicked = true
                it.postDelayed({
                    clicked = false
                } , 500)
                val intent = Intent(activity, RecipeDetail::class.java)
                intent.putExtra("Recipe", element.recipe)
                activity.startActivity(intent)
            }

            description?.text = element.recipe.title
            if (imageView != null) {
                imageView.hierarchy.setPlaceholderImage(R.mipmap.ic_old_fashioned)
                loadImage(activity.applicationContext, imageView, element.recipe)
            }

            title?.text = element.getDescription()
            val numFavorites = element.recipe.favoriteCount
            numberOfFavoritesView?.text = if(numFavorites > 0) numFavorites.toString() else ""
        }
    }

    override fun getItemCount(): Int {
        val count = BarAssistant.feed.count()
        return if(count > 0) count else 1
    }
}
