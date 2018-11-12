package com.speakeasy.watsonbarassistant

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.speakeasy.watsonbarassistant.extensions.allRecipesDocument
import com.speakeasy.watsonbarassistant.extensions.favoritesDocument
import com.speakeasy.watsonbarassistant.extensions.recipeDocument
import com.speakeasy.watsonbarassistant.extensions.toStringMutableList
import java.util.*
import kotlin.concurrent.thread


const val MAX_FRIEND_RECIPES = 85
const val NUM_POPULAR_RECIPES = 15
const val UPPER_RANDOM_LIMIT = 50
const val LOWER_RANDOM_LIMIT = 15


fun loadFeedRecipes(fireStore: FirebaseFirestore, refresh: (() -> Unit)? = null) {
    val ingredientsList = BarAssistant.ingredients.toMutableList()
    thread {
        val randomRecipes = if (ingredientsList.count() == 0) {
            getRandomRecipes(UPPER_RANDOM_LIMIT, fireStore)
        } else {
            getRandomRecipes(LOWER_RANDOM_LIMIT, fireStore)
        }
        val popularRecipes = getPopularRecipes(fireStore)
        val friendRecipes = getFriendRecipes(fireStore)
        synchronized(BarAssistant.feed) {
            BarAssistant.feed.clear()
            BarAssistant.feed.addAll(randomRecipes)
            BarAssistant.feed.addAll(friendRecipes)
            BarAssistant.feed.addAll(popularRecipes)
        }
        if (BarAssistant.friends.count() > 0 && ingredientsList.count() > 0) {
            addSuggestions(BarAssistant.feed, 15)
        } else if (BarAssistant.friends.count() > 0) {
            addSuggestions(BarAssistant.feed, 10)
        } else if (BarAssistant.ingredients.count() > 0) {
            addSuggestions(BarAssistant.feed, 30)
        }
        synchronized(BarAssistant.feed) {
            BarAssistant.feed.shuffle()
            refresh?.invoke()
        }
    }
}

private fun addSuggestions(feedElements: MutableList<FeedElement>, count: Int) {
    if(BarAssistant.recipes[0].count() >= count) {
        synchronized(BarAssistant.feed) {
            feedElements.addAll(BarAssistant.recipes[0].slice(0 until count).map{ FeedElement(it, FeedType.SUGGESTION) })
        }
    } else if(BarAssistant.recipes.count() > 0) {
        synchronized(BarAssistant.feed) {
            feedElements.addAll(BarAssistant.recipes[0].slice(0 until BarAssistant.recipes[0].count()).map {
                FeedElement(it, FeedType.SUGGESTION)
            })
        }
    }
}

private fun getRandomRecipes(count: Int, fireStore: FirebaseFirestore): MutableList<FeedElement> {
    val document = Tasks.await(fireStore.allRecipesDocument().get())
    val storedRecipes = document.get(ALL_RECIPES_LIST) as? ArrayList<*>
    val allRecipeIds: MutableList<String>? = storedRecipes?.toStringMutableList()
    val allIds = allRecipeIds ?: mutableListOf()
    allIds.shuffle()
    val allIdSubset = allIds.slice(0 until count)
    val feedElements = mutableListOf<FeedElement>()
    val callbacks = mutableListOf<Task<DocumentSnapshot>>()
    for (id in allIdSubset) {
        val task = fireStore.recipeDocument(id).get()
        callbacks.add(task)
        task.addOnSuccessListener {
            val recipe = it.toObject(FireStoreRecipe::class.java)?.toDiscoveryRecipe()
            recipe?.let{ r -> feedElements.add(FeedElement(r, FeedType.RANDOM))}
        }
    }
    Tasks.await(Tasks.whenAllComplete(callbacks))
    return feedElements
}

private fun getPopularRecipes(fireStore: FirebaseFirestore): MutableList<FeedElement> {
    val query = fireStore.collection(RECIPE_COLLECTION).orderBy("favoriteCount", Query.Direction.DESCENDING)
            .limit(NUM_POPULAR_RECIPES.toLong())
    val popularSnapshot = Tasks.await(query.get())
    val popularRecipes = mutableListOf<FeedElement>()
    popularSnapshot.documents.forEach {
        val recipe = it.toObject(FireStoreRecipe::class.java)?.toDiscoveryRecipe()
        recipe?.let { r -> popularRecipes.add(FeedElement(r, FeedType.POPULAR)) }
    }
    return popularRecipes
}

private fun getFriendRecipes(fireStore: FirebaseFirestore): MutableList<FeedElement> {
    val friendRecipes = mutableMapOf<String, MutableList<DiscoveryRecipe>>()
    Log.d("Feed Algorithm", BarAssistant.friends.toString())
    val friends = synchronized(BarAssistant.friends){BarAssistant.friends}
    val tasks = mutableListOf<Task<DocumentSnapshot>>()
    for(friend in friends) {
        friend.userId?.let { fid ->
            friendRecipes[friend.username] = mutableListOf()
            val document = Tasks.await(fireStore.favoritesDocument(fid).get())
            val storedIds = document.get(FAVORITES_LIST) as? ArrayList<*>
            val storedStringIds = storedIds?.toStringMutableList() ?: mutableListOf()
            Log.d("Feed Algorithm", "Stored ids: " + storedIds?.toString())
            Log.d("Feed Algorithm", "Stored string ids: " + storedStringIds.toString())
            for(rid in storedStringIds) {
                val task = fireStore.recipeDocument(rid).get()
                tasks.add(task)
                task.addOnSuccessListener {
                    val recipe = it.toObject(FireStoreRecipe::class.java)?.toDiscoveryRecipe()
                    recipe?.let{ r -> friendRecipes[friend.username]?.add(r) }
                }
            }
        }
    }
    Tasks.await(Tasks.whenAllComplete(tasks))
    val feedElements = mutableListOf<FeedElement>()
    Log.d("Feed Algorithm", "Friend recipes" + friendRecipes.toString())
    do {
        var keepGoing = false
        for ((friendUsername, recipeList) in friendRecipes){
            if(feedElements.count() >= MAX_FRIEND_RECIPES) {
                break
            }
            val count = recipeList.count()
            if (count > 0) {
                keepGoing = true
                val recipe = recipeList.removeAt(count - 1)
                val feedElement = FeedElement(recipe, FeedType.FRIEND, friendUsername)
                Log.d("Feed Algorithm", feedElement.toString())
                feedElements.add(feedElement)
            }
        }
    } while(keepGoing && feedElements.count() < MAX_FRIEND_RECIPES)
    Log.d("Feed Algorithm", feedElements.toString())
    return feedElements
}