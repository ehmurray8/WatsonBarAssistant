package com.speakeasy.watsonbarassistant.Recipe

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.loadImage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_recipe_detail.*

class RecipeDetail : AppCompatActivity() {

    private val picasso = Picasso.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)
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
    }
}
