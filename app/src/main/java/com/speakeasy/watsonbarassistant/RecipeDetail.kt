package com.speakeasy.watsonbarassistant

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.favorites
import com.speakeasy.watsonbarassistant.R.id.tagContainerDetail
import kotlinx.android.synthetic.main.activity_recipe_detail.*
import java.util.ArrayList

class RecipeDetail : AppCompatActivity() {

    private var authorization = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()
    private var favorited: Boolean = false
    private var unfavorited: Boolean = false
    private lateinit var favoriteAnim: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)
        val recipe = intent.getSerializableExtra("Recipe") as? DiscoveryRecipe

        /* Set transparency */
        button_favorite.alpha = 0.35.toFloat()

        if(recipe != null) {
            addToRecentlyViewed(recipe)
            val barAssistant = application as BarAssistant
            barAssistant.loadFavoritesFromFireStore(authorization, fireStore)

            val recipeTitle = findViewById<TextView>(R.id.recipe_title)
            recipeTitle.text = recipe.title

            var recipeIngredientsString = ""
            recipe.ingredientList.forEachIndexed { i, element ->
                recipeIngredientsString += "${i + 1}. $element\n"
            }
            recipe_ingredients.text = recipeIngredientsString
            description_content.text = recipe.description

            loadImage(baseContext, drink_detail_image, recipe)
            addTags(recipe)

            favoriteAnim = AnimationUtils.loadAnimation(baseContext, R.anim.anim_favorite)

            /* Check if already favorited */
            BarAssistant.favorites.forEachIndexed { index, _ ->

                if (BarAssistant.favorites[index].equals(recipe.imageId)){
                    /* Already on favorites list */
                    favorited = true
                    button_favorite.startAnimation(favoriteAnim)
                    button_favorite.isChecked = true
                }
            }
        }


        button_favorite.setOnClickListener {

            if(favorited){
                favorited=false
                unfavorited = true
                Toast.makeText(baseContext, "Un-favorited ${recipe?.title}.", Toast.LENGTH_SHORT).show()
            }
            else{
                favorited=true
                unfavorited = false
                button_favorite.startAnimation(favoriteAnim)
                Toast.makeText(baseContext, "Favorited ${recipe?.title}.", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun addToRecentlyViewed(recipe: DiscoveryRecipe) {
        val currentTime = System.currentTimeMillis()
        if(BarAssistant.lastViewedRecipes.values.map { it.toFireStoreRecipe() }.contains(recipe.toFireStoreRecipe())) {
            val keysToRemove = BarAssistant.lastViewedRecipes.filter { it.value.toFireStoreRecipe() == recipe.toFireStoreRecipe() }.map { it.key }
            keysToRemove.forEach {
                BarAssistant.lastViewedRecipes.remove(it)
                BarAssistant.lastViewedTimes.remove(it)
            }
        }
        BarAssistant.lastViewedTimes.add(currentTime)
        BarAssistant.lastViewedRecipes[currentTime] = recipe

        BarAssistant.lastViewedTimes.sortByDescending { it }
        if (BarAssistant.lastViewedTimes.count() > MAX_LAST_VIEWED) {
            BarAssistant.lastViewedTimes.removeAt(BarAssistant.lastViewedTimes.count() - 1)
        }
        BarAssistant.recipes[1].clear()
        BarAssistant.recipes[1].addAll(listOfNotNull(*BarAssistant.lastViewedTimes
                .map { BarAssistant.lastViewedRecipes[it] }.toTypedArray()))
    }

    private fun addTags(recipe: DiscoveryRecipe) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        tagContainerDetail.removeAllViews()
        var tags = recipe.getTags()
        if(tags.count() > 4) tags = recipe.getTags().subList(0, 4)
        tags.forEach {
            val tag = inflater.inflate(R.layout.recipe_tag, tagContainerDetail, false) as TextView
            val drawable = tag.background as GradientDrawable
            drawable.setColor(it.getColor())
            tag.text = it.title
            tagContainerDetail.addView(tag)
        }
    }

    private fun saveFavoriteToFireStore(recipe: DiscoveryRecipe) {
        val uid = authorization.currentUser?.uid

        val recipeId = recipe.imageId

        if (uid != null && !favorites.contains(recipeId)) {
            BarAssistant.favorites.add(recipeId)
        }
    }

    private fun removeFavoriteFromFireStore(recipe: DiscoveryRecipe) {
        val uid = authorization.currentUser?.uid
        val recipeId = recipe.imageId

        if (uid != null && favorites.contains(recipeId)) {
            BarAssistant.favorites.remove(recipeId)
        }
    }

    override fun onPause() {
        super.onPause()
        val barAssistant = application as BarAssistant
        barAssistant.storeRecentlyViewed(authorization, fireStore)
        val recipe = intent.getSerializableExtra("Recipe") as DiscoveryRecipe
        if(favorited && !unfavorited) {
            saveFavoriteToFireStore(recipe)
            barAssistant.updateFavoriteFireStore(authorization, fireStore)
        }
        else if(!favorited && unfavorited){
            removeFavoriteFromFireStore(recipe)
            barAssistant.updateFavoriteFireStore(authorization, fireStore)
        }
    }
}


