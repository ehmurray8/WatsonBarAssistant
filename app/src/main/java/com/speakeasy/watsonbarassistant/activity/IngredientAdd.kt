package com.speakeasy.watsonbarassistant.activity

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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


class IngredientAdd : AppCompatActivity() {

    private var authorization = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()

    companion object {
       var addIngredients:((MutableList<Ingredient>)-> Unit)?=null
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

        expandable_ingredient_list_view.setAdapter(IngredientExpandableListAdapter(this, secondLevel!![currentIndex], thirdLevel!!.slice(indexStart until indexEnd).toMutableList(), expandable_ingredient_list_view))
        //expandable_ingredient_list_view.setAdapter(MapExpandableListAdapter(this, ingredientsMap, expandable_ingredient_list_view))
    }

    fun addIngredients(){
        //val mainMenu : MainMenu = Activity
    }
}
