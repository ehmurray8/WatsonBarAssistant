package com.speakeasy.watsonbarassistant.adapter

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.activity.RecipeCollection
import com.speakeasy.watsonbarassistant.fragment.HomeTab


class HomeAdapter(private var activity: Activity):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class RecipeViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val linearLayout = LayoutInflater.from(parent.context).inflate(R.layout.home_recipe_category,
                parent, false) as LinearLayout
        return RecipeViewHolder(linearLayout)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindRecipeViewHolder(holder as RecipeViewHolder, position)
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

    override fun getItemCount(): Int {
        synchronized(BarAssistant.recipes) {
            return BarAssistant.recipes.count()
        }
    }
}
