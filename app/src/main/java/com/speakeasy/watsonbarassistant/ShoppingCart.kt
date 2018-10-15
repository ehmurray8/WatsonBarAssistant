package com.speakeasy.watsonbarassistant

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.activity_shopping_cart.*

class ShoppingCart : AppCompatActivity() {

    private val shoppingCartItems = mutableMapOf(Ingredient("Whiskey") to true,
            Ingredient("Scotch") to true, Ingredient("Rum") to true, Ingredient("Gin") to true)

    private val orderedItems = mutableListOf(Ingredient("Scotch"), Ingredient("Rum"), Ingredient("Gin"),
            Ingredient("Whiskey"))

    private var viewAdapter: ShoppingCartAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_cart)

        val manager = LinearLayoutManager(baseContext)
        viewAdapter = ShoppingCartAdapter(shoppingCartItems, orderedItems, this)

        shoppingCartContainer.apply {
            setHasFixedSize(true)
            layoutManager = manager
            adapter = viewAdapter
        }
        addShoppingCartInput.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        val name = addShoppingCartInput.text.toString()
                        val ingredient = Ingredient(name)
                        addIngredient(ingredient)
                        addShoppingCartInput.selectAll()
                        addShoppingCartInput.setText("")
                        true
                    }
                    else -> false
                }
        }
    }

    private fun addIngredient(ingredient: Ingredient) {
        shoppingCartItems[ingredient] = true
        orderedItems.add(ingredient)
        viewAdapter?.notifyDataSetChanged()
    }
}
