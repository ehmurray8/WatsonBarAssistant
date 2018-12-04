package com.speakeasy.watsonbarassistant.fragment


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.activity.VisionActivity
import com.speakeasy.watsonbarassistant.adapter.IngredientGridAdapter
import com.speakeasy.watsonbarassistant.extensions.addIngredientHandlers
import com.speakeasy.watsonbarassistant.extensions.addMainIngredients
import com.speakeasy.watsonbarassistant.extensions.closeIngredientRadial
import com.speakeasy.watsonbarassistant.extensions.openIngredientRadial
import kotlinx.android.synthetic.main.fragment_ingredient_tab_radial.*
import kotlinx.android.synthetic.main.ingredients_radial_overlay.*


class IngredientsTab : Fragment() {

    private lateinit var menuAnimOpen: Animation
    private lateinit var menuAnimClose: Animation
    private lateinit var menuAnimRotateOut: Animation
    private lateinit var menuAnimRotateBack: Animation

    private var isAddMenuOpen: Boolean = false
    private var viewAdapter: IngredientGridAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Ingredients"
        return inflater.inflate(R.layout.fragment_ingredient_tab_radial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewManager = GridLayoutManager(activity, 2)
        viewAdapter = IngredientGridAdapter(synchronized(BarAssistant.ingredients) { BarAssistant.ingredients }, activity as Activity)

        ingredients_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        menuAnimOpen = AnimationUtils.loadAnimation(context, R.anim.menu_anim_open)
        menuAnimClose = AnimationUtils.loadAnimation(context, R.anim.menu_anim_close)
        menuAnimRotateOut = AnimationUtils.loadAnimation(context, R.anim.menu_anim_rotate_out)
        menuAnimRotateBack = AnimationUtils.loadAnimation(context, R.anim.menu_anim_rotate_back)

        addMenuButton.setOnClickListener {
            when {
                isAddMenuOpen -> closeMenus()
                else -> openMenus()
            }
        }

        activity?.addIngredientHandlers(::addMainIngredients)

        addViaCameraButton.setOnClickListener {
            val intent = Intent(activity, VisionActivity::class.java)
            startActivity(intent)
        }

        closeMenus()
        refresh()
    }


    fun refresh() {
        activity?.runOnUiThread {
            viewAdapter?.notifyDataSetChanged()
        }
    }

    private fun closeMenus(){
        barrier.visibility=View.GONE
        addMenuButton.startAnimation(menuAnimRotateBack)
        addViaCameraButton.startAnimation(menuAnimClose)
        addViaCameraButton.isClickable = false
        addViaCameraButton.hide()
        activity?.closeIngredientRadial()
        ingredients_recycler_view.alpha = 1.0F
        isAddMenuOpen = false
    }

    private fun openMenus(){
        barrier.visibility=View.VISIBLE
        barrier.setOnClickListener{
            closeMenus()
        }
        addMenuButton.startAnimation(menuAnimRotateOut)
        addViaCameraButton.startAnimation(menuAnimOpen)
        addViaCameraButton.isClickable = true
        addViaCameraButton.show()
        ingredientsTabRadial.bringToFront()
        activity?.openIngredientRadial()
        ingredients_recycler_view.alpha = 0.4F
        isAddMenuOpen = true
    }
}
