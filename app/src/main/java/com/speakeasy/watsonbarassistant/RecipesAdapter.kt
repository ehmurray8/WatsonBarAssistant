package com.speakeasy.watsonbarassistant

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

class RecipesAdapter(private var dataSet: MutableList<DiscoveryRecipe>,
                     private val activity: Activity):
        RecyclerView.Adapter<RecipesAdapter.ViewHolder>() {

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipesAdapter.ViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.recipe_view,
                parent, false) as TextView
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = dataSet[position].title
        var bitmap = dataSet[position].createBitMap()
        if(dataSet[position].imageBase64 == DEFAULT_IMAGE_BASE64) {
            bitmap = BitmapFactory.decodeResource(activity.resources,R.mipmap.ic_alcohol)
        }
        val bitmapDrawable = BitmapDrawable(activity.resources, bitmap)
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(bitmapDrawable, null, null, null)
    }

    override fun getItemCount(): Int = dataSet.count()
}
