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
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.serialization.json.JSON

class SearchActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private val client = Client(ALGOLIA_APP_ID, ALGOLIA_API_KEY)
    private val recipeIndex = client.getIndex("Recipe")
    private var searchMenuItem: MenuItem? = null
    private var searchView: SearchView? = null
    private var recyclerView: RecyclerView? = null
    private var viewAdapter: MyRecipeAdapter? = null
    private val searchRecipes: MutableList<DiscoveryRecipe> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(toolbar as Toolbar)
        (toolbar as? Toolbar)?.title = "Search Recipes"
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
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        recipeIndex.searchAsync(Query(query)) { content, error ->
            if(error != null) {
                Log.d("Algolia", "Error Code: ${error.statusCode}, Message: ${error.message}")
            }

            runOnUiThread {
                val response = content.getJSONArray("hits")
                searchRecipes.clear()
                for (i in 0 until response.length()) {
                    val recipe = JSON.nonstrict.parse<DiscoveryRecipe>(response.getJSONObject(i).toString())
                    searchRecipes.add(recipe)
                }
                viewAdapter?.notifyDataSetChanged()
                Log.d("Algolia", "Recipe Count: ${searchRecipes.count()}")
            }
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }
}
