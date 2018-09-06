package com.speakeasy.watsonbarassistant

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView

class RecipeDetail : AppCompatActivity() {

    private var recipe: Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)
        recipe = intent.getSerializableExtra("Recipe") as? Recipe

        val recipeTitle = findViewById<TextView>(R.id.recipe_title)
        recipeTitle.text = recipe?.name

        val recipeIngredientsView = findViewById<TextView>(R.id.recipe_ingredients)
        var recipeIngredientsString = ""
        recipe?.ingredients?.forEachIndexed { i, element ->
            recipeIngredientsString += "${i+1}. $element\n"
        }
        recipeIngredientsView.text = recipeIngredientsString

        val drinkDetailImage = findViewById<ImageView>(R.id.drink_detail_image)
        val imageId = recipe?.imageId ?: return
        val drinkBitmap = BitmapFactory.decodeResource(resources, imageId)
        drinkDetailImage.setImageBitmap(drinkBitmap)
    }
}
