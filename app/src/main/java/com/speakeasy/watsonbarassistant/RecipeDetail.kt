package com.speakeasy.watsonbarassistant

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView

class RecipeDetail : AppCompatActivity() {

    var recipe: Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)
        recipe = intent.getSerializableExtra("Recipe") as? Recipe

        val recipeTitle = findViewById<TextView>(R.id.recipe_title)
        recipeTitle.text = recipe?.name

        val recipeIngredientsView = findViewById<TextView>(R.id.recipe_ingredients)
        var recipeIngredintsString = ""
        recipe?.ingredients?.forEachIndexed { i, element ->
            recipeIngredintsString += "${i+1}. $element\n"
        }
        recipeIngredientsView.text = recipeIngredintsString
    }
}
