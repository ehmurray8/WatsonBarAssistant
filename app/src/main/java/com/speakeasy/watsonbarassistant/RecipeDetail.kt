package com.speakeasy.watsonbarassistant

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.TextView
import android.widget.Toast
import com.speakeasy.watsonbarassistant.R.layout.activity_recipe_detail
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_recipe_detail.*
import kotlinx.android.synthetic.main.activity_recipe_detail.view.*

class RecipeDetail : AppCompatActivity() {

    private val picasso = Picasso.get()
    private var favorited: Boolean = false
    private lateinit var favoriteAnim: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        activity_recipe_detail.apply{

        }

        val recipe = intent.getSerializableExtra("Recipe") as? DiscoveryRecipe

        val recipeTitle = findViewById<TextView>(R.id.recipe_title)
        recipeTitle.text = recipe?.title

        var recipeIngredientsString = ""
        recipe?.ingredientList?.forEachIndexed { i, element ->
            recipeIngredientsString += "${i+1}. $element\n"
        }
        recipe_ingredients.text = recipeIngredientsString
        description_content.text = recipe?.description

        loadImage(assets, drink_detail_image, recipe, picasso)


        /* Set favorite button after checking if recipe is favorited or not */

        favoriteAnim = AnimationUtils.loadAnimation(baseContext, R.anim.anim_favorite)

        /* NOT favorited previously */

        button_favorite.setOnClickListener{
            if (!favorited){
                favorited = true
                button_favorite.startAnimation(favoriteAnim)
                Toast.makeText(baseContext, "Added to Favorites", Toast.LENGTH_SHORT).show()

            }
            else{
                favorited = false
                Toast.makeText(baseContext, "Removed from Favorites", Toast.LENGTH_SHORT).show()

            }
            // Wait ~15 (?) seconds before putting on to firebase favorites list
        }

        /* Or wait until page is navigated away from/ app is closed (???) */
    }


}
