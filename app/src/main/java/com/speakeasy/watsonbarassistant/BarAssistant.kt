package com.speakeasy.watsonbarassistant

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.common.util.ByteConstants
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import java.util.ArrayList


class BarAssistant: Application() {

    companion object {

        var defaultImage: Drawable? = null
        var networkInfo: NetworkInfo? = null
        var storageReference: StorageReference? = null
        var recipes = mutableListOf<MutableList<DiscoveryRecipe>>()
        var favorites = mutableListOf<String>()

        val lastViewedRecipes: MutableMap<Long, DiscoveryRecipe> = mutableMapOf()
        val lastViewedTimes: MutableList<Long> = mutableListOf()
        val homeCategories = mutableListOf(SUGGESTIONS_CATEGORY, RECENTLY_VIEWED_CATEGORY)

        fun isInternetConnected(): Boolean {
            return networkInfo?.isConnectedOrConnecting == true
        }
    }

    init {
        if(recipes.isEmpty()) {
            homeCategories.forEach { _ ->
                recipes.add(mutableListOf())
            }
        }
        if(favorites.isEmpty()) {
            homeCategories.forEach { _ ->
                favorites = mutableListOf()
            }
        }

    }

    override fun onCreate() {
        super.onCreate()
        setupFresco()
        defaultImage = Drawable.createFromStream(assets.open(DEFAULT_RECIPE_IMAGE_NAME), null)
        networkInfo = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
    }

    private fun setupFresco() {
        val diskCacheConfig = DiskCacheConfig.newBuilder(baseContext).setBaseDirectoryPath(baseContext.cacheDir)
            .setBaseDirectoryName("v1").setMaxCacheSize(100 * ByteConstants.MB.toLong())
            .setMaxCacheSizeOnLowDiskSpace(10 * ByteConstants.MB.toLong())
            .setMaxCacheSizeOnVeryLowDiskSpace(5 * ByteConstants.MB.toLong()).setVersion(1).build()
        val imagePipelineConfig = ImagePipelineConfig.newBuilder(baseContext)
            .setMainDiskCacheConfig(diskCacheConfig).build()
        Fresco.initialize(baseContext, imagePipelineConfig)
    }

    fun storeRecentlyViewed(authorization: FirebaseAuth, fireStore: FirebaseFirestore) {
        storeRecentlyViewedFireStore(authorization, fireStore)
        val preferences = getSharedPreferences(SHARED_PREFERENCES_SETTINGS, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val gson = Gson()
        BarAssistant.homeCategories.forEachIndexed { i, category ->
            val json = gson.toJson(BarAssistant.recipes[i].toTypedArray())
            editor.putString(category, json)
        }
        val lastViewedTimesJson = gson.toJson(BarAssistant.lastViewedTimes.sortedByDescending { it -> it })
        editor.putString(LAST_VIEWED_RECIPE_TIMES, lastViewedTimesJson)
        editor.apply()
    }

    private fun storeRecentlyViewedFireStore(authorization: FirebaseAuth, fireStore: FirebaseFirestore) {
        if (BarAssistant.isInternetConnected()) {
            val uid = authorization.currentUser?.uid
            if (uid != null) {
                val recipes = mutableListOf<Int>()
                BarAssistant.lastViewedRecipes.keys.sortedByDescending { it -> it }
                        .forEach {
                            val recipe = BarAssistant.lastViewedRecipes[it]
                            if(recipe != null) recipes.add(recipe.imageId.toFloat().toInt())
                        }
                val recentlyViewedMap = mapOf(LAST_VIEWED_RECIPE_TIMES to BarAssistant.lastViewedTimes,
                        LAST_VIEWED_RECIPES to recipes)
                fireStore.collection(MAIN_COLLECTION).document(uid).collection(RECENTLY_VIEWED_COLLECTION)
                        .document("main").set(recentlyViewedMap)
            }
        }
    }

    fun loadFavoritesFromFireStore(authorization: FirebaseAuth, fireStore: FirebaseFirestore) {
        val uid = authorization.currentUser?.uid
        if(uid != null) {
            fireStore.collection(MAIN_COLLECTION).document(uid).collection(FAVORITES_COLLECTION)
                    .document("main").get().addOnSuccessListener {

                        favorites = it.get(FAVORITES_LIST) as MutableList<String>
                        favorites.forEachIndexed { index, s ->
                            //var currentFavorite = favorites[index].toInt()
                            //var recipesList = fireStore.collection(MAIN_COLLECTION).document(uid).collection(FAVORITES_COLLECTION)
                                    //.document("main").id[currentFavorite]
                            //favoritesRecipes.add(
                        }
                    }
        }
    }

    fun updateFavoriteFireStore(authorization: FirebaseAuth, fireStore: FirebaseFirestore) {
        if (BarAssistant.isInternetConnected()) {
            val uid = authorization.currentUser?.uid
            val favoritesMap = mapOf(FAVORITES_LIST to favorites)
            if (uid != null) {
                fireStore.collection(MAIN_COLLECTION).document(uid).collection(FAVORITES_COLLECTION).document("main").set(favoritesMap)
            }
        }
    }
}