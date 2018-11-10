package com.speakeasy.watsonbarassistant.adapter

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.speakeasy.watsonbarassistant.DiscoveryRecipe
import com.speakeasy.watsonbarassistant.MAX_HOME_RECIPES
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.activity.RecipeCollection
import com.speakeasy.watsonbarassistant.activity.RecipeDetail
import com.speakeasy.watsonbarassistant.loadImage


class HorizontalRecipeAdapter(private val recipes: MutableList<DiscoveryRecipe>,
                              private val activity: Activity, private val collectionName: String):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cardView = LayoutInflater.from(parent.context).inflate(R.layout.recipe_card,
                parent, false) as CardView
        return ViewHolder(cardView)
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == MAX_HOME_RECIPES) 1 else 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        if(position == MAX_HOME_RECIPES) {
            val mainLayout = holder.card.getChildAt(0) as RelativeLayout
            val textView = mainLayout.findViewById<TextView>(R.id.recipeCardName)
            textView.text = "See All"
            val imageView = mainLayout.findViewById<SimpleDraweeView>(R.id.home_recipe_card)
            imageView.hierarchy.setPlaceholderImage(R.mipmap.ic_old_fashioned)
            holder.card.setOnClickListener {
                RecipeCollection.recipesList = recipes
                val intent = Intent(activity.baseContext, RecipeCollection::class.java)
                intent.putExtra("Collection Name", collectionName)
                activity.startActivity(intent)
            }
        } else {
            bindRecipeViewHolder(viewHolder, position)
        }
    }

    private fun bindRecipeViewHolder(holder: ViewHolder, position: Int) {
        val mainLayout = holder.card.getChildAt(0) as RelativeLayout
        mainLayout.setOnClickListener {
            val intent = Intent(activity, RecipeDetail::class.java)
            intent.putExtra("Recipe", recipes[position])
            activity.startActivity(intent)
        }
        val recipe = recipes[position]
        val textView = mainLayout.findViewById<TextView>(R.id.recipeCardName)
        setupImageView(mainLayout, recipe, textView)
        textView.text = recipe.title
    }

    private fun setupImageView(mainLayout: RelativeLayout, recipe: DiscoveryRecipe,
                               textView: TextView) {
        val imageView = mainLayout.findViewById<SimpleDraweeView>(R.id.home_recipe_card)
        textView.setTypeface(textView.typeface, Typeface.BOLD)
        loadImage(activity.baseContext, imageView, recipe)
    }

    override fun getItemCount(): Int {
        return when {
            recipes.count() == 0 -> 0
            recipes.count() <= MAX_HOME_RECIPES -> recipes.count()
            else -> MAX_HOME_RECIPES + 1
        }
    }
}
