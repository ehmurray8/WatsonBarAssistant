package com.speakeasy.watsonbarassistant

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_recipe_detail.*

class RecipeDetail : AppCompatActivity() {

    private var recipe: DiscoveryRecipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)
        recipe = intent.getSerializableExtra("Recipe") as? DiscoveryRecipe

        val recipeTitle = findViewById<TextView>(R.id.recipe_title)
        recipeTitle.text = recipe?.title

        var recipeIngredientsString = ""
        recipe?.ingredientList?.forEachIndexed { i, element ->
            recipeIngredientsString += "${i+1}. $element\n"
        }
        recipe_ingredients.text = recipeIngredientsString

        var imageBitmap = recipe?.createBitMap()
        if(recipe?.imageBase64 == DEFAULT_IMAGE_BASE64) {
            imageBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_alcohol)
        }
        drink_detail_image.setImageBitmap(imageBitmap)
    }
}
