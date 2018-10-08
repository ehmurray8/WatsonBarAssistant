package com.speakeasy.watsonbarassistant

import kotlinx.serialization.Optional
import java.io.Serializable

@kotlinx.serialization.Serializable

data class DiscoveryRecipe(@Optional var queueValue: Int = 0,
                           @Optional val title: String = "",
                           @Optional val imageUri: String = "",
                           @Optional val reviewCount: String = "",
                           @Optional val description: String = "",
                           @Optional val recipeUrl: String = "",
                           @Optional val ingredientList: List<String> = emptyList(),
                           @Optional val instructionList: List<String> = emptyList(),
                           @Optional val prepTime: String = "",
                           @Optional val cookTime: String = "",
                           @Optional val totalTime: String = "",
                           @Optional val imageId: String = "",
                           @Optional val googleBestImgUrl: String = "",
                           @Optional val googleBestImgScore: Double = 0.0): Serializable {

    fun calculatePercentAvailable(userIngredients: Array<Ingredient>){

        var count = 0
        for(recipeIngredient in ingredientList) {
            for(userIngredient in userIngredients) {
                if(recipeIngredient.contains(userIngredient.name, ignoreCase = true)) {
                    count++
                    break
                }
            }
        }
        queueValue = (count * 100) / (ingredientList.count())
    }

    fun getImageName(): String {
        return "recipe_images/GSBimg-${imageId.toFloat().toInt()}.jpg"
    }
}