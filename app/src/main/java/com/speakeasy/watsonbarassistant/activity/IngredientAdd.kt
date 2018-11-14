package com.speakeasy.watsonbarassistant.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.secondLevelIngredients
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.thirdLevelIngredients
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.adapter.FirstExpandableListAdapter
import kotlinx.android.synthetic.main.fragment_ingredient_add_main.*
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.currentIndex


class IngredientAdd : AppCompatActivity() {

    private var authorization = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
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

        /*secondLevel!!.forEach {temp ->
            temp.sort()
        }

        val ingredientsMap: LinkedHashMap<String, MutableList<MutableList<String>>> = linkedMapOf()

        secondLevel[currentIndex].forEach { index->
            ingredientsMap.keys.add(index)
        }
        //lastViewedRecipes.keys.add(secondLevel[currentIndex])
        ingredientsMap.values.add(thirdLevel!!.slice(indexStart until indexEnd).toMutableList())
        ingredientsMap.keys.forEach{ index ->
            index.toSortedSet()
        }*/
        expandable_ingredient_list_view.setAdapter(FirstExpandableListAdapter(this, secondLevel!![currentIndex], thirdLevel!!.slice(indexStart until indexEnd).toMutableList(), expandable_ingredient_list_view))
        //expandable_ingredient_list_view.setAdapter(FirstExpandableListAdapter(this, lastViewedRecipes.keys.toMutableList(), lastViewedRecipes.values, expandable_ingredient_list_view))
        //expandable_ingredient_list_view.setAdapter(MapExpandableListAdapter(this, ingredientsMap, expandable_ingredient_list_view))
        //ingredientsMap.clear()

    }
}
