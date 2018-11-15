package com.speakeasy.watsonbarassistant.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.currentIngredientCategoryIndex
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.secondLevelIngredients
import com.speakeasy.watsonbarassistant.Ingredient
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.adapter.IngredientExpandableListAdapter
import kotlinx.android.synthetic.main.fragment_ingredient_add_main.*


class IngredientAdd : AppCompatActivity() {

    private var authorization = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()

    companion object {
        var addIngredientFunc: ((MutableList<Ingredient>, Context) -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_ingredient_add_main)

        title = "Add Ingredients"

        getIngredientMasterList()
    }

    private fun getIngredientMasterList() {
        if(secondLevelIngredients?.count() == 0) {
            (application as? BarAssistant)?.loadMasterIngredientsFromFireStore(authorization, fireStore)
        }
        var temp = 0
        for (i in currentIngredientCategoryIndex - 1 downTo 0) {
            temp += secondLevelIngredients?.get(i)?.size ?: 0
        }
        val tempAdd = temp + (secondLevelIngredients?.get(currentIngredientCategoryIndex)?.size ?: 0)
        addToExpandableList(secondLevelIngredients, BarAssistant.thirdLevelIngredients, temp, tempAdd)
    }

    private fun addToExpandableList(secondLevel: MutableList<MutableList<String>>?, thirdLevel: MutableList<MutableList<String>>?, indexStart: Int, indexEnd : Int) {
        thirdLevel?.forEach {temp ->
            temp.sort()
        }
        val adapter = IngredientExpandableListAdapter(this, secondLevel?.get(currentIngredientCategoryIndex),
                        thirdLevel?.slice(indexStart until indexEnd)?.toMutableList(), expandable_ingredient_list_view, currentIngredientCategoryIndex)
        expandable_ingredient_list_view.setAdapter(adapter)
    }
}
