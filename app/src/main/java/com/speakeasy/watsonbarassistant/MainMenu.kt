package com.speakeasy.watsonbarassistant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
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
import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.UpdateManager
import com.ibm.watson.developer_cloud.discovery.v1.Discovery
import com.ibm.watson.developer_cloud.service.security.IamOptions
import com.ibm.watson.developer_cloud.discovery.v1.model.QueryResponse
import com.ibm.watson.developer_cloud.discovery.v1.model.QueryOptions
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.serialization.json.JSON
import java.util.*
import android.os.AsyncTask
import javax.security.auth.callback.Callback


const val ENV_ID_DIS_TEST = "system"
const val COL_ID_DIS_TEST = "news-en"

const val ENV_ID_DIS = "6f23a82f-ad96-4975-8ec3-539b9d4eb5d3"
const val COL_ID_DIS = "cae1b403-6449-4719-81f8-41091a59c04b"

const val ENDPOINT_DIS = "https://gateway-wdc.watsonplatform.net/discovery/api"
const val API_KEY_DIS = "p5C9aI_YYB_-IJnfDuatG5La3if5erc3bxQ8gsXtBh4E"

const val ENV_ID_MIKE_DIS = "04760902-0426-4f36-857e-37e9d7e09f5e"
const val COL_ID_MIKE_DIS = "aecd1f2c-6cab-4fa4-96cf-81d8c55bf181"

const val PASSWORD_MIKE_DIS = "BERRVZvxKgto"
const val USERNAME_MIKE_DIS = "539fdfc9-4579-4861-a1d2-74660add2ba6"

const val URL_MIKE_DIS = "https://gateway.watsonplatform.net/discovery/api"

const val VERSION_DIS = "2018-08-01"


const val USERNAME = "test@gmail.com"
const val PASSWORD = "test123"

class MainMenu : AppCompatActivity() {

    var ingredients = mutableListOf<Ingredient>()
    var recipes = mutableListOf<MutableList<DiscoveryRecipe>>()
    var homeCategories = mutableListOf<String>()
    var documentsMap = mutableMapOf<String, String>()
    var currentUser: FirebaseUser? = null
    var tabIndex = 0
    private var fragment: Fragment? = null

    private val fireStore = FirebaseFirestore.getInstance()
    private var authorization = FirebaseAuth.getInstance()

    init {
        homeCategories.add("Suggestions")
        homeCategories.add("Recently Viewed")
    }

    private fun addDefaultRecipes() {
        /*
        recipes.add(mutableListOf())
        recipes.add(mutableListOf())
        val bloodyMaryIngredients = arrayOf("Tabasco", "Salt", "3 parts Vodka", "Pepper",
                "Worcestershire Sauce", "6 parts Tomato Juice", "1 part Lemon Juice").asList()
        recipes[0].add(Recipe("Bloody Mary", R.mipmap.ic_bloody_mary, bloodyMaryIngredients))

        val mojitoIngredients = arrayOf("6 Leaves of Mint", "2 Teaspoons Sugar",
                "2 Parts White Rum", "1 oz. Fresh Lime Juice", "Soda Water").asList()
        recipes[0].add(Recipe("Mojito", R.mipmap.ic_mojito, mojitoIngredients))

        val oldFashionedIngredients = arrayOf("1 Sugar Cube", "2 Parts Bourbon",
                "Few Dashes Plain Water", "2 Dashes Angostura Bitters").asList()
        recipes[0].add(Recipe("Old Fashioned", R.mipmap.ic_old_fashioned, oldFashionedIngredients))

        val margaritaIngredients = arrayOf("1 oz Cointreau", "1 oz Lime Juice", "2 oz Tequila").asList()
        recipes[0].add(Recipe("Margarita", R.mipmap.ic_margarita, margaritaIngredients))
        recipes[0].reversed().forEach { recipes[1].add(it) }
        */

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


                    Log.i("Discovery", "Ingredients: " + ingredients.toString())
                    var discovery = SearchDiscovery(HandleDiscovery(recipes))
                    //var test = mutableListOf<Ingredient>()
                    //test.add(Ingredient("Vodka"))
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

class CompareLists {

    companion object : Comparator<DiscoveryRecipe> {

        override fun compare(a: DiscoveryRecipe, b: DiscoveryRecipe): Int = when {
            a.queueValue < b.queueValue -> 1
            else -> -1
        }
    }
}

 class SearchDiscovery(inputListener: OnTaskCompleted) : AsyncTask<List<Ingredient>, Void, MutableList<DiscoveryRecipe>>() {

     private var listener: OnTaskCompleted

     init {
         this.listener = inputListener
     }

    override fun doInBackground(vararg args: List<Ingredient>): MutableList<DiscoveryRecipe> {
        var ingredients = args[0]
        var orderedRecipes = PriorityQueue<DiscoveryRecipe>(10, CompareLists)

        Log.i("Discovery", "Start.")
        val discovery = Discovery(
                VERSION_DIS,
                USERNAME_MIKE_DIS,
                PASSWORD_MIKE_DIS
        )
        discovery.endPoint = URL_MIKE_DIS

        val queryBuilder = QueryOptions.Builder(ENV_ID_MIKE_DIS, COL_ID_MIKE_DIS)

        queryBuilder.query(buildIngredientQuery(ingredients)).count(50)
        val queryResponse = discovery.query(queryBuilder.build()).execute()

        Log.i("Discovery", "Serialization Start.")
        //Log.i("Discovery", "Response: " + queryResponse.results[0])
        for(response in queryResponse.results){
            val recipe = JSON.nonstrict.parse<DiscoveryRecipe>(response.toString())
            if(recipe.imgageBase64 == ""){
                Log.i("Discovery","Failed: " + recipe.title + ": " + response.toString() + "\n")
            }
            recipe.calculatePercentAvailable(ingredients)
            orderedRecipes.add(recipe)
        }

        Log.i("Discovery", "Serialization Finished.")

        return orderedRecipes.toMutableList()
    }

    protected fun onProgressUpdate(vararg progress: Int) {
    }

    override fun onPostExecute(result: MutableList<DiscoveryRecipe>){
        super.onPostExecute(result)

        this.listener.onTaskCompleted(result)
    }

    private fun buildIngredientQuery(ingredients: List<Ingredient>): String{
        val queryString = "ingredientList:"
        val ingredients = ingredients
        val sb = StringBuilder()
        sb.append(queryString)

        for (ingredient in ingredients){
            if(ingredient.name != "") {
                sb.append(ingredient.name)
                sb.append("|")
            }
        }

        sb.replace(sb.length-1,sb.length, "")
        Log.i("Discovery", "Ingredient Query: " + sb.toString())
        return sb.toString()
    }
}

interface OnTaskCompleted {
    fun onTaskCompleted(recipes: MutableList<DiscoveryRecipe>)
}

class HandleDiscovery(overAllList: MutableList<MutableList<DiscoveryRecipe>>): OnTaskCompleted{
    private var list = mutableListOf<MutableList<DiscoveryRecipe>>()
    init{
        this.list = overAllList
    }
    override fun onTaskCompleted(recipes: MutableList<DiscoveryRecipe>) {
        this.list.add(0, recipes)
        this.list.add(1, recipes)

        HomeTab().refresh()

        Log.i("Discovery", "TEST")
    }
}