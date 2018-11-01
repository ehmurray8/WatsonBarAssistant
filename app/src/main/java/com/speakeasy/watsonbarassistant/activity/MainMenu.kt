package com.speakeasy.watsonbarassistant.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabItem
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.algolia.search.saas.Client
import com.algolia.search.saas.Query
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.speakeasy.watsonbarassistant.*
import com.speakeasy.watsonbarassistant.discovery.HandleDiscovery
import com.speakeasy.watsonbarassistant.discovery.SearchDiscovery
import com.speakeasy.watsonbarassistant.extensions.loadRecentlyViewed
import com.speakeasy.watsonbarassistant.extensions.loadRecentlyViewedRecipesSharedPreferences
import com.speakeasy.watsonbarassistant.extensions.toast
import com.speakeasy.watsonbarassistant.fragment.FavoritesTab
import com.speakeasy.watsonbarassistant.fragment.HomeTab
import com.speakeasy.watsonbarassistant.fragment.IngredientsTab
import com.speakeasy.watsonbarassistant.fragment.SearchFragment
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.serialization.json.JSON
import java.util.*

class MainMenu : AppCompatActivity(), SearchView.OnQueryTextListener {
    var currentUser: FirebaseUser? = null
    var tabIndex = 1
    var fragment: Fragment? = null
    var searchFragment: SearchFragment? = null

    private var tabsItems: Array<TabItem>? = null

    internal val temporaryLastViewedTimes = mutableListOf<Long>()
    internal val temporaryLastViewedRecipes = mutableListOf<DiscoveryRecipe?>()

    internal val fireStore = FirebaseFirestore.getInstance()
    private var authorization = FirebaseAuth.getInstance()
    private var lastDiscoveryRefreshTime = -1L

    private val client = Client(ALGOLIA_APP_ID, ALGOLIA_API_KEY)
    private val recipeIndex = client.getIndex("Recipe")
    private var searchMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        BarAssistant.storageReference = FirebaseStorage.getInstance().reference

        loadSharedPreferences()

        val ingredientName = intent.getStringExtra("Ingredient")
        if(ingredientName != null) {
            addIngredient(ingredientName)
        } else {
            loadUserData()
        }

        tabs.getTabAt(tabIndex)?.select()
        tabs.addOnTabSelectedListener(MainMenuTabListener(this))
        setSupportActionBar(toolbar as Toolbar)
        if (!BarAssistant.isInternetConnected()) {
            applicationContext.toast("Failed to download user data from the internet.")
        }
    }

    private fun loadSharedPreferences() {
        val preferences = getSharedPreferences(SHARED_PREFERENCES_SETTINGS, Context.MODE_PRIVATE)
        tabIndex = preferences.getInt(TAB_INDEX, 1)
        val gson = Gson()
        BarAssistant.homeCategories.forEachIndexed { i, category ->
            synchronized(BarAssistant.recipes) {
                val recipeJson = preferences.getString(category, "")
                val storedRecipes = gson.fromJson(recipeJson, Array<DiscoveryRecipe>::class.java)
                if (storedRecipes != null && storedRecipes.count() > 0) {
                    BarAssistant.recipes[i].addAll(storedRecipes.toList())
                    synchronized(BarAssistant.feed) {
                        if (i == 0) BarAssistant.feed.addAll(storedRecipes.toList().map { FeedElement(it) })
                    }
                }
            }
        }
        val ingredientsJson = preferences.getString(INGREDIENT_PREFERENCES_ID, "")
        val lastViewedTimesJson = preferences.getString(LAST_VIEWED_RECIPE_TIMES, "")
        val storedLastViewedTimes = gson.fromJson(lastViewedTimesJson, Array<Long>::class.java)
        synchronized(BarAssistant.favoritesList) {
            val favoritesJson = preferences.getString(FAVORITES_PREFERENCES, "")
            val favorites = gson.fromJson(favoritesJson, Array<DiscoveryRecipe>::class.java)
            if (favorites != null && favorites.count() > 0) {
                BarAssistant.favoritesList.clear()
                BarAssistant.favoritesList.addAll(favorites)
            }
        }
        synchronized(BarAssistant.ingredients) {
            val storedIngredients = gson.fromJson(ingredientsJson, Array<Ingredient>::class.java)
            if (storedIngredients != null && storedIngredients.count() > 0) {
                BarAssistant.ingredients.clear()
                BarAssistant.ingredients.addAll(storedIngredients)
            }
        }
        if(storedLastViewedTimes != null && storedLastViewedTimes.count() > 0) {
            loadRecentlyViewedRecipesSharedPreferences(storedLastViewedTimes)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as? SearchManager
        searchMenuItem = menu?.findItem(R.id.searchMenuButton)
        val searchView = searchMenuItem?.actionView as? SearchView
        searchView?.findViewById<View?>(R.id.search_close_btn)?.setOnClickListener {
            searchView.setQuery("", false)
        }

        searchMenuItem?.setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                showCurrentFragment()
                searchFragment = null
                return true
            }
        })

        searchView?.setOnSearchClickListener { _ ->
            searchFragment = SearchFragment()
            synchronized(BarAssistant.searchRecipes) {
                BarAssistant.searchRecipes.clear()
                val numRecipes = BarAssistant.lastViewedRecipes.count()
                val end = if(numRecipes >= 25) 25 else numRecipes
                BarAssistant.searchRecipes.addAll(BarAssistant.lastViewedRecipes.map { it -> it.value }.slice(0..end))
            }
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, searchFragment)
            transaction.commit()
        }

        searchView?.setSearchableInfo(searchManager?.getSearchableInfo(componentName))
        searchView?.setOnQueryTextListener(this)
        return true
    }

    override fun onBackPressed() {
        searchMenuItem?.collapseActionView()
        when {
            searchFragment != null -> {
                showCurrentFragment()
                searchFragment = null
            }
            tabIndex != 1 -> {
                tabIndex = 1
                tabs.getTabAt(tabIndex)?.select()
                showCurrentFragment()
            }
            else -> super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        searchMenuItem?.collapseActionView()
        val uid = currentUser?.uid
        if(uid != null) {
            val ingredientsMap = synchronized(BarAssistant.ingredients) {
                mapOf(INGREDIENT_COLLECTION to BarAssistant.ingredients.asSequence().map { it.name }.toList())
            }
            fireStore.collection(MAIN_COLLECTION).document(uid).collection(INGREDIENT_COLLECTION)
                    .document("main").set(ingredientsMap)
        }
        val preferences = getSharedPreferences(SHARED_PREFERENCES_SETTINGS, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(TAB_INDEX, tabIndex)
        val gson = Gson()
        val ingredientJson = synchronized(BarAssistant.ingredients) {
            gson.toJson(BarAssistant.ingredients.toTypedArray())
        }
        editor.putString(INGREDIENT_PREFERENCES_ID, ingredientJson)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        synchronized(BarAssistant.lastViewedTimes) {
            BarAssistant.lastViewedTimes.clear()
        }
        synchronized(BarAssistant.lastViewedRecipes) {
            BarAssistant.lastViewedRecipes.clear()
        }
        synchronized(BarAssistant.recipes) {
            BarAssistant.recipes.forEach { it.clear() }
        }
    }

    private fun loadUserData() {
        currentUser = authorization.currentUser
        loadIngredients()
        loadRecentlyViewed()
        (application as? BarAssistant)?.loadFavoritesFromFireStore(authorization, fireStore)
        (fragment as? IngredientsTab)?.refresh()
        (fragment as? FavoritesTab)?.refresh()
    }

    private fun loadIngredients() {
        if (BarAssistant.isInternetConnected()) {
            val uid = currentUser?.uid
            refreshDiscovery()
            val oldIngredients = synchronized(BarAssistant.ingredients) { BarAssistant.ingredients.toTypedArray() }
            if (uid != null) {
                fireStore.collection(MAIN_COLLECTION).document(uid)
                        .collection(INGREDIENT_COLLECTION).document("main").get().addOnSuccessListener {
                            synchronized(BarAssistant.ingredients) {
                                BarAssistant.ingredients.clear()
                                val storedIngredients = it.get(INGREDIENT_COLLECTION) as? ArrayList<*>
                                storedIngredients?.forEach { element: Any ->
                                    val name = element as? String
                                    if (name != null) {
                                        BarAssistant.ingredients.add(Ingredient(name))
                                    }
                                }
                            }
                            if (!oldIngredients.toMutableList().containsAll(synchronized(BarAssistant.ingredients){BarAssistant.ingredients})) {
                                refreshDiscovery(true)
                            }
                        }
            }
        }
    }

    fun refreshDiscovery(forceRefresh: Boolean = false) {
        synchronized(BarAssistant.ingredients) {
            if (BarAssistant.ingredients.count() > 0) {
                if (forceRefresh || lastDiscoveryRefreshTime == -1L ||
                        Date().time - lastDiscoveryRefreshTime >= 30_000) {
                    lastDiscoveryRefreshTime = Date().time
                    val discovery = SearchDiscovery(HandleDiscovery(this))
                    discovery.execute(BarAssistant.ingredients.toTypedArray())
                }
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
            2 -> fragment = FavoritesTab()
        }
        replaceFragment()
    }

    private fun replaceFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment ?: return)
        transaction.commit()
        tabsItems?.get(tabIndex)?.isSelected = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.user_profile -> {
                val intent = Intent(this, UserProfile::class.java)
                startActivity(intent)
                return true
            }
            R.id.shoppingCartMenuButton -> {
                val intent = Intent(this, ShoppingCart::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun addIngredient(name: String) {
        val ingredient = Ingredient(name)
        synchronized(BarAssistant.ingredients) {
            if (BarAssistant.ingredients.any { it.name.toLowerCase() == ingredient.name.toLowerCase() }) {
                applicationContext?.toast("${ingredient.name} is already stored as an ingredient.")
            } else {
                BarAssistant.ingredients.add(ingredient)
                refreshDiscovery(true)
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query != null && query != "") {
            searchAlgolia(query)
            return true
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(newText != null && newText != "") {
            searchAlgolia(newText)
            return true
        }
        return true
    }

    private fun searchAlgolia(query: String) {
        recipeIndex.searchAsync(Query(query)) { content, error ->
            if (error != null) {
                Log.d("Algolia", "Error Code: ${error.statusCode}, Message: ${error.message}")
                applicationContext.toast("Failed to retrieve any results.")
            } else if (content != null) {
                val response = content.getJSONArray("hits")
                if (response.length() == 0) {
                    applicationContext.toast("No results for $query.")
                }
                synchronized(BarAssistant.searchRecipes) {
                    BarAssistant.searchRecipes.clear()
                }
                for (i in 0 until response.length()) {
                    val recipe = JSON.nonstrict.parse<DiscoveryRecipe>(response.getJSONObject(i).toString())
                    synchronized(BarAssistant.searchRecipes) {
                        BarAssistant.searchRecipes.add(recipe)
                    }
                }
                searchFragment?.refresh()
            }
        }
    }
}