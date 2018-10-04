package com.speakeasy.watsonbarassistant

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_recipe_detail.*

class RecipeDetail : AppCompatActivity() {

    private var recipe: DiscoveryRecipe? = null
    private val picasso = Picasso.get()

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

        description_content.text = recipe?.description

        val imageUri = recipe?.createImageUri(baseContext)
        picasso.load(imageUri).into(drink_detail_image)
    }
}
