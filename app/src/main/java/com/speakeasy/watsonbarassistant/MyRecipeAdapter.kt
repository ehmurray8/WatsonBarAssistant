package com.speakeasy.watsonbarassistant

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_recipe_detail.view.*

class MyRecipeAdapter(private var recipes: MutableList<DiscoveryRecipe>, private val activity: Activity):RecyclerView.Adapter<MyRecipeAdapter.ViewHolder>(){

        class ViewHolder(val layout: ConstraintLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRecipeAdapter.ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.my_recipe_view2,parent,false) as ConstraintLayout
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val image = holder.layout.getChildAt(0) as ImageView
        val name = holder.layout.getChildAt(2) as TextView
        val description = holder.layout.getChildAt(1) as TextView

        image.setImageBitmap(recipes[position].createBitMap())
        name.text = recipes[position].title
        description.text = recipes[position].description

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemCount(): Int = recipes.count()

}

