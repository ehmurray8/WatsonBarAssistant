package com.speakeasy.watsonbarassistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabItem
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.speakeasy.watsonbarassistant.discovery.HandleDiscovery
import com.speakeasy.watsonbarassistant.discovery.SearchDiscovery
import kotlinx.android.synthetic.main.activity_main_menu.*
import java.util.*

class MainMenu : AppCompatActivity() {

    var ingredients = sortedSetOf<Ingredient>(kotlin.Comparator { o1, o2 -> if (o1.compareName() > o2.compareName()) 1 else -1 })
    var documentsMap = mutableMapOf<String, String>()
    var currentUser: FirebaseUser? = null
    var tabIndex = 1
    var fragment: Fragment? = null

    private var tabsItems: Array<TabItem>? = null

    internal val temporaryLastViewedTimes = mutableListOf<Long>()
    internal val temporaryLastViewedRecipes = mutableListOf<DiscoveryRecipe?>()

    internal val fireStore = FirebaseFirestore.getInstance()
    private var authorization = FirebaseAuth.getInstance()
    private var lastDiscoveryRefreshTime = -1L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        BarAssistant.storageReference = FirebaseStorage.getInstance().reference

        loadSharedPreferences()
        loadUserData()
        tabs.getTabAt(tabIndex)?.select()
        tabs.addOnTabSelectedListener(MainMenuTabListener(this))
        setSupportActionBar(toolbar as Toolbar)
        if (!BarAssistant.isInternetConnected()) {
            Toast.makeText(baseContext, "Failed to download user data from the internet.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSharedPreferences() {
        val preferences = getSharedPreferences(SHARED_PREFERENCES_SETTINGS, Context.MODE_PRIVATE)
        tabIndex = preferences.getInt(TAB_INDEX, 1)
        val gson = Gson()
        BarAssistant.homeCategories.forEachIndexed { i, category ->
            val recipeJson = preferences.getString(category, "")
            val storedRecipes = gson.fromJson(recipeJson, Array<DiscoveryRecipe>::class.java)
            val ingredientsJson = preferences.getString(INGREDIENT_PREFERENCES_ID, "")
            val storedIngredients = gson.fromJson(ingredientsJson, Array<Ingredient>::class.java)
            val lastViewedTimesJson = preferences.getString(LAST_VIEWED_RECIPE_TIMES, "")
            val storedLastViewedTimes = gson.fromJson(lastViewedTimesJson, Array<Long>::class.java)
            if (storedRecipes != null && storedRecipes.count() > 0) {
                BarAssistant.recipes[i].addAll(storedRecipes.toList())
            }
            if (storedIngredients != null && storedIngredients.count() > 0) {
                ingredients.clear()
                ingredients.addAll(storedIngredients)
            }
            loadRecentlyViewedRecipesSharedPreferences(storedLastViewedTimes
                    ?: return@forEachIndexed)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onPause() {
        super.onPause()
        val barAssistant = (application as BarAssistant)
        barAssistant.storeRecentlyViewed(authorization, fireStore)
        val preferences = getSharedPreferences(SHARED_PREFERENCES_SETTINGS, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val gson = Gson()
        val ingredientJson = gson.toJson(ingredients.toTypedArray())
        editor.putString(INGREDIENT_PREFERENCES_ID, ingredientJson)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        BarAssistant.lastViewedTimes.clear()
        BarAssistant.lastViewedRecipes.clear()
        BarAssistant.recipes.forEach { it.clear() }
    }

    private fun loadUserData() {
        currentUser = authorization.currentUser
        loadIngredients()
        loadRecentlyViewed()
        (fragment as? IngredientsTab)?.refresh()
    }

    private fun loadIngredients() {
        if (BarAssistant.isInternetConnected()) {
            val uid = currentUser?.uid
            refreshDiscovery()
            val oldIngredients = ingredients.toTypedArray()
            if (uid != null) {
                fireStore.collection(MAIN_COLLECTION).document(uid)
                        .collection(INGREDIENT_COLLECTION).get().addOnCompleteListener {
                            ingredients.clear()
                            if (it.isSuccessful) {
                                it.result?.forEach { snapshot ->
                                    parseSnapshot(snapshot)
                                }
                                if (!oldIngredients.toMutableList().containsAll(ingredients)) {
                                    refreshDiscovery(true)
                                }
                            }
                        }
            }
        }
    }

    fun refreshDiscovery(forceRefresh: Boolean = false) {
        if (ingredients.count() > 0) {
            if (forceRefresh || lastDiscoveryRefreshTime == -1L ||
                    Date().time - lastDiscoveryRefreshTime >= 60_000) {
                lastDiscoveryRefreshTime = Date().time
                val discovery = SearchDiscovery(HandleDiscovery(this))
                discovery.execute(ingredients.toTypedArray())
            }
        }
    }

    override fun onStart() {
        super.onStart()
        showCurrentFragment()
    }

    fun showCurrentFragment() {
        when (tabIndex) {
            0 -> fragment = IngredientsTab()
            1 -> fragment = HomeTab()
            2 -> fragment = MyRecipesTab()
        }
        replaceFragment()
    }

    private fun replaceFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment ?: return)
        transaction.commit()
        tabsItems?.get(tabIndex)?.isSelected = true
    }

    private fun parseSnapshot(snapshot: QueryDocumentSnapshot) {
        val name = snapshot.get("name") as? String
        val id = snapshot.id
        if (name != null) {
            documentsMap[name] = id
            val ingredient = Ingredient(name)
            ingredients.add(ingredient)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.user_profile -> {
                val intent = Intent(this, UserProfile::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}