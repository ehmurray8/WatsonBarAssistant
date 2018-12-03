package com.speakeasy.watsonbarassistant.activity

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.CheckedTextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.secondLevelIngredients
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.thirdLevelIngredients
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.adapter.IngredientExpandableListAdapter
import kotlinx.android.synthetic.main.fragment_ingredient_add_main.*
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.currentIndex
import com.speakeasy.watsonbarassistant.Ingredient
import com.speakeasy.watsonbarassistant.R.id.expandable_ingredient_list_view
import com.speakeasy.watsonbarassistant.activity.MainMenu
import com.speakeasy.watsonbarassistant.extensions.toast


class IngredientAdd : AppCompatActivity() {

    private var authorization = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()

    companion object {
       fun addIngredient(name: String){
           val ingredient = Ingredient(name)
           synchronized(BarAssistant.ingredients) {
               if (!BarAssistant.ingredients.any { it.name.toLowerCase() == ingredient.name.toLowerCase() }) {
                   BarAssistant.ingredients.add(ingredient)
               }
           }
       }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_ingredient_add_main)

        title = "Add Ingredients"

        getIngredientMasterList()
    }

    private fun getIngredientMasterList() {

        val barAssistant = application as? BarAssistant
        barAssistant?.loadMasterIngredientsFromFireStore(authorization, fireStore)
        var temp = 0
        for (i in currentIndex-1 downTo 0) {
            temp += secondLevelIngredients!![i].size
        }
        var tempAdd = temp + secondLevelIngredients!![currentIndex].size
        addToExpandableList(secondLevelIngredients, thirdLevelIngredients, temp, tempAdd)

    }

    private fun addToExpandableList(secondLevel: MutableList<MutableList<String>>?, thirdLevel: MutableList<MutableList<String>>?, indexStart: Int, indexEnd : Int) {
        thirdLevel!!.forEach {temp ->
            temp.sort()
        }
        var childCheckedBoolean:MutableList<MutableList<Boolean>> = mutableListOf()
        var temp:MutableList<Boolean>
        thirdLevel.forEach { index->
            temp= MutableList(index.size) {false}
            childCheckedBoolean.add(temp)
        }
        var parentCheckedBoolean:MutableList<Boolean> =  MutableList(secondLevel!![currentIndex]!!.size) { false }
        //var childCheckedBoolean = MutableList(childLevel!!.size) {index -> MutableList(childLevel!![groupPosition].size) {index2-> false} }

        expandable_ingredient_list_view.setAdapter(IngredientExpandableListAdapter(this, secondLevel!![currentIndex], parentCheckedBoolean, childCheckedBoolean, thirdLevel.slice(indexStart until indexEnd).toMutableList(), expandable_ingredient_list_view, currentIndex))
        //expandable_ingredient_list_view.setAdapter(MapExpandableListAdapter(this, ingredientsMap, expandable_ingredient_list_view))
    }

}
