package com.speakeasy.watsonbarassistant.adapter

import android.app.Activity
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.activity.RecipeCollection
import com.speakeasy.watsonbarassistant.activity.RecipeDetail
import com.speakeasy.watsonbarassistant.fragment.HomeTab
import com.speakeasy.watsonbarassistant.loadImage


class HomeAdapter(private var activity: Activity):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class RecipeViewHolder(val layout: LinearLayout): RecyclerView.ViewHolder(layout)
    class FeedViewHolder(val layout: ConstraintLayout): RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 0) {
            val linearLayout = LayoutInflater.from(parent.context).inflate(R.layout.home_recipe_category,
                    parent, false) as LinearLayout
            RecipeViewHolder(linearLayout)
        } else {
            val layout = LayoutInflater.from(parent.context).inflate(R.layout.large_recipe_card,
                    parent, false) as ConstraintLayout
            FeedViewHolder(layout)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.itemViewType == 0) {
            bindRecipeViewHolder(holder as RecipeViewHolder, position)
        } else {
            bindFeedViewHolder(holder as FeedViewHolder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        synchronized(BarAssistant.recipes) {
            return if(position <= BarAssistant.recipes.count() - 1) 0 else 1
        }
    }

    private fun bindRecipeViewHolder(recipeHolder: RecipeViewHolder, position: Int) {
        val layout = recipeHolder.layout
        val headerView = layout.getChildAt(0) as Button
        val category = BarAssistant.homeCategories[position]
        headerView.text = activity.resources.getString(R.string.collection_name, category)
        val recipes= synchronized(BarAssistant.recipes) { BarAssistant.recipes[position] }
        headerView.setOnClickListener {
            val intent = Intent(activity.applicationContext, RecipeCollection::class.java)
            intent.putExtra("Collection Name", category)
            RecipeCollection.recipesList = recipes
            activity.startActivity(intent)
        }
        val recipesList = layout.getChildAt(1) as RecyclerView

        val viewManager = LinearLayoutManager(activity.applicationContext,
                LinearLayoutManager.HORIZONTAL, false)

        HomeTab.homeScrollManagers[position] = viewManager
        val viewAdapter = HomeRecipeAdapter(recipes, activity, category)

        recipesList.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        viewManager.scrollToPosition(HomeTab.homeScrollPositions[category] ?: 0)
    }

    private fun bindFeedViewHolder(holder: FeedViewHolder?, position: Int) {
        val pos = synchronized(BarAssistant.recipes) {
            position - BarAssistant.recipes.count()
        }
        val layout = holder?.layout
        val cardView  = layout?.getChildAt(0) as? CardView
        val relativeLayout = cardView?.getChildAt(0) as? ConstraintLayout

        val title = relativeLayout?.getChildAt(0) as? TextView
        val imageView = relativeLayout?.getChildAt(1) as? SimpleDraweeView
        val description = relativeLayout?.getChildAt(2) as? TextView

        val element = synchronized(BarAssistant.feed) {
            BarAssistant.feed[pos]
        }

        cardView?.setOnClickListener {
            val intent = Intent(activity, RecipeDetail::class.java)
            intent.putExtra("Recipe", element.recipe)
            activity.startActivity(intent)
        }

        title?.text = element.recipe.title
        if(imageView != null) {
            loadImage(activity.applicationContext, imageView, element.recipe)
        }

        description?.text = "Test"
    }

    override fun getItemCount(): Int {
        synchronized(BarAssistant.recipes) {
            synchronized(BarAssistant.feed) {
                return BarAssistant.recipes.count() + BarAssistant.feed.count()
            }
        }
    }
}
