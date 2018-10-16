package com.speakeasy.watsonbarassistant

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.speakeasy.watsonbarassistant.DiscoveryRecipe
import com.speakeasy.watsonbarassistant.RecipeCollection


class HomeAdapter(private var activity: Activity):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class RecipeViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    class ButtonViewHolder(val layout: LinearLayout): RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 0) {
            val layout = LayoutInflater.from(parent.context).inflate(R.layout.home_buttons_view,
                    parent, false) as LinearLayout
            ButtonViewHolder(layout)
        } else {
            val linearLayout = LayoutInflater.from(parent.context).inflate(R.layout.home_recipe_category,
                    parent, false) as LinearLayout
            RecipeViewHolder(linearLayout)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == 0) 0 else 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(position == 0) {
            bindButtonViewHolder(holder as ButtonViewHolder)
        } else {
            bindRecipeViewHolder(holder as RecipeViewHolder, position-1)
        }
    }

    private fun bindButtonViewHolder(holder: ButtonViewHolder) {
        val layout = holder.layout
        val shoppingCartButton = layout.getChildAt(0)
        shoppingCartButton.setOnClickListener {
            val intent = Intent(activity, ShoppingCart::class.java)
            activity.startActivity(intent)
        }
        val ingredientsButton = layout.getChildAt(1)
        ingredientsButton.setOnClickListener {
            Log.d("TODO", "Create Ingredients View.")
        }
        val addView = layout.getChildAt(2)
        addView.setOnClickListener {
            Log.d("TODO", "Create Add View.")
        }
    }

    private fun bindRecipeViewHolder(recipeHolder: RecipeViewHolder, position: Int) {
        val layout = recipeHolder.layout
        val headerView = layout.getChildAt(0) as Button
        val category = BarAssistant.homeCategories[position]
        headerView.text = activity.resources.getString(R.string.collection_name, category)
        val recipes = BarAssistant.recipes[position]
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

    override fun getItemCount(): Int = BarAssistant.recipes.count() + 1
}
