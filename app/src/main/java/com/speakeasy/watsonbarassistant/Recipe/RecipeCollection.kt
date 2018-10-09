package com.speakeasy.watsonbarassistant.Recipe

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.speakeasy.watsonbarassistant.OnItemClickListener
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.addOnItemClickListener
import kotlinx.android.synthetic.main.activity_recipe_collection.*

class RecipeCollection : AppCompatActivity() {

    private var collectionName: String? = null
    private var recyclerView: RecyclerView? = null

    companion object Recipes {
        var recipesList: MutableList<DiscoveryRecipe> = mutableListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_collection)

        collectionName = intent.getStringExtra("Collection Name")
        recipe_collection_title.text = collectionName
        setupListView()
    }

    private fun setupListView() {
        val viewManager = LinearLayoutManager(this)
        val viewAdapter = MyRecipeAdapter(recipesList, assets)

        recyclerView = recipes_collection_list.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        recyclerView?.addItemDecoration(itemDecorator)

        setupOnClickListener()
    }

    private fun setupOnClickListener() {
        recyclerView?.addOnItemClickListener(object: OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val intent = Intent(applicationContext, RecipeDetail::class.java)
                val recipe = recipesList[position]
                intent.putExtra("Recipe", recipe)
                startActivity(intent)
            }
        })
    }
}
