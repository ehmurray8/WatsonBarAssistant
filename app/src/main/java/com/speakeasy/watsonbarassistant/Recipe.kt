package com.speakeasy.watsonbarassistant

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.serialization.Optional
import java.io.Serializable

@kotlinx.serialization.Serializable
data class DiscoveryRecipe(
        @Optional
        var queueValue: Int = 0,
        @Optional
        var imageBase64: String = DEFAULT_IMAGE_BASE64,
        @Optional
        val description: String = "",
        @Optional
        val instructionList: List<String> = emptyList(),
        @Optional
        val prepTime: String = "",
        @Optional
        val cookTime: String = "",
        @Optional
        val totalTime: String = "",
        val title: String = "",
        val ingredientList: List<String> = emptyList()
): Serializable{

    companion object {
        fun newInstance(): DiscoveryRecipe = DiscoveryRecipe()
    }

    fun calculatePercentAvailable(ingredients: List<Ingredient>){
        val recipeIngredients = this.ingredientList
        var count = 0

        for(ingredient in ingredients){
            for(recipeIngredient in recipeIngredients) {
                if (recipeIngredient.contains(ingredient.toString(), ignoreCase=true)) {
                    count++
                }
            }
        }
        this.queueValue = (count * 100) / recipeIngredients.count()
    }

    fun createBitMap(): Bitmap {
        if(imageBase64 == DEFAULT_IMAGE_BASE64) {
            val image = Login.assetManager?.open(DEFAULT_RECIPE_IMAGE_NAME)
            if(image != null) {
                return BitmapFactory.decodeStream(image)
            }
        }
        val imageId = Base64.decode(imageBase64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageId, 0, imageId.count())
    }
}