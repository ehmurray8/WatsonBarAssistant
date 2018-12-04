package com.speakeasy.watsonbarassistant.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.currentIngredientCategoryIndex
import com.speakeasy.watsonbarassistant.Ingredient
import com.speakeasy.watsonbarassistant.activity.IngredientAdd
import com.speakeasy.watsonbarassistant.addIngredient
import com.speakeasy.watsonbarassistant.fragment.IngredientsTab
import kotlinx.android.synthetic.main.ingredients_radial_overlay.*


fun Activity.addIngredientHandlers(addIngredients: ((MutableList<Ingredient>, Context) -> Unit)) {
    alcoholsButton.setOnClickListener {
        currentIngredientCategoryIndex = 0
        showIngredientAdd(addIngredients)
    }
    alcoholsLayout.setOnClickListener {
        currentIngredientCategoryIndex = 0
        showIngredientAdd(addIngredients)
    }
    beveragesAndFlavoringLayout.setOnClickListener {
        currentIngredientCategoryIndex = 1
        showIngredientAdd(addIngredients)
    }
    beveragesAndFlavoringButton.setOnClickListener {
        currentIngredientCategoryIndex = 1
        showIngredientAdd(addIngredients)
    }
    bittersLayout.setOnClickListener {
        currentIngredientCategoryIndex = 2
        showIngredientAdd(addIngredients)
    }
    bittersButton.setOnClickListener {
        currentIngredientCategoryIndex = 2
        showIngredientAdd(addIngredients)
    }
    fruitsAndVegetablesLayout.setOnClickListener {
        currentIngredientCategoryIndex = 3
        showIngredientAdd(addIngredients)
    }
    fruitsAndVegetablesButton.setOnClickListener {
        currentIngredientCategoryIndex = 3
        showIngredientAdd(addIngredients)
    }
    herbsAndSpicesLayout.setOnClickListener {
        currentIngredientCategoryIndex = 4
        showIngredientAdd(addIngredients)
    }
    herbsAndSpicesButton.setOnClickListener {
        currentIngredientCategoryIndex = 4
        showIngredientAdd(addIngredients)
    }
    juicesLayout.setOnClickListener {
        currentIngredientCategoryIndex = 5
        showIngredientAdd(addIngredients)
    }
    juicesButton.setOnClickListener {
        currentIngredientCategoryIndex = 5
        showIngredientAdd(addIngredients)
    }
    liqueursLayout.setOnClickListener {
        currentIngredientCategoryIndex = 6
        showIngredientAdd(addIngredients)
    }
    liqueursButton.setOnClickListener {
        currentIngredientCategoryIndex = 6
        showIngredientAdd(addIngredients)
    }
    miscellaneousLayout.setOnClickListener {
        currentIngredientCategoryIndex = 7
        showIngredientAdd(addIngredients)
    }
    miscellaneousButton.setOnClickListener {
        currentIngredientCategoryIndex = 7
        showIngredientAdd(addIngredients)
    }
    sweetsLayout.setOnClickListener {
        currentIngredientCategoryIndex = 8
        showIngredientAdd(addIngredients)
    }
    sweetsButton.setOnClickListener {
        currentIngredientCategoryIndex = 8
        showIngredientAdd(addIngredients)
    }
    syrupsLayout.setOnClickListener {
        currentIngredientCategoryIndex = 9
        showIngredientAdd(addIngredients)
    }
    syrupsButton.setOnClickListener {
        currentIngredientCategoryIndex = 9
        showIngredientAdd(addIngredients)
    }
}

fun Activity.showIngredientAdd(addIngredients: ((MutableList<Ingredient>, Context) -> Unit)) {
    IngredientAdd.addIngredientFunc = addIngredients
    val intent = Intent(this, IngredientAdd::class.java)
    startActivity(intent)
}


fun Activity.closeIngredientRadial() {
    //barrier.visibility=View.GONE
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
    herbsAndSpicesLayout.visibility = View.GONE
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
}


fun Activity.openIngredientRadial() {
    //barrier.visibility = View.VISIBLE
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
    herbsAndSpicesLayout.visibility = View.VISIBLE
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
}


fun addMainIngredients(ingredients: MutableList<Ingredient>, context: Context) {
    ingredients.forEachIndexed { index, it ->
        addIngredient(newIngredient = it, refreshDiscovery = index == ingredients.count() - 1, context = context)
    }
}
