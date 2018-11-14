package com.speakeasy.watsonbarassistant.adapter

import android.app.Activity
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.speakeasy.watsonbarassistant.Ingredient
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.loadIngredientImage
import java.util.*

class IngredientGridAdapter(private val ingredientsSet: TreeSet<Ingredient>, private val activity: Activity):
        RecyclerView.Adapter<IngredientGridAdapter.ViewHolder>() {


    private val ingredientsList: MutableList<Ingredient>
    get() {
        return ingredientsSet.toMutableList()
    }

    private var showDelete = false

    class ViewHolder(val layout: CardView) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.ingredient_grid_view,
                parent, false) as CardView
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardView = holder.layout as? CardView
        val relativeLayout = cardView?.getChildAt(0) as? RelativeLayout
        val textView = relativeLayout?.findViewById<TextView>(R.id.ingredientGridName)

        val ingredient = ingredientsList[position]
        textView?.text = ingredient.name.capitalize()

        val imageView = relativeLayout?.findViewById<SimpleDraweeView>(R.id.ingredientCard)
        imageView?.hierarchy?.setPlaceholderImage(R.mipmap.ic_cherry)

        imageView?.let{
            loadIngredientImage(activity, it, ingredient)
        }

        val deleteButton = relativeLayout?.findViewById<ImageButton>(R.id.ingredientGridDelete)
        if(showDelete) {
            deleteButton?.visibility = View.VISIBLE
            deleteButton?.setOnClickListener {
                removeAt(position)
                notifyDataSetChanged()
            }
            imageView?.setOnClickListener(null)
            imageView?.setOnClickListener {
                showDelete = false
                notifyDataSetChanged()
            }
        } else {
            deleteButton?.visibility = View.GONE
            imageView?.setOnClickListener(null)
            imageView?.setOnLongClickListener { _ ->
                showDelete = true
                notifyDataSetChanged()
                true
            }
        }


        imageView?.let {
            loadIngredientImage(activity.applicationContext, it, ingredient)
        }
    }

    override fun getItemCount(): Int = ingredientsSet.count()

    fun removeAt(position: Int) {
        val ingredient = ingredientsList[position]
        ingredientsSet.removeIf { it.name == ingredient.name }
        ingredientsSet.remove(ingredient)
        notifyDataSetChanged()
    }
}
