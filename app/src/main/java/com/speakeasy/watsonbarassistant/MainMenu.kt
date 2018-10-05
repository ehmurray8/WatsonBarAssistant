package com.speakeasy.watsonbarassistant

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.TabItem
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.gson.Gson
import com.ibm.watson.developer_cloud.discovery.v1.Discovery
import com.ibm.watson.developer_cloud.discovery.v1.model.QueryOptions
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.serialization.json.JSON
import java.util.*


class MainMenu : AppCompatActivity() {

    var ingredients = mutableListOf<Ingredient>()
    var recipes = mutableListOf<MutableList<DiscoveryRecipe>>()
    var homeCategories = mutableListOf<String>()
    var documentsMap = mutableMapOf<String, String>()
    var currentUser: FirebaseUser? = null
    var tabIndex = 1
    var fragment: Fragment? = null

    private var tabsItems: Array<TabItem>? = null

    private val fireStore = FirebaseFirestore.getInstance()
    private var authorization = FirebaseAuth.getInstance()

    private var scrollPositionMap: MutableMap<String, Int> = mutableMapOf()


    private var lastDiscoveryRefreshTime = -1L

    init {
        recipes.add(0, mutableListOf())
        recipes.add(1, mutableListOf())
        homeCategories.add("Suggestions")
        homeCategories.add("Recently Viewed")
        homeCategories.forEach {
            scrollPositionMap[it] = 0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        loadSharedPreferences()
        loadUserData()
        tabs.getTabAt(tabIndex)?.select()
        tabs.addOnTabSelectedListener(MainMenuTabListener(this))
        setSupportActionBar(toolbar as Toolbar)
    }

    private fun loadSharedPreferences() {
        val preferences = getSharedPreferences(SHARED_PREFERENCES_SETTINGS, Context.MODE_PRIVATE)
        tabIndex = preferences.getInt(TAB_INDEX, 1)
        val gson = Gson()
        homeCategories.forEachIndexed { i, category ->
            val recipeJson = preferences.getString(category, "")
            val storedRecipes = gson.fromJson(recipeJson, Array<DiscoveryRecipe>::class.java)
            val ingredientsJson = preferences.getString(INGREDIENT_PREFERENCES_ID, "")
            val storedIngredients = gson.fromJson(ingredientsJson, Array<Ingredient>::class.java)
            if(storedRecipes != null && storedRecipes.count() > 0) {
                recipes[i].addAll(storedRecipes.toList())
            }
            if (storedIngredients != null && storedIngredients.count() > 0) {
                ingredients.addAll(storedIngredients)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        val preferences = getSharedPreferences(SHARED_PREFERENCES_SETTINGS, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val gson = Gson()
        homeCategories.forEachIndexed { i, category ->
            val json = gson.toJson(recipes[i].toTypedArray())
            editor.putString(category, json)
        }
        val ingredientJson = gson.toJson(ingredients.toTypedArray())
        editor.putString(INGREDIENT_PREFERENCES_ID, ingredientJson)
        editor.apply()
    }

    private fun loadUserData() {
        currentUser = authorization.currentUser
        loadIngredients()
    }

    private fun loadIngredients() {
        val uid = currentUser?.uid
        refreshDiscovery()
        val oldIngredients = ingredients.toTypedArray()
        if(uid != null) {
            ingredients.clear()
            fireStore.collection("app").document(uid)
                    .collection("ingredients").get().addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result.forEach { snapshot ->
                        parseSnapshot(snapshot)
                    }
                    if(!oldIngredients.toMutableList().containsAll(ingredients)) {
                        refreshDiscovery(true)
                    }
                }
            }
        }
    }

    fun refreshDiscovery(forceRefresh: Boolean = false) {
        if(ingredients.count() > 0) {
            if(forceRefresh || lastDiscoveryRefreshTime == -1L ||
                    Date().time - lastDiscoveryRefreshTime >= 60_000) {
                lastDiscoveryRefreshTime = Date().time
                val discovery = SearchDiscovery(HandleDiscovery(recipes, this))
                discovery.execute(ingredients.toTypedArray())
                Log.d("Discovery", "Refreshing Discovery...")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        showCurrentFragment()
    }

    fun showCurrentFragment() {
        when(tabIndex) {
            0 -> fragment = IngredientsTab()
            1 -> fragment = HomeTab()
            2 -> fragment = MyRecipesTab()
        }
        replaceFragment()
    }

    private fun replaceFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
        tabsItems?.get(tabIndex)?.isSelected = true
    }

    private fun parseSnapshot(snapshot: QueryDocumentSnapshot) {
        val name = snapshot.get("name") as? String
        val id = snapshot.id
        if(name != null) {
            documentsMap[name] = id
            val ingredient = Ingredient(name)
            ingredients.add(ingredient)
            ingredients.sortBy { it.name.toLowerCase().replace("\\s".toRegex(), "") }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.user_profile) {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

 class SearchDiscovery(private val inputListener: OnTaskCompleted):
         AsyncTask<Array<Ingredient>, Void, MutableList<DiscoveryRecipe>>() {

    override fun doInBackground(vararg args: Array<Ingredient>): MutableList<DiscoveryRecipe> {
        val ingredients = args[0]
        val orderedRecipes = PriorityQueue<DiscoveryRecipe>(10) { a, b ->
            when {
                a.queueValue < (b.queueValue) -> 1
                else -> -1
            }
        }
        val discovery = Discovery(VERSION_DIS, USERNAME_MIKE_DIS, PASSWORD_MIKE_DIS)
        discovery.endPoint = URL_MIKE_DIS

        val queryBuilder = QueryOptions.Builder(ENV_ID_MIKE_DIS, COL_ID_MIKE_DIS)

        queryBuilder.query(buildIngredientQuery(ingredients)).count(50)
        val queryResponse = discovery.query(queryBuilder.build()).execute()

        for (response in queryResponse.results) {
            val recipe = JSON.nonstrict.parse<DiscoveryRecipe>(response.toString())
            recipe.calculatePercentAvailable(ingredients)
            orderedRecipes.add(recipe)
        }
        orderedRecipes.filter {
            it.title != "" && it.ingredientList.count() > 0
        }
        return orderedRecipes.toMutableList()
    }

    override fun onPostExecute(result: MutableList<DiscoveryRecipe>){
        super.onPostExecute(result)
        inputListener.onTaskCompleted(result)
    }

    private fun buildIngredientQuery(ingredients: Array<Ingredient>): String{
        return ingredients.asSequence().filter { it.name != "" }
                .joinToString("|", "ingredientList:") { it.name }
    }
}

interface OnTaskCompleted {
    fun onTaskCompleted(recipes: MutableList<DiscoveryRecipe>)
}

class HandleDiscovery(private val overAllList: MutableList<MutableList<DiscoveryRecipe>>,
                      private val mainMenu: MainMenu?): OnTaskCompleted {

    override fun onTaskCompleted(recipes: MutableList<DiscoveryRecipe>) {
        overAllList[0].clear()
        overAllList[1].clear()
        overAllList[0].addAll(recipes)
        overAllList[1].addAll(recipes.shuffled().toMutableList())
        val fragment = mainMenu?.fragment
        if(fragment as? HomeTab != null) {
            fragment.refresh()
        } else if(fragment as? MyRecipesTab != null) {
            fragment.refresh()
        }
    }
}