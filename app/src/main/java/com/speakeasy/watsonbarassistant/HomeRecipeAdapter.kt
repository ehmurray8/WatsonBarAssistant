package com.speakeasy.watsonbarassistant

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
<<<<<<< HEAD
import com.speakeasy.watsonbarassistant.Recipe.DiscoveryRecipe
import com.speakeasy.watsonbarassistant.Recipe.RecipeCollection
import com.speakeasy.watsonbarassistant.Recipe.RecipeDetail
import com.squareup.picasso.Picasso
=======
import com.facebook.drawee.view.SimpleDraweeView
>>>>>>> master

const val MAX_RECIPES = 15

class HomeRecipeAdapter(private val recipes: MutableList<DiscoveryRecipe>,
                        private val activity: Activity, private val collectionName: String):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card)

    class ViewHolderSeeAll(val card: CardView) : RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 0) {
            val cardView = LayoutInflater.from(parent.context).inflate(R.layout.recipe_card,
                    parent, false) as CardView
            ViewHolder(cardView)
        } else {
            val cardView = LayoutInflater.from(parent.context).inflate(R.layout.see_all_card,
                    parent, false) as CardView
            ViewHolderSeeAll(cardView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == getLastPosition()) 1 else 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(position == getLastPosition()) {
            if(holder as? ViewHolderSeeAll != null) {
                holder.card.setOnClickListener {
                    RecipeCollection.recipesList = recipes
                    val intent = Intent(activity.baseContext, RecipeCollection::class.java)
                    intent.putExtra("Collection Name", collectionName)
                    activity.startActivity(intent)
                }
            }
        } else {
            bindRecipeViewHolder(holder as ViewHolder, position)
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
        val textView = mainLayout.getChildAt(1) as TextView
        setupImageView(mainLayout, recipe, textView)
        textView.text = recipe.title
    }

    private fun setupImageView(mainLayout: RelativeLayout, recipe: DiscoveryRecipe,
                               textView: TextView) {
        val imageView = mainLayout.getChildAt(0) as SimpleDraweeView
        textView.setTypeface(textView.typeface, Typeface.BOLD)
        loadImage(activity.baseContext, imageView, recipe)
    }

    override fun getItemCount(): Int {
        return when {
            recipes.count() == 0 -> 0
            recipes.count() <= MAX_RECIPES -> recipes.count() + 1
            else -> MAX_RECIPES + 1
        }
    }

    private fun getLastPosition(): Int {
        if(recipes.count() <= MAX_RECIPES) {
            return recipes.count()
        }
        return MAX_RECIPES
    }
}
