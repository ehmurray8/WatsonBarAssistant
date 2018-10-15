package com.speakeasy.watsonbarassistant

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.speakeasy.watsonbarassistant.R.layout.activity_recipe_detail
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_recipe_detail.*
import kotlinx.android.synthetic.main.activity_recipe_detail.view.*

class RecipeDetail : AppCompatActivity() {

    private val picasso = Picasso.get()

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

        /* NOT favorited previously */
        favorite_not_pressed.setOnClickListener{
            favorite_not_pressed.hide()
            favorite_pressed.show()
            // Wait ~15 (?) seconds before putting on to firebase favorites list

            favorite_pressed.setOnClickListener{
                favorite_pressed.hide()
                favorite_not_pressed.show()
                // Wait ~15 (?) seconds before deleting from firebase favorites list
            }
        }

        /* Favorited previously */
        favorite_pressed.setOnClickListener{
            favorite_pressed.hide()
            favorite_not_pressed.show()
            // Wait ~15 (?) seconds before deleting from firebase favorites list

            favorite_not_pressed.setOnClickListener{
                favorite_not_pressed.hide()
                favorite_pressed.show()
                // Wait ~15 (?) seconds before putting on to firebase favorites list
            }
        }

        /* Or wait until page is navigated away from/ app is closed (???) */
    }
}
