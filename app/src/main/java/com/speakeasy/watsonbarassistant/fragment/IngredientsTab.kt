package com.speakeasy.watsonbarassistant.fragment


import android.app.Activity
import android.content.Context
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
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.currentIndex
import com.speakeasy.watsonbarassistant.Ingredient
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.activity.IngredientAdd
import com.speakeasy.watsonbarassistant.activity.MainMenu
import com.speakeasy.watsonbarassistant.activity.VisionActivity
import com.speakeasy.watsonbarassistant.adapter.IngredientGridAdapter
import com.speakeasy.watsonbarassistant.extensions.addIngredient
import kotlinx.android.synthetic.main.fragment_ingredient_tab_radial.*



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

        alcoholsButton.setOnClickListener {
            currentIndex = 0
            showIngredientAdd()
        }
        beveragesAndFlavoringButton.setOnClickListener {
            currentIndex = 1
            showIngredientAdd()
        }
        bittersButton.setOnClickListener {
            currentIndex = 2
            showIngredientAdd()
        }
        fruitsAndVegetablesButton.setOnClickListener {
            currentIndex = 3
            showIngredientAdd()
        }
        herbsAndSpicesButton.setOnClickListener {
            currentIndex = 4
            showIngredientAdd()
        }
        juicesButton.setOnClickListener {
            currentIndex = 5
            showIngredientAdd()
        }
        liqueursButton.setOnClickListener {
            currentIndex = 6
            showIngredientAdd()
        }
        miscellaneousButton.setOnClickListener {
            currentIndex = 7
            showIngredientAdd()
        }
        sweetsButton.setOnClickListener {
            currentIndex = 8
            showIngredientAdd()
        }
        syrupsButton.setOnClickListener {
            currentIndex = 9
            showIngredientAdd()
        }

        addViaCameraButton.setOnClickListener {
            val intent = Intent(activity, VisionActivity::class.java)
            startActivity(intent)
        }

        refresh()
    }

    private fun showIngredientAdd() {
        IngredientAdd.addIngredientFunc = this::addIngredients
        val intent = Intent(activity, IngredientAdd::class.java)
        startActivity(intent)
    }

    private fun addIngredients(ingredients: MutableList<Ingredient>, context: Context) {
        ingredients.forEachIndexed { index, it ->
            addIngredient(newIngredient = it, refreshDiscovery = index == ingredients.count() - 1, context = context)
        }
    }

    fun refresh() {
        activity?.runOnUiThread {
            viewAdapter?.notifyDataSetChanged()
        }
    }

    private fun closeMenus(){
        addMenuButton.startAnimation(menuAnimRotateBack)
        addViaCameraButton.startAnimation(menuAnimClose)
        addViaCameraButton.isClickable = false
        addViaCameraButton.hide()
        alcoholsLayout.visibility = View.GONE
        alcoholsButton.isClickable = false
        alcoholsButton.hide()
        beveragesAndFlavoringLayout.visibility = View.GONE
        beveragesAndFlavoringButton.isClickable = false
        beveragesAndFlavoringButton.hide()
        bittersLayout.visibility = View.GONE
        bittersButton.isClickable = false
        bittersButton.hide()
        fruitsAndVegetablesLayout.visibility = View.GONE
        fruitsAndVegetablesButton.isClickable = false
        fruitsAndVegetablesButton.hide()
        herbsAndSpicesButtonLayout.visibility = View.GONE
        herbsAndSpicesButton.isClickable = false
        herbsAndSpicesButton.hide()
        juicesLayout.visibility = View.GONE
        juicesButton.isClickable = false
        juicesButton.hide()
        liqueursLayout.visibility = View.GONE
        liqueursButton.isClickable = false
        liqueursButton.hide()
        miscellaneousLayout.visibility = View.GONE
        miscellaneousButton.isClickable = false
        miscellaneousButton.hide()
        sweetsLayout.visibility = View.GONE
        sweetsButton.isClickable = false
        sweetsButton.hide()
        syrupsLayout.visibility = View.GONE
        syrupsButton.isClickable = false
        syrupsButton.hide()
        ingredients_recycler_view.alpha = 1.0F
        isAddMenuOpen = false
    }

    private fun openMenus(){
        addMenuButton.startAnimation(menuAnimRotateOut)
        addViaCameraButton.startAnimation(menuAnimOpen)
        addViaCameraButton.isClickable = true
        addViaCameraButton.show()
        alcoholsLayout.visibility = View.VISIBLE
        alcoholsButton.isClickable = true
        alcoholsButton.show()
        beveragesAndFlavoringLayout.visibility = View.VISIBLE
        beveragesAndFlavoringButton.isClickable = true
        beveragesAndFlavoringButton.show()
        bittersLayout.visibility = View.VISIBLE
        bittersButton.isClickable = true
        bittersButton.show()
        fruitsAndVegetablesLayout.visibility = View.VISIBLE
        fruitsAndVegetablesButton.isClickable = true
        fruitsAndVegetablesButton.show()
        herbsAndSpicesButtonLayout.visibility = View.VISIBLE
        herbsAndSpicesButton.isClickable = true
        herbsAndSpicesButton.show()
        juicesLayout.visibility = View.VISIBLE
        juicesButton.isClickable = true
        juicesButton.show()
        liqueursLayout.visibility = View.VISIBLE
        liqueursButton.isClickable = true
        liqueursButton.show()
        miscellaneousLayout.visibility = View.VISIBLE
        miscellaneousButton.isClickable = true
        miscellaneousButton.show()
        sweetsLayout.visibility = View.VISIBLE
        sweetsButton.isClickable = true
        sweetsButton.show()
        syrupsLayout.visibility = View.VISIBLE
        syrupsButton.isClickable = true
        syrupsButton.show()
        ingredients_recycler_view.alpha = 0.4F
        isAddMenuOpen = true
    }
}
