package com.speakeasy.watsonbarassistant

import kotlinx.serialization.Optional
import java.io.Serializable

@kotlinx.serialization.Serializable
data class FireStoreRecipe(val title: String = "", val imageUrl: String = "", val reviewCount: Long = 0,
                           val description: String = "", val recipeUrl: String = "", val ingredientList: List<String> = emptyList(),
                           val instructionList: List<String> = emptyList(), val prepTime: String = "", val cookTime: String = "",
                           val totalTime: String = "", val imageId: Long = -1, val googleBestImgUrl: String = "",
                           val googleBestImgScore: Double = 0.0, val objectId: Long = -1, var favoriteCount: Int = 0) {


    fun toDiscoveryRecipe(): DiscoveryRecipe {
        return DiscoveryRecipe(title = title, imageUrl = imageUrl, reviewCount = reviewCount.toString(),
                description = description, recipeUrl = recipeUrl, ingredientList = ingredientList,
                instructionList = instructionList, prepTime = prepTime, cookTime = cookTime,
                totalTime = totalTime, imageId = imageId.toString(), googleBestImgUrl = googleBestImgUrl,
                googleBestImgScore = googleBestImgScore, favoriteCount = favoriteCount)
    }
}

@kotlinx.serialization.Serializable
data class DiscoveryRecipe(@Optional val title: String = "",
                           @Optional val imageUrl: String = "",
                           @Optional val reviewCount: String = "0",
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
                           @Optional val favoriteCount: Int = 0): Serializable, Comparable<DiscoveryRecipe> {

    @Optional var percentOfIngredientsOwned: Int = 0
    @Optional var recipeImageUriString: String = ""

    fun toFireStoreRecipe(): FireStoreRecipe {
        return FireStoreRecipe(title = title, imageUrl = imageUrl, reviewCount = reviewCount.toFloat().toLong(),
                description = description, recipeUrl = recipeUrl, ingredientList = ingredientList,
                instructionList = instructionList, prepTime = prepTime, cookTime = cookTime,
                totalTime = totalTime, imageId = imageId.toLong(), googleBestImgUrl = googleBestImgUrl,
                googleBestImgScore = googleBestImgScore, favoriteCount = favoriteCount, objectId = imageId.toLong())
    }

    override fun equals(other: Any?): Boolean {
        if(other as? DiscoveryRecipe != null) {
            return other.imageId == imageId
        }
        return false
    }

    override fun hashCode(): Int {
        return imageId.hashCode()
    }

    override fun compareTo(other: DiscoveryRecipe): Int {
        return title.compareTo(other.title)
    }

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
        var id = imageId
        if (id.contains(".")){
            id = id.dropLast(2)
        }
        return "recipe_images/GSBimg-${id.toLong()}.jpg"
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
        return ingredientList.any {
            it.contains("Tequila", true)
        }
    }

    private fun checkMissing(): Boolean {
        return percentOfIngredientsOwned < 50
    }
}