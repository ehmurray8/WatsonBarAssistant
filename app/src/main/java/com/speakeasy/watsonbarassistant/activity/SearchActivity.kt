package com.speakeasy.watsonbarassistant.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.algolia.search.saas.Client
import com.algolia.search.saas.Query
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.*
import com.speakeasy.watsonbarassistant.adapter.RecipeAdapter
import com.speakeasy.watsonbarassistant.extensions.OnItemClickListener
import com.speakeasy.watsonbarassistant.extensions.addOnItemClickListener
import com.speakeasy.watsonbarassistant.extensions.toast
import com.speakeasy.watsonbarassistant.DiscoveryRecipe
import com.speakeasy.watsonbarassistant.FireStoreRecipe
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.serialization.json.JSON

class SearchActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private val client = Client(ALGOLIA_APP_ID, ALGOLIA_API_KEY)
    private val recipeIndex = client.getIndex("Recipe")
    private var searchMenuItem: MenuItem? = null
    private var searchView: SearchView? = null
    private var recyclerView: RecyclerView? = null
    private var viewAdapter: RecipeAdapter? = null
    private val searchRecipes: MutableList<DiscoveryRecipe> = mutableListOf()
    private val fireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.title = "Search Recipes"
        val manager = LinearLayoutManager(baseContext)
        viewAdapter = RecipeAdapter(searchRecipes, this)

        recyclerView = searchResults.apply {
            setHasFixedSize(true)
            layoutManager = manager
            adapter = viewAdapter
        }

        val itemDecorator = DividerItemDecoration(baseContext, DividerItemDecoration.VERTICAL)
        recyclerView?.addItemDecoration(itemDecorator)
        setupOnClickListener()
    }

    private fun setupOnClickListener() {
        recyclerView?.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                if (searchView?.isShown != null) {
                    searchMenuItem?.collapseActionView()
                    searchView?.setQuery("", false)
                }
                val intent = Intent(baseContext, RecipeDetail::class.java)
                intent.putExtra("Recipe", searchRecipes[position])
                startActivity(intent)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as? SearchManager
        searchMenuItem = menu?.findItem(R.id.search)
        searchView = searchMenuItem?.actionView as? SearchView
        searchView?.setSearchableInfo(searchManager?.getSearchableInfo(componentName))
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)

        val randomMenuItem = menu?.findItem(R.id.randomRecipe)
        randomMenuItem?.setOnMenuItemClickListener {
            getRandomRecipe()
            true
        }
        return true
    }

    private fun getRandomRecipe() {
        fireStore.collection(RECIPE_COLLECTION).document(getRandom()).get().addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val recipe = task.result?.toObject(FireStoreRecipe::class.java)
                    val discoveryRecipe = recipe?.toDiscoveryRecipe()
                    if(discoveryRecipe?.title != "") {
                        val intent = Intent(baseContext, RecipeDetail::class.java)
                        intent.putExtra("Recipe", discoveryRecipe)
                        startActivity(intent)
                    } else {
                        getRandomRecipe()
                    }
                } else {
                    getRandomRecipe()
                }
            }
    }

    private fun getRandom(): String {
        return (1..1038).shuffled().first().toString()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        recipeIndex.searchAsync(Query(query)) { content, error ->
            if(error != null) {
                Log.d("Algolia", "Error Code: ${error.statusCode}, Message: ${error.message}")
                applicationContext.toast("Failed to retrieve any results.")
            } else if(content != null) {
                val response = content.getJSONArray("hits")
                if(response.length() == 0) {
                    applicationContext.toast("No results for $query.")
                }
                searchRecipes.clear()
                for (i in 0 until response.length()) {
                    val recipe = JSON.nonstrict.parse<DiscoveryRecipe>(response.getJSONObject(i).toString())
                    searchRecipes.add(recipe)
                }

                runOnUiThread {
                    viewAdapter?.notifyDataSetChanged()
                }
            }
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }
}