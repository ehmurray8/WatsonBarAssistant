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
import kotlinx.android.synthetic.main.fragment_my_favorites_tab.*

class MyFavoritesTab : Fragment() {

    private var viewAdapter: MyRecipeAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var manager: LinearLayoutManager? = null

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
        viewAdapter = MyRecipeAdapter(mainMenu.recipes[0], mainMenu.assets)

        recyclerView = recipes_list.apply {
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
                val ingredients = (activity as MainMenu).ingredients
                intent.putExtra("Ingredients", ingredients[0])
                startActivity(intent)
            }
        })
    }

    fun refresh() {
        viewAdapter?.notifyDataSetChanged()
    }
}