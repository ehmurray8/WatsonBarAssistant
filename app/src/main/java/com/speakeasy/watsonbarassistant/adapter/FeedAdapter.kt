package com.speakeasy.watsonbarassistant.adapter

import android.app.Activity
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.activity.RecipeDetail
import com.speakeasy.watsonbarassistant.loadImage


class FeedAdapter(private var activity: Activity): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class FeedViewHolder(val layout: ConstraintLayout): RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.large_recipe_card,
                parent, false) as ConstraintLayout
        return FeedViewHolder(layout)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindFeedViewHolder(holder as FeedViewHolder, position)
    }

    private fun bindFeedViewHolder(holder: FeedViewHolder?, position: Int) {
        val layout = holder?.layout
        val cardView  = layout?.getChildAt(0) as? CardView
        val relativeLayout = cardView?.getChildAt(0) as? ConstraintLayout

        val title = relativeLayout?.getChildAt(0) as? TextView
        val imageView = relativeLayout?.getChildAt(1) as? SimpleDraweeView
        val description = relativeLayout?.getChildAt(2) as? TextView

        val element = synchronized(BarAssistant.feed) {
            BarAssistant.feed[position]
        }

        cardView?.setOnClickListener {
            val intent = Intent(activity, RecipeDetail::class.java)
            intent.putExtra("Recipe", element.recipe)
            activity.startActivity(intent)
        }

        title?.text = element.recipe.title
        if(imageView != null) {
            imageView.hierarchy.setPlaceholderImage(R.mipmap.ic_old_fashioned)
            loadImage(activity.applicationContext, imageView, element.recipe)
        }

        description?.text = "Test"
    }

    override fun getItemCount() = synchronized(BarAssistant.feed) { BarAssistant.feed.count() }
}
