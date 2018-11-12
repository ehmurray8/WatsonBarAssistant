package com.speakeasy.watsonbarassistant

fun loadFeedRecipes(ingredientsList: List<Ingredient>): MutableList<DiscoveryRecipe> {
    val recipes = mutableListOf<DiscoveryRecipe>()
    if(ingredientsList.count() == 0) {
        recipes.addAll(getRandomRecipes(50))
    }
    return recipes
}

private fun getRandomRecipes(count: Int): MutableList<DiscoveryRecipe> {
    val frequentRecipesNum = (count * .75).toInt()
    val inFrequentRecipesNum = (count * .25).toInt()

    return mutableListOf()
}