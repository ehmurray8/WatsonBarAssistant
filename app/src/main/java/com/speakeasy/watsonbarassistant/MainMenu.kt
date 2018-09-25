package com.speakeasy.watsonbarassistant

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.ibm.watson.developer_cloud.discovery.v1.Discovery
import com.ibm.watson.developer_cloud.discovery.v1.model.QueryOptions
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.serialization.json.JSON
import java.util.*


const val ENV_ID_MIKE_DIS = "04760902-0426-4f36-857e-37e9d7e09f5e"
const val COL_ID_MIKE_DIS = "aecd1f2c-6cab-4fa4-96cf-81d8c55bf181"
const val PASSWORD_MIKE_DIS = "BERRVZvxKgto"
const val USERNAME_MIKE_DIS = "539fdfc9-4579-4861-a1d2-74660add2ba6"
const val URL_MIKE_DIS = "https://gateway.watsonplatform.net/discovery/api"
const val VERSION_DIS = "2018-08-01"


class MainMenu : AppCompatActivity() {

    var ingredients = mutableListOf<Ingredient>()
    var recipes = mutableListOf<MutableList<DiscoveryRecipe>>()
    var homeCategories = mutableListOf<String>()
    private var documentsMap = mutableMapOf<String, String>()
    var currentUser: FirebaseUser? = null
    var tabIndex = 0
    var fragment: Fragment? = null

    private val fireStore = FirebaseFirestore.getInstance()
    private var authorization = FirebaseAuth.getInstance()

    init {
        recipes.add(0, mutableListOf())
        recipes.add(1, mutableListOf())
        homeCategories.add("Suggestions")
        homeCategories.add("Recently Viewed")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadUserData()
        setContentView(R.layout.activity_main_menu)
        tabs.addOnTabSelectedListener(MainMenuTabListener(this))
        setSupportActionBar(toolbar as Toolbar)
    }

    private fun loadUserData() {
        currentUser = authorization.currentUser
        loadIngredients()
    }

    fun loadIngredients() {
        val uid = currentUser?.uid
        ingredients.clear()
        if(uid != null) {
            fireStore.collection("app").document(uid)
                    .collection("ingredients").get().addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result.forEach { snapshot ->
                        parseSnapshot(snapshot)
                    }
                    val discovery = SearchDiscovery(HandleDiscovery(recipes, this))
                    discovery.execute(ingredients)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        showCurrentFragment()
    }

    fun showCurrentFragment() {
        when(tabIndex) {
            0 -> fragment = HomeTab()
            1 -> fragment = AddTab()
            2 -> fragment = RecipesTab()
        }
        replaceFragment()
    }

    private fun replaceFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    private fun parseSnapshot(snapshot: QueryDocumentSnapshot) {
        val name = snapshot.get("name") as? String
        val id = snapshot.id
        if(name != null) {
            documentsMap[name] = id
            val ingredient = Ingredient(name)
            ingredients.add(ingredient)
            ingredients.sortBy {
                it.name
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
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
         AsyncTask<List<Ingredient>, Void, MutableList<DiscoveryRecipe>>() {

    override fun doInBackground(vararg args: List<Ingredient>): MutableList<DiscoveryRecipe> {
        val ingredients = args[0]
        val orderedRecipes = PriorityQueue<DiscoveryRecipe>(10) { a, b ->
            when {
                a.queueValue < b.queueValue -> 1
                else -> -1
            }
        }

        val discovery = Discovery(VERSION_DIS, USERNAME_MIKE_DIS, PASSWORD_MIKE_DIS)
        discovery.endPoint = URL_MIKE_DIS

        val queryBuilder = QueryOptions.Builder(ENV_ID_MIKE_DIS, COL_ID_MIKE_DIS)

        queryBuilder.query(buildIngredientQuery(ingredients)).count(50)
        val queryResponse = discovery.query(queryBuilder.build()).execute()

        for(response in queryResponse.results){
            val recipe = JSON.nonstrict.parse<DiscoveryRecipe>(response.toString())
            recipe.calculatePercentAvailable(ingredients)
            orderedRecipes.add(recipe)
        }
        return orderedRecipes.toMutableList()
    }

    override fun onPostExecute(result: MutableList<DiscoveryRecipe>){
        super.onPostExecute(result)
        inputListener.onTaskCompleted(result)
    }

    private fun buildIngredientQuery(ingredients: List<Ingredient>): String{
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
        overAllList[0].addAll(recipes)
        overAllList[1].addAll(recipes.shuffled().toMutableList())
        val fragment = mainMenu?.fragment
        if(fragment as? HomeTab != null) {
            fragment.refresh()
        } else if(fragment as? RecipesTab != null) {
            fragment.refresh()
        }
    }
}