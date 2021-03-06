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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.speakeasy.watsonbarassistant.com.speakeasy.watsonbarassistant.AddToAlgolia
import com.speakeasy.watsonbarassistant.extensions.*
import java.util.*


class BarAssistant: Application() {

    companion object {
        var defaultImage: Drawable? = null
        var networkInfo: NetworkInfo? = null
        var storageReference: StorageReference? = null
        var userInfo: UserInfo? = null

        val recipes = mutableListOf<MutableList<DiscoveryRecipe>>()
        val favoritesList = sortedSetOf<DiscoveryRecipe>()
        val userCreatedRecipes = sortedSetOf<DiscoveryRecipe>()
        val ingredients = sortedSetOf<Ingredient>(kotlin.Comparator { o1, o2 -> if (o1.compareName() > o2.compareName()) 1 else -1 })
        val searchRecipes = mutableListOf<DiscoveryRecipe>()
        val feed = mutableListOf<FeedElement>()
        val friends = mutableListOf<UserInfo>()

        // Active requests that the user has made to other users
        val pendingRequests = mutableListOf<UserInfo>()

        // Requests that the user has received that they have not yet accepted
        val requestsInProgress = mutableListOf<UserInfo>()

        val allUsers = mutableListOf<UserInfo>()

        val blockedUsers = mutableListOf<UserInfo>()

        val lastViewedRecipes: MutableMap<Long, DiscoveryRecipe> = mutableMapOf()
        val lastViewedTimes: MutableList<Long> = mutableListOf()
        val homeCategories = listOf(SUGGESTIONS_CATEGORY, RECENTLY_VIEWED_CATEGORY)

        var secondLevelIngredients : MutableList<MutableList<String>>? = ArrayList()
        var thirdLevelIngredients : MutableList<MutableList<String>>? = ArrayList()
        var currentIngredientCategoryIndex: Int = 0

        fun isInternetConnected(): Boolean {
            return networkInfo?.isConnectedOrConnecting == true
        }
    }

    init {
        synchronized(recipes) {
            if (recipes.isEmpty()) {
                homeCategories.forEach { _ ->
                    recipes.add(mutableListOf())
                }
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

    fun storeRecentlyViewedAndFavorites(authorization: FirebaseAuth, fireStore: FirebaseFirestore) {
        storeRecentlyViewedFireStore(authorization, fireStore)
        val preferences = getSharedPreferences(SHARED_PREFERENCES_SETTINGS, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val gson = Gson()
        BarAssistant.homeCategories.forEachIndexed { i, category ->
            val json = synchronized(BarAssistant.recipes) {
                gson.toJson(BarAssistant.recipes[i].toTypedArray())
            }
            editor.putString(category, json)
        }
        val lastViewedTimesJson = synchronized(BarAssistant.lastViewedTimes) {
            gson.toJson(BarAssistant.lastViewedTimes.sortedByDescending { it -> it })
        }
        editor.putString(LAST_VIEWED_RECIPE_TIMES, lastViewedTimesJson)
        val favoritesJson = synchronized(BarAssistant.favoritesList) {
            gson.toJson(BarAssistant.favoritesList)
        }
        editor.putString(FAVORITES_PREFERENCES, favoritesJson)
        editor.apply()
    }

    fun addNewImageToFireStore(newImage: ByteArray):String{
        var newImageId = "-1"

        if (BarAssistant.isInternetConnected()) {
            synchronized(BarAssistant.userCreatedRecipes) {
                newImageId = UUID.randomUUID().mostSignificantBits.toString().dropLast(4)
                val imagePath = "$RECIPE_IMAGES/GSBimg-$newImageId.jpg"
                val uploadTask = BarAssistant.storageReference?.child(imagePath)?.putBytes(newImage)
                uploadTask?.addOnFailureListener{
                    //TODO failure thing
                }?.addOnSuccessListener {
                    //TODO on success thing
                }
            }
        }
        return newImageId
    }

    fun storeNewRecipeInFireStore(authorization: FirebaseAuth, fireStore: FirebaseFirestore, newRecipe: DiscoveryRecipe) {
        if (BarAssistant.isInternetConnected()) {
            val uid = authorization.currentUser?.uid
            if (uid != null) {
                synchronized(BarAssistant.userCreatedRecipes) {

                    //Add to master recipe list
                    val fireRecipe = newRecipe.toFireStoreRecipe()
                    fireStore.collection(RECIPE_COLLECTION).document(newRecipe.imageId).set(fireRecipe)
                    //Add to user created list
                    synchronized(BarAssistant.userCreatedRecipes){
                        userCreatedRecipes.add(newRecipe)
                    }

                    updateUserCreatedReipesFireStore(authorization,fireStore)

                    val addToAlgolia = AddToAlgolia()
                    addToAlgolia.execute(newRecipe)
                }
            } else {
                //TODO invalid user error
            }
        }
    }

    private fun storeRecentlyViewedFireStore(authorization: FirebaseAuth, fireStore: FirebaseFirestore) {
        if (BarAssistant.isInternetConnected()) {
            val uid = authorization.currentUser?.uid
            if (uid != null) {
                synchronized(BarAssistant.lastViewedRecipes) {
                    val recipes = mutableListOf<Int>()
                    BarAssistant.lastViewedRecipes.keys.sortedByDescending { it -> it } .forEach {
                        val recipe = BarAssistant.lastViewedRecipes[it]
                        if (recipe != null) recipes.add(recipe.imageId.toFloat().toInt())
                    }
                    synchronized(BarAssistant.lastViewedTimes) {
                        val recentlyViewedMap = mapOf(
                                LAST_VIEWED_RECIPE_TIMES to BarAssistant.lastViewedTimes,
                                LAST_VIEWED_RECIPES to recipes)
                        fireStore.recentlyViewedDocument(uid).set(recentlyViewedMap)
                    }
                }
            }
        }
    }

    fun loadFavoritesFromFireStore(authorization: FirebaseAuth, fireStore: FirebaseFirestore) {
        val uid = authorization.currentUser?.uid
        if(isInternetConnected() && uid != null) {
            fireStore.favoritesDocument(uid).get().addOnSuccessListener {
                loadRecipeFromDocument(it, fireStore)
            }
        }
    }

    fun loadUserCreatedRecipesFromFireStore(authorization: FirebaseAuth, fireStore: FirebaseFirestore) {
        val uid = authorization.currentUser?.uid
        if(isInternetConnected() && uid != null) {
            fireStore.userCreatedRecipesDocument(uid).get().addOnSuccessListener {
                loadRecipeFromDocumentToUserCreatedList(it, fireStore)
            }
        }
    }

    private fun loadRecipeFromDocumentToUserCreatedList(document: DocumentSnapshot, fireStore: FirebaseFirestore){
        val temporaryList = mutableListOf<DiscoveryRecipe>()
        val recipeIds = document.get(USER_CREATED_RECIPES_LIST) as? ArrayList<*>
        var count = 0
        recipeIds?.forEach { recipeId ->
            fireStore.recipeDocument(recipeId.toString()).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val recipeDocument = it.result ?: return@addOnCompleteListener
                    val recipe = recipeDocument.toObject(FireStoreRecipe::class.java)
                    temporaryList.add(recipe?.toDiscoveryRecipe() ?: return@addOnCompleteListener)
                    if(++count >= recipeIds.count()) {
                        synchronized(BarAssistant.userCreatedRecipes) {
                            userCreatedRecipes.clear()
                            userCreatedRecipes.addAll(temporaryList)
                        }
                    }
                }
            }
        }
    }

    private fun loadRecipeFromDocument(document: DocumentSnapshot, fireStore: FirebaseFirestore) {
        val temporaryList = mutableListOf<DiscoveryRecipe>()
        val favoriteIds = document.get(FAVORITES_LIST) as? ArrayList<*>
        var count = 0
        favoriteIds?.forEach { recipeId ->
            fireStore.recipeDocument(recipeId.toString()).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val recipeDocument = it.result ?: return@addOnCompleteListener
                    val favorite = recipeDocument.toObject(FireStoreRecipe::class.java)
                    temporaryList.add(favorite?.toDiscoveryRecipe() ?: return@addOnCompleteListener)
                    if(++count >= favoriteIds.count()) {
                        synchronized(BarAssistant.favoritesList) {
                            favoritesList.clear()
                            favoritesList.addAll(temporaryList)
                        }
                    }
                }
            }
        }
    }

    private fun updateUserCreatedReipesFireStore(authorization: FirebaseAuth, fireStore: FirebaseFirestore) {
        if (BarAssistant.isInternetConnected()) {
            val uid = authorization.currentUser?.uid
            val recipesMap = synchronized(BarAssistant.userCreatedRecipes) {
                mapOf(USER_CREATED_RECIPES_LIST to userCreatedRecipes.map { it.imageId.toLong() })
            }
            if (uid != null) {
                fireStore.userCreatedRecipesDocument(uid).set(recipesMap)
                userCreatedRecipes.forEach { userRecipe ->
                    fireStore.recipeDocument(userRecipe.imageId).get().addOnSuccessListener {
                        val recipe = it.toObject(FireStoreRecipe::class.java)
                        if(recipe != null) {
                            recipe.favoriteCount
                            fireStore.recipeDocument(userRecipe.imageId).set(recipe)
                        }
                    }
                }
            }
        }
    }

    fun updateFavoriteFireStore(authorization: FirebaseAuth, fireStore: FirebaseFirestore) {
        if (BarAssistant.isInternetConnected()) {
            val uid = authorization.currentUser?.uid
            val favoritesMap = synchronized(BarAssistant.favoritesList) {
                mapOf(FAVORITES_LIST to favoritesList.map { it.imageId.toFloat().toInt() })
            }
            if (uid != null) {
                fireStore.favoritesDocument(uid).set(favoritesMap)
                favoritesList.forEach { favorite ->
                    fireStore.recipeDocument(favorite.imageId).get().addOnSuccessListener {
                        val recipe = it.toObject(FireStoreRecipe::class.java)
                        if(recipe != null) {
                            recipe.favoriteCount++
                            fireStore.recipeDocument(favorite.imageId).set(recipe)
                        }
                    }
                }
            }
        }
    }

    fun loadUserInfo(authorization: FirebaseAuth, fireStore: FirebaseFirestore) {
        if(BarAssistant.isInternetConnected()) {
            val uid = authorization.currentUser?.uid
            if (uid != null) {
                getUserInfoFromCollection(fireStore, uid, PENDING_FRIENDS_COLLECTION, pendingRequests, PENDING_LIST)
                getUserInfoFromCollection(fireStore, uid, FRIEND_REQUEST_COLLECTION, requestsInProgress, REQUEST_LIST)
                getUserInfoFromCollection(fireStore, uid, FRIENDS_COLLECTION, friends, FRIEND_LIST)
                getUserInfoFromCollection(fireStore, uid, BLOCKED_COLLECTION, blockedUsers, BLOCKED_LIST)
                getAllUsers(fireStore)
                loadFeedRecipes(fireStore)
            }
            loadMasterIngredientsFromFireStore(authorization, fireStore)
        }
    }

    private fun getUserInfoFromCollection(fireStore: FirebaseFirestore, uid: String,
                                          collection: String, outputList: MutableList<UserInfo>,
                                          identifier: String) {
        fireStore.appDocument(uid, collection).get().addOnSuccessListener {
            val requestIds = it.get(identifier) as? ArrayList<*>
            synchronized(outputList) {
                outputList.clear()
            }
            requestIds?.forEach { rid ->
                (rid as? String)?.let { otherUserId ->
                    addUserInfoToList(fireStore, otherUserId, outputList)
                }
            }
        }
    }

    private fun addUserInfoToList(fireStore: FirebaseFirestore, otherUserId: String,
                                  outputList: MutableList<UserInfo>) {
        fireStore.userDocument(otherUserId).get().addOnSuccessListener {
            it.toObject(UserInfo::class.java)?.let { userInfo ->
                synchronized(outputList) {
                    userInfo.userId = otherUserId
                    outputList.add(userInfo)
                }
            }
        }
    }

    private fun getAllUsers(fireStore: FirebaseFirestore) {
        fireStore.allUsersDocument().get().addOnSuccessListener {
            val storedUserIds = it.get(ALL_USERS_LIST) as? ArrayList<*>
            val allUserIds = storedUserIds?.toStringMutableList()
            allUserIds?.forEach { userId ->
                synchronized(BarAssistant.allUsers){
                    BarAssistant.allUsers.clear()
                }
                fireStore.userDocument(userId).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.toObject(UserInfo::class.java)?.let { user ->
                            synchronized(BarAssistant.allUsers) {
                                user.userId = userId
                                BarAssistant.allUsers.add(user)
                            }
                        }
                    }
                }
            }
        }
    }

    fun loadMasterIngredientsFromFireStore(authorization: FirebaseAuth, fireStore: FirebaseFirestore) {
        val uid = authorization.currentUser?.uid
        if(isInternetConnected() && uid != null) {
            fireStore.collection(INGREDIENT_COLLECTION).get().addOnSuccessListener {
                secondLevelIngredients?.clear()
                thirdLevelIngredients?.clear()
                loadMasterIngredients(fireStore)
            }
        }
    }

    private fun loadMasterIngredients(fireStore: FirebaseFirestore) {
        fireStore.collection(INGREDIENT_COLLECTION).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.forEach { document ->
                    secondLevelIngredients?.add(document.data.keys.toMutableList())
                    document.data.entries.forEach { index  ->
                        val temp = index.value as ArrayList<*>
                        if(temp.size != 0) {
                            thirdLevelIngredients?.add(temp.toStringMutableList())
                        } else{
                            thirdLevelIngredients?.add(mutableListOf())
                        }
                    }
                }
            }
        }
    }
}