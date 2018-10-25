
package com.speakeasy.watsonbarassistant

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.speech.HandleTtS
import com.speakeasy.watsonbarassistant.speech.TextToSpeech
import kotlinx.android.synthetic.main.activity_recipe_detail.*

class RecipeDetail : AppCompatActivity() {

    private var authorization = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()
    private var mediaPlayer: MediaPlayer? = null
    private var favorited = false

    private var favoriteIds = listOf<String>()
    get() {
        return BarAssistant.favoritesList.map { it.imageId }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)
        val recipe = intent.getSerializableExtra("Recipe") as? DiscoveryRecipe

        mediaPlayer = MediaPlayer()
        button_favorite.alpha = 0.35.toFloat()

        if(recipe != null) {
            addToRecentlyViewed(recipe)

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

            val favoriteAnim = AnimationUtils.loadAnimation(baseContext, R.anim.anim_favorite)

            if(favoriteIds.contains(recipe.imageId)) {
                favorited = true
                button_favorite.startAnimation(favoriteAnim)
                button_favorite.isChecked = true
            }

            readDescriptionButton.setOnClickListener {
                val textToSpeech = TextToSpeech(HandleTtS(), mediaPlayer ?: return@setOnClickListener)
                textToSpeech.execute(recipe.title + ". " + recipe.description)
            }

            button_favorite.setOnClickListener {
                if(favorited) {
                    favorited=false
                    BarAssistant.favoritesList.removeIf { favorite ->
                        favorite.imageId == recipe.imageId
                    }
                    Toast.makeText(baseContext, "Unfavorited ${recipe.title}.", Toast.LENGTH_SHORT).show()
                } else {
                    favorited=true
                    button_favorite.startAnimation(favoriteAnim)
                    if(!favoriteIds.contains(recipe.imageId)) {
                        BarAssistant.favoritesList.add(recipe)
                    }
                    Toast.makeText(baseContext, "Favorited ${recipe.title}.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

     @Synchronized private fun addToRecentlyViewed(recipe: DiscoveryRecipe) {
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

    override fun onPause() {
        super.onPause()
        mediaPlayer?.release()
        mediaPlayer = null
        val barAssistant = application as? BarAssistant
        barAssistant?.updateFavoriteFireStore(authorization, fireStore)
        barAssistant?.storeRecentlyViewedAndFavorites(authorization, fireStore)
    }
}
