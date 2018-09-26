package com.speakeasy.watsonbarassistant

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

class HomeRecipeAdapter(private val recipes: MutableList<DiscoveryRecipe>,
                        private val activity: Activity):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cardView = LayoutInflater.from(parent.context).inflate(R.layout.recipe_card,
                parent, false) as CardView
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindRecipeViewHolder(holder as ViewHolder, position)
    }

    private fun bindRecipeViewHolder(holder: ViewHolder, position: Int) {
        val mainLayout = holder.card.getChildAt(0) as RelativeLayout
        mainLayout.setOnClickListener {
            val intent = Intent(activity, RecipeDetail::class.java)
            intent.putExtra("Recipe", recipes[position])
            activity.startActivity(intent)
        }
        val recipe = recipes[position]
        val textView = mainLayout.getChildAt(1) as TextView
        setupImageView(mainLayout, recipe, textView)
        textView.text = recipe.title
    }

    private fun setupImageView(mainLayout: RelativeLayout, recipe: DiscoveryRecipe,
                               textView: TextView) {
        val imageView = mainLayout.getChildAt(0) as ImageView
        textView.setTypeface(textView.typeface, Typeface.BOLD)
        var bitmap = recipe.createBitMap()
        if(recipe.imageBase64 == DEFAULT_IMAGE_BASE64) {
            bitmap = BitmapFactory.decodeResource(activity.resources, R.mipmap.ic_alcohol)
        }
        imageView.setImageBitmap(bitmap)
    }

    override fun getItemCount(): Int =  recipes.count()
}
