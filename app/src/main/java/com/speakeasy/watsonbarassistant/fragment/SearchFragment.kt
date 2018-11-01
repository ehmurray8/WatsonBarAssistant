package com.speakeasy.watsonbarassistant.fragment


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.activity.RecipeDetail
import com.speakeasy.watsonbarassistant.adapter.RecipeAdapter
import com.speakeasy.watsonbarassistant.extensions.OnItemClickListener
import com.speakeasy.watsonbarassistant.extensions.addOnItemClickListener
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var searchAdapter: RecipeAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val manager = LinearLayoutManager(activity?.applicationContext)
        searchAdapter = RecipeAdapter(BarAssistant.searchRecipes, activity as Activity)

        recyclerView = searchRecipeResults.apply {
            setHasFixedSize(true)
            layoutManager = manager
            adapter = searchAdapter
        }

        val itemDecorator = DividerItemDecoration(activity?.applicationContext, DividerItemDecoration.VERTICAL)
        recyclerView?.addItemDecoration(itemDecorator)
        setupOnClickListener()
    }

    private fun setupOnClickListener() {
        recyclerView?.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val intent = Intent(activity?.applicationContext, RecipeDetail::class.java)
                val recipe = synchronized(BarAssistant.searchRecipes) {
                    BarAssistant.searchRecipes[position]
                }
                intent.putExtra("Recipe", recipe)
                startActivity(intent)
            }
        })
    }

    fun refresh() {
        searchAdapter?.notifyDataSetChanged()
    }
}
