package com.speakeasy.watsonbarassistant


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


class HomeTab : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayIngredients()
    }

    fun displayIngredients() {
        val mainMenu = activity as MainMenu
        val ingredients = mainMenu.ingredients
        val ingredientsTextView = view?.findViewById<TextView>(R.id.ingredients_view)
        ingredientsTextView?.text = ingredients.joinToString { ingredient ->
            ingredient.name
        }
    }
}
