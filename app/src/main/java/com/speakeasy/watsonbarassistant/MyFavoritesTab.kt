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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_recipe_collection.*
import kotlinx.android.synthetic.main.fragment_my_favorites_tab.*
import java.util.ArrayList

class MyFavoritesTab : Fragment() {

    private var viewAdapter: MyRecipeAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var manager: LinearLayoutManager? = null

    private var authorization = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()

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
        var favoritesList = BarAssistant.favoritesRecipes
        manager = LinearLayoutManager(activity?.baseContext)
        val mainMenu = activity as MainMenu
        viewAdapter = MyRecipeAdapter(favoritesList, mainMenu)

        favorites_collection_list.apply {
            setHasFixedSize(true)
            layoutManager = manager
            adapter = viewAdapter
        }

        manager?.scrollToPosition(lastScrolledPosition)

        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        recyclerView?.addItemDecoration(itemDecorator)

        //loadFromFireStore()
        setupOnClickListener()
    }

    private fun setupOnClickListener() {
        var favoritesList = BarAssistant.favoritesRecipes
        recyclerView?.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val intent = Intent(activity, RecipeDetail::class.java)
                intent.putExtra("Favorite", favoritesList[position])
                startActivity(intent)
            }
        })
    }

    /*private fun loadFromFireStore() {
        val uid = authorization.currentUser?.uid
        if(uid != null) {
            fireStore.collection(MAIN_COLLECTION).document(uid).collection(SHOPPING_CART_COLLECTION)
                    .document("main").get().addOnSuccessListener {
                        val favorites = it.get(FAVORITES_COLLECTION) as? ArrayList<*>

                        favorites?.forEachIndexed { i, element ->
                            val favorite = DiscoveryRecipe(element as String)
                            if(!favoritesList.contains(favorite)) {
                                favoritesList.add(favorite)
                            }
                        }
                        viewAdapter?.notifyDataSetChanged()
                    }
        }
    }*/

    fun refresh() {
        viewAdapter?.notifyDataSetChanged()
    }
}