package com.speakeasy.watsonbarassistant

import com.google.firebase.firestore.DocumentSnapshot

internal fun loadRecentlyViewedRecipesSharedPreferences(storedLastViewedTimes: Array<Long>) {
    if (storedLastViewedTimes.count() > 0) {
        val lastViewedRecipes =
                BarAssistant.recipes[BarAssistant.homeCategories.indexOf(RECENTLY_VIEWED_CATEGORY)]
        if (lastViewedRecipes.count() == storedLastViewedTimes.count()) {
            val lastViewedMap = storedLastViewedTimes.zip(lastViewedRecipes).toMap()
            BarAssistant.lastViewedTimes.clear()
            BarAssistant.lastViewedRecipes.clear()
            BarAssistant.lastViewedTimes.addAll(storedLastViewedTimes)
            BarAssistant.lastViewedTimes.sortByDescending { it -> it }
            BarAssistant.lastViewedRecipes.putAll(lastViewedMap)
            BarAssistant.recipes[1].clear()
            BarAssistant.recipes[1] = listOfNotNull(*BarAssistant.lastViewedTimes.asSequence().map { BarAssistant.lastViewedRecipes[it] }.toList().toTypedArray()).toMutableList()
        }
    }
}

internal fun MainMenu.loadRecentlyViewed() {
    if (BarAssistant.isInternetConnected()) {
        val uid = currentUser?.uid
        if (uid != null) {
            fireStore.collection(MAIN_COLLECTION).document(uid).collection(RECENTLY_VIEWED_COLLECTION)
                    .document("main").get().addOnCompleteListener {
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
            fireStore.collection(RECIPE_COLLECTION).document(recipeId.toString())
                    .get().addOnCompleteListener { snapShotRecipe ->
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
        BarAssistant.lastViewedRecipes.keys.sortedByDescending { it -> it }.forEach {
            val numberOfKeyDuplicates = BarAssistant.lastViewedRecipes.values.count { recipe -> BarAssistant.lastViewedRecipes[it] == recipe }
            if(numberOfKeyDuplicates == 1) goodKeys.add(it)
            else goodKeys.add(BarAssistant.lastViewedRecipes.keys.asSequence().sortedByDescending { key -> key }.first { key -> BarAssistant.lastViewedRecipes[key] == BarAssistant.lastViewedRecipes[it] })
        }
        BarAssistant.lastViewedTimes.clear()
        BarAssistant.lastViewedTimes.addAll(goodKeys)
        val lastViewedRecipes = BarAssistant.lastViewedRecipes.filter { goodKeys.contains(it.key) }
        BarAssistant.lastViewedRecipes.clear()
        BarAssistant.lastViewedRecipes.putAll(lastViewedRecipes)
        BarAssistant.recipes[1].clear()
        BarAssistant.lastViewedTimes.sortedByDescending { it -> it }.forEach {
            val sortedRecipe = BarAssistant.lastViewedRecipes[it]
            if(sortedRecipe != null) BarAssistant.recipes[1].add(sortedRecipe)
        }
        (fragment as? HomeTab)?.refresh()
    }
}