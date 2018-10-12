package com.speakeasy.watsonbarassistant

import kotlinx.serialization.Optional
import java.io.Serializable

@kotlinx.serialization.Serializable

data class DiscoveryRecipe(@Optional var percentOfIngredientsOwned: Int = 0,
                           @Optional val title: String = "",
                           @Optional val imageUrl: String = "",
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
                           @Optional val googleBestImgScore: Double = 0.0,
                           @Optional var recipeImageUriString: String = ""): Serializable {


    fun calculatePercentAvailable(userIngredients: Array<Ingredient>){
        var count = 0
        for(recipeIngredient in ingredientList) {
            for(userIngredient in userIngredients) {
                if(recipeIngredient.toLowerCase().contains("[\\s ,.]${userIngredient.name.toLowerCase()}[\\s ,.]".toRegex())) {
                    count++
                    break
                }
            }
        }
        percentOfIngredientsOwned = (count * 100) / (ingredientList.count())
    }

    fun getImageName(): String {
        return "recipe_images/GSBimg-${imageId.toFloat().toInt()}.jpg"
    }

    fun getTags(): List<RecipeTag> {
        val tags = mutableListOf<RecipeTag>()
        if(checkMissing()) tags.add(RecipeTag.MISSING)
        if(checkWhiskeyTag()) tags.add(RecipeTag.WHISKEY)
        if(checkVodkaTag()) tags.add(RecipeTag.VODKA)
        if(checkTequilaTag()) tags.add(RecipeTag.TEQUILA)
        return tags
    }

    private fun checkWhiskeyTag(): Boolean {
        return ingredientList.any {
            it.contains("Whiskey", true) ||
                    it.contains("Whisky", true) ||
                    it.contains("Bourbon", true) ||
                    it.contains("Scotch", true)
        }
    }

    private fun checkVodkaTag(): Boolean {
        return ingredientList.any {
            it.contains("Vodka", true)
        }
    }

    private fun checkTequilaTag(): Boolean {
        return ingredientList.any() {
            it.contains("Tequila", true)
        }
    }

    private fun checkMissing(): Boolean {
        return percentOfIngredientsOwned < 50
    }
}