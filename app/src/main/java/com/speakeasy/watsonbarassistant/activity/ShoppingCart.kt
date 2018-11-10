package com.speakeasy.watsonbarassistant.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.inputmethod.EditorInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.speakeasy.watsonbarassistant.*
import com.speakeasy.watsonbarassistant.extensions.shoppingCartDocument
import com.speakeasy.watsonbarassistant.extensions.toast
import kotlinx.android.synthetic.main.activity_shopping_cart.*
import java.util.*

class ShoppingCart : AppCompatActivity() {

    private val shoppingCartItems = mutableMapOf<Ingredient, Boolean>()

    private val orderedItems = mutableListOf<Ingredient>()

    private var viewAdapter: ShoppingCartAdapter? = null

    private var authorization = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_cart)
        getShoppingCart()

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
        setupSwipeHandler()
        loadFromFireStore()
    }

    private fun addIngredient(ingredient: Ingredient) {
        if(!orderedItems.contains(ingredient)) {
            shoppingCartItems[ingredient] = true
            orderedItems.add(ingredient)
            viewAdapter?.notifyDataSetChanged()
        } else {
            applicationContext.toast("${ingredient.name} is already in the grocery list.")
        }
    }

    override fun onPause() {
        super.onPause()
        saveToFireStore()
        storeShoppingCart()
    }

    private fun storeShoppingCart() {
        val preferences = getSharedPreferences(SHARED_PREFERENCES_GROCERY, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val gson = Gson()
        val groceryJson = gson.toJson(orderedItems.toTypedArray())
        val isNeeded = orderedItems.map { shoppingCartItems[it] }
        val isNeededJson = gson.toJson(isNeeded.toTypedArray())
        editor.putString(GROCERY_INGREDIENTS_PREFERENCES, groceryJson)
        editor.putString(GROCERY_NEEDED_PREFERENCES, isNeededJson)
        editor.apply()
    }

    private fun getShoppingCart() {
        val preferences = getSharedPreferences(SHARED_PREFERENCES_GROCERY, Context.MODE_PRIVATE)
        val gson = Gson()
        val groceryString = preferences.getString(GROCERY_INGREDIENTS_PREFERENCES, "")
        val neededString = preferences.getString(GROCERY_NEEDED_PREFERENCES, "")
        val storedIngredients = gson.fromJson(groceryString, Array<Ingredient>::class.java)
        val storedNeeded = gson.fromJson(neededString, Array<Boolean>::class.java)
        orderedItems.clear()
        shoppingCartItems.clear()
        if(storedIngredients != null && storedIngredients.count() > 0 && storedNeeded != null &&
                storedNeeded.count() == storedIngredients.count()) {
            orderedItems.addAll(storedIngredients)
            orderedItems.forEachIndexed { i, ingredient -> shoppingCartItems[ingredient] = storedNeeded[i] }
        }
    }

    private fun saveToFireStore() {
        val uid = authorization.currentUser?.uid
        if(uid != null) {
            val neededList = mutableListOf<Boolean>()
            orderedItems.forEach {
                val needed = shoppingCartItems[it]
                if(needed != null) {
                    neededList.add(needed)
                }
            }
            val shoppingCartMap = mutableMapOf(GROCERY_INGREDIENTS to orderedItems.map { it -> it.name },
                    GROCERY_NEEDED to neededList)
            fireStore.shoppingCartDocument(uid).set(shoppingCartMap.toMap())
        }
    }

    private fun loadFromFireStore() {
        val uid = authorization.currentUser?.uid
        if(uid != null) {
            fireStore.shoppingCartDocument(uid).get().addOnSuccessListener {
                val ingredients = it.get(GROCERY_INGREDIENTS) as? ArrayList<*>
                val neededList = it.get(GROCERY_NEEDED) as? ArrayList<*>
                ingredients?.forEachIndexed { i, element ->
                    val ingredient = Ingredient(element as String)
                    if(!orderedItems.contains(ingredient)) {
                        orderedItems.add(ingredient)
                        shoppingCartItems[ingredient] = (neededList?.get(i) as? Boolean) ?: true
                    }
                }
                viewAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun setupSwipeHandler() {
         val swipeHandler = object : SwipeToDeleteCallback(baseContext) {

             override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
                return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT)
             }

             override fun isLongPressDragEnabled(): Boolean {
                 return true
             }

             override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                 val from = p1.adapterPosition
                 val to = p2.adapterPosition
                 viewAdapter?.notifyItemMoved(from, to)
                 Collections.swap(orderedItems, from, to)
                 return true
             }

             override fun onSwiped(p0: RecyclerView.ViewHolder, direction: Int) {
                 val position = p0.layoutPosition
                 val ingredient = orderedItems.removeAt(position)
                 shoppingCartItems.remove(ingredient)
                 viewAdapter?.notifyDataSetChanged()
             }
         }
         val itemTouchHelper = ItemTouchHelper(swipeHandler)
         itemTouchHelper.attachToRecyclerView(shoppingCartContainer)
     }
}
