package com.speakeasy.watsonbarassistant

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_my_favorites_tab.*


class MyFavoritesTab : Fragment() {

    private var viewAdapter: MyRecipeAdapter? = null
    private var manager: LinearLayoutManager? = null
    private var favorites = mutableListOf<DiscoveryRecipe>()
    get() = BarAssistant.favoritesList.toMutableList()

    companion object {
        var lastScrolledPosition: Int = 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):View? {
        super.onCreateView(inflater, container, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Favorites"
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
        viewAdapter = MyRecipeAdapter(favorites, mainMenu)

        favorites_list.apply {
            setHasFixedSize(true)
            layoutManager = manager
            adapter = viewAdapter
        }

        manager?.scrollToPosition(lastScrolledPosition)

        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        favorites_list?.addItemDecoration(itemDecorator)

        setupOnClickListener()
        refresh()
    }

    private fun setupOnClickListener() {
        favorites_list?.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val intent = Intent(activity, RecipeDetail::class.java)
                val favoritesList = BarAssistant.favoritesList
                intent.putExtra("Recipe", favoritesList.toMutableList()[position])
                startActivity(intent)
            }
        })
    }

    fun refresh() {
        viewAdapter?.notifyDataSetChanged()
    }
}