package com.speakeasy.watsonbarassistant

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

class HomeRecipeAdapter(private val recipes: MutableList<Recipe>,
                        private val activity: Activity):
        RecyclerView.Adapter<HomeRecipeAdapter.ViewHolder>() {

    class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecipeAdapter.ViewHolder {
        val cardView = LayoutInflater.from(parent.context).inflate(R.layout.recipe_card,
                parent, false) as CardView
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mainLayout = holder.card.getChildAt(0) as RelativeLayout
        mainLayout.setOnClickListener {
            val intent = Intent(activity, RecipeDetail::class.java)
            intent.putExtra("Recipe", recipes[position])
            activity.startActivity(intent)
        }
        val imageView = mainLayout.getChildAt(0) as ImageView

        val recipe = recipes[position]

        val imageId = recipe.imageId
        val drinkBitmap = BitmapFactory.decodeResource(activity.resources, imageId)
        imageView.setImageBitmap(drinkBitmap)

        val textView = mainLayout.getChildAt(1) as TextView
        textView.text = recipe.name
    }

    override fun getItemCount(): Int {
        return recipes.size
    }
}
