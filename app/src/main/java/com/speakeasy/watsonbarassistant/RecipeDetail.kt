package com.speakeasy.watsonbarassistant

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.speakeasy.watsonbarassistant.R.layout.activity_recipe_detail
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_recipe_detail.*
import kotlinx.android.synthetic.main.activity_recipe_detail.view.*

class RecipeDetail : AppCompatActivity() {

    private val picasso = Picasso.get()
    private var favorited: Boolean = false

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
        if(button_favorite.isActivated)
        {
            Toast.makeText(baseContext, "REEEEEE", Toast.LENGTH_SHORT).show()
        }
        button_favorite.setOnClickListener{
            if (!favorited){
                favorited = true

            }
            else{
                favorited = false

            }
            // Wait ~15 (?) seconds before putting on to firebase favorites list
        }

        /* Or wait until page is navigated away from/ app is closed (???) */
    }
}
