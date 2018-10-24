package com.speakeasy.watsonbarassistant

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.lastViewedTimes
import kotlinx.android.synthetic.main.activity_recipe_collection.*

class MyFavoritesTab : Fragment() {

    private var viewAdapter: MyFavoritesAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var manager: LinearLayoutManager? = null

    private var authorization = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()
    internal val favoritesList = mutableListOf<DiscoveryRecipe?>()

    companion object {
        var lastScrolledPosition: Int = 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):View? {
        return inflater.inflate(R.layout.activity_recipe_collection, container, false)
    }

    override fun onStop() {
        super.onStop()
        lastScrolledPosition = manager?.findFirstVisibleItemPosition() ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        var favorites = BarAssistant.favorites
        //favoritesList =
        manager = LinearLayoutManager(activity?.baseContext)
        val mainMenu = activity as MainMenu
        //viewAdapter = MyRecipeAdapter(favoritesList, mainMenu)

        favorites_collection_list.apply {
            setHasFixedSize(true)
            layoutManager = manager
            adapter = viewAdapter
        }

        manager?.scrollToPosition(lastScrolledPosition)

        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        recyclerView?.addItemDecoration(itemDecorator)

        convertIdListToDiscoveryRecipeList(favorites)
        setupOnClickListener()
    }

    private fun setupOnClickListener() {
        //var favoritesList =
        recyclerView?.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val intent = Intent(activity, RecipeDetail::class.java)
                //intent.putExtra("Favorite", favoritesList[position])
                startActivity(intent)
            }
        })
    }

    private fun convertIdListToDiscoveryRecipeList(favorites: MutableList<String>) {

        favoritesList.clear()
        favorites?.forEach { _ -> favoritesList.add(null) }
        //var count = 0
        favorites?.forEachIndexed { index, recipeId ->
            if (recipeId != null) {
                fireStore.collection(RECIPE_COLLECTION).document(recipeId.toString())
                        .get().addOnCompleteListener { snapShotRecipe ->
                            if (snapShotRecipe.isSuccessful) {
                                val recipeDocument = snapShotRecipe.result
                                        ?: return@addOnCompleteListener
                                //favoritesList.add(snapShotRecipe)
                            }
                        }
            }
        }
    }

    fun refresh() {
        viewAdapter?.notifyDataSetChanged()
    }
}