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
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.favorites
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.favoritesList
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.recipes
import kotlinx.android.synthetic.main.fragment_my_favorites_tab.*


class MyFavoritesTab : Fragment() {

    private var viewAdapter: MyFavoritesAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var manager: LinearLayoutManager? = null

    private var fireStore = FirebaseFirestore.getInstance()

    companion object {
        var lastScrolledPosition: Int = 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):View? {
        return inflater.inflate(R.layout.fragment_my_favorites_tab, container, false)
    }

    override fun onStop() {
        super.onStop()
        lastScrolledPosition = manager?.findFirstVisibleItemPosition() ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        manager = LinearLayoutManager(activity?.baseContext)
        val mainMenu = activity as MainMenu

        convertIdListToDiscoveryRecipeList(BarAssistant.favorites)

        viewAdapter = MyFavoritesAdapter(BarAssistant.favoritesList, mainMenu)

        favorites_list.apply {
            setHasFixedSize(true)
            layoutManager = manager
            adapter = viewAdapter
        }

        manager?.scrollToPosition(lastScrolledPosition)

        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        recyclerView?.addItemDecoration(itemDecorator)

        setupOnClickListener()
    }

    private fun setupOnClickListener() {

        recyclerView?.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val intent = Intent(activity, RecipeDetail::class.java)
                val favoritesList = BarAssistant.favoritesList
                intent.putExtra("Favorite", favoritesList[position])
                startActivity(intent)

            }
        })
    }

    private fun convertIdListToDiscoveryRecipeList(favorites: MutableList<String>) {


        favoritesList.clear()
        favorites.forEach { _ -> favoritesList.add(null) }
        favorites.forEachIndexed { index, recipeId ->
            //Toast.makeText(context, "Bing! ${index}.", Toast.LENGTH_SHORT).show()
            fireStore.collection(RECIPE_COLLECTION).document(recipeId.substringBefore('.')).get().addOnCompleteListener { snapShotRecipe ->
            if (snapShotRecipe.isSuccessful) {
                val recipeDocument = snapShotRecipe.result ?: return@addOnCompleteListener
                addFavoritesRecipe(recipeDocument, index)
            }
        }
    }
}

    private fun addFavoritesRecipe(recipeDocument: DocumentSnapshot, index: Int) {
        val favorite = recipeDocument.toObject(FireStoreRecipe::class.java)
        favoritesList[index] = favorite!!.toDiscoveryRecipe()
        //Toast.makeText(context, "Added ${favoritesList[index]?.title}.", Toast.LENGTH_SHORT).show()

    }

    fun refresh() {
        viewAdapter?.notifyDataSetChanged()
    }
}