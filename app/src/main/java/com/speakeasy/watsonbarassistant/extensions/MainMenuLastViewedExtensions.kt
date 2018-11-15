package com.speakeasy.watsonbarassistant.extensions

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.speakeasy.watsonbarassistant.*
import com.speakeasy.watsonbarassistant.activity.MainMenu
import com.speakeasy.watsonbarassistant.fragment.HomeTab

internal fun loadRecentlyViewedRecipesSharedPreferences(storedLastViewedTimes: Array<Long>) {
    if (storedLastViewedTimes.count() > 0) {
        val lastViewedRecipes = synchronized(BarAssistant.recipes) {
            BarAssistant.recipes[BarAssistant.homeCategories.indexOf(RECENTLY_VIEWED_CATEGORY)]
        }
        if (lastViewedRecipes.count() == storedLastViewedTimes.count()) {
            val lastViewedMap = storedLastViewedTimes.zip(lastViewedRecipes).toMap()
            synchronized(BarAssistant.lastViewedTimes) {
                BarAssistant.lastViewedTimes.clear()
                BarAssistant.lastViewedTimes.addAll(storedLastViewedTimes)
                BarAssistant.lastViewedTimes.sortByDescending { it -> it }
            }
            synchronized(BarAssistant.lastViewedRecipes) {
                BarAssistant.lastViewedRecipes.clear()
                BarAssistant.lastViewedRecipes.putAll(lastViewedMap)
            }
            synchronized(BarAssistant.recipes) {
                BarAssistant.recipes[1].clear()
                BarAssistant.recipes[1] = listOfNotNull(*BarAssistant.lastViewedTimes.asSequence().map { BarAssistant.lastViewedRecipes[it] }.toList().toTypedArray()).toMutableList()
            }
        }
    }
}

internal fun MainMenu.loadRecentlyViewed() {
    if (BarAssistant.isInternetConnected()) {
        val uid = currentUser?.uid
        if (uid != null) {
            fireStore.recentlyViewedDocument(uid).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val document = it.result ?: return@addOnCompleteListener
                    processRecentlyViewed(document)
                }
            }
        }
    }
}


private fun MainMenu.processRecentlyViewed(document: DocumentSnapshot) {
    temporaryLastViewedTimes.clear()
    val lastViewedTimes = document.get(LAST_VIEWED_RECIPE_TIMES) as? ArrayList<*>
    val lastViewedRecipeIds = document.get(LAST_VIEWED_RECIPES) as? ArrayList<*>
    lastViewedTimes?.forEach { time ->
        val timeLong = time as? Long
        timeLong?.let { it1 -> temporaryLastViewedTimes.add(it1) }
    }
    temporaryLastViewedRecipes.clear()
    lastViewedRecipeIds?.forEach { _ -> temporaryLastViewedRecipes.add(null) }
    var count = 0
    lastViewedRecipeIds?.forEachIndexed { index, recipeId ->
        if (recipeId != null) {
            fireStore.recipeDocument(recipeId.toString()).get().addOnCompleteListener { snapShotRecipe ->
                if (snapShotRecipe.isSuccessful) {
                    val recipeDocument = snapShotRecipe.result ?: return@addOnCompleteListener
                    addLastViewedRecipe(recipeDocument, index, ++count)
                }
            }
        }
    }
}

private fun MainMenu.addLastViewedRecipe(recipeDocument: DocumentSnapshot, index: Int, count: Int) {
    val recipe = recipeDocument.toObject(FireStoreRecipe::class.java)
    temporaryLastViewedRecipes[index] = recipe?.toDiscoveryRecipe()
    if (count == temporaryLastViewedRecipes.count()) {
        val temporaryMap = temporaryLastViewedTimes.zip(listOfNotNull(*temporaryLastViewedRecipes.toTypedArray())).toMap()
        BarAssistant.lastViewedRecipes.putAll(temporaryMap)
        val goodKeys = mutableSetOf<Long>()
        synchronized(BarAssistant.lastViewedRecipes) {
            synchronized(BarAssistant.lastViewedTimes) {
                BarAssistant.lastViewedRecipes.keys.sortedByDescending { it -> it }.forEach {
                    val numberOfKeyDuplicates = BarAssistant.lastViewedRecipes.values.count { recipe -> BarAssistant.lastViewedRecipes[it] == recipe }
                    if (numberOfKeyDuplicates == 1) goodKeys.add(it)
                    else goodKeys.add(BarAssistant.lastViewedRecipes.keys.asSequence().sortedByDescending { key -> key }.first { key -> BarAssistant.lastViewedRecipes[key] == BarAssistant.lastViewedRecipes[it] })
                }
                BarAssistant.lastViewedTimes.clear()
                BarAssistant.lastViewedTimes.addAll(goodKeys)
                val lastViewedRecipes = BarAssistant.lastViewedRecipes.filter { goodKeys.contains(it.key) }
                BarAssistant.lastViewedRecipes.clear()
                BarAssistant.lastViewedRecipes.putAll(lastViewedRecipes)
                synchronized(BarAssistant.recipes) {
                    BarAssistant.recipes[1].clear()
                    BarAssistant.lastViewedTimes.sortedByDescending { it -> it }.forEach {
                        val sortedRecipe = BarAssistant.lastViewedRecipes[it]
                        synchronized(BarAssistant.recipes) {
                            if (sortedRecipe != null) BarAssistant.recipes[1].add(sortedRecipe)
                        }
                    }
                    (fragment as? HomeTab)?.refresh()
                }
            }
        }
    }
}
fun addIngredient(name: String = "", newIngredient: Ingredient? = null, context: Context, refreshDiscovery: Boolean = false) {
    val ingredient = if(name != "") {
        Ingredient(name)
    } else {
        newIngredient ?: return
    }
    synchronized(BarAssistant.ingredients) {
        if (BarAssistant.ingredients.any { it.name.toLowerCase() == ingredient.name.toLowerCase() }) {
            context.toast("${ingredient.name} is already stored as an ingredient.")
        } else {
            BarAssistant.ingredients.add(ingredient)
            // refreshDiscovery(refreshDiscovery)
        }
    }
}
