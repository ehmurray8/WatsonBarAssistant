package com.speakeasy.watsonbarassistant

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import com.algolia.search.saas.Client
import com.algolia.search.saas.Query
import com.algolia.search.saas.android.BuildConfig.ALGOLIA_API_KEY
import com.algolia.search.saas.android.BuildConfig.ALGOLIA_APPLICATION_ID
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.serialization.json.JSON

class SearchActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private val client = Client(ALGOLIA_APPLICATION_ID, ALGOLIA_API_KEY)
    private val recipeIndex = client.getIndex("Recipe")
    private var searchMenuItem: MenuItem? = null
    private var searchView: SearchView? = null
    private var recyclerView: RecyclerView? = null
    private var viewAdapter: MyRecipeAdapter? = null
    private val searchRecipes: MutableList<DiscoveryRecipe> = mutableListOf()
    private val fireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.title = "Search Recipes"
        val manager = LinearLayoutManager(baseContext)
        viewAdapter = MyRecipeAdapter(searchRecipes, this)

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
            }

            val response = content.getJSONArray("hits")
            searchRecipes.clear()
            for (i in 0 until response.length()) {
                val recipe = JSON.nonstrict.parse<DiscoveryRecipe>(response.getJSONObject(i).toString())
                searchRecipes.add(recipe)
            }

            runOnUiThread {
                viewAdapter?.notifyDataSetChanged()
            }
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }
}
