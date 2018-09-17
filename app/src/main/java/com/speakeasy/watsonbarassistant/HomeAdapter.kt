package com.speakeasy.watsonbarassistant

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView


class HomeAdapter(private var dataSet: MutableList<MutableList<Recipe>>,
                  private var categories: MutableList<String>, private var activity: Activity):
        RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    class ViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapter.ViewHolder {
        val linearLayout = LayoutInflater.from(parent.context).inflate(R.layout.home_recipe_category,
                parent, false) as LinearLayout
        return ViewHolder(linearLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val layout = holder.layout
        val headerView = layout.getChildAt(0) as TextView
        headerView.text = categories[position]
        val recipesList = layout.getChildAt(1) as RecyclerView

        val viewManager = LinearLayoutManager(activity.applicationContext, LinearLayoutManager.HORIZONTAL, false)
        val viewAdapter = HomeRecipeAdapter(dataSet[position], activity)

        recipesList.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    override fun getItemCount(): Int = dataSet.count()
}
