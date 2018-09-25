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
        val imageBase64: String = "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\n",
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

    fun calculatePercentAvailable(ingredients: List<Ingredient>){
        val recipeIngredients = this.ingredientList
        var count = 0


        for(ingredient in ingredients){
            for(recipeIngredient in recipeIngredients) {
                if (recipeIngredient.contains(ingredient.toString(), ignoreCase = true)) {
                    count++
                }
            }
        }
        this.queueValue = (count * 100) / recipeIngredients.count()
    }

    fun createBitMap(): Bitmap{
        val imageId = Base64.decode(imageBase64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageId, 0, imageId.count())
    }

}