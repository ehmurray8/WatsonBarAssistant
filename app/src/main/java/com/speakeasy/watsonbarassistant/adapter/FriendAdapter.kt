package com.speakeasy.watsonbarassistant.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.speakeasy.watsonbarassistant.R

class FriendAdapter: RecyclerView.Adapter<FriendAdapter.ViewHolder>() {

    class ViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.ingredient_view,
                parent, false) as LinearLayout
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    override fun getItemCount(): Int = 1
}
