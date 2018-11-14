package com.speakeasy.watsonbarassistant.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.firstLevelIngredients
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.secondLevelIngredients
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.thirdLevelIngredients
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.adapter.FirstExpandableListAdapter
import com.speakeasy.watsonbarassistant.adapter.SecondExpandableListAdapter
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

    fun getIngredientMasterList() {

        val barAssistant = application as? BarAssistant
        barAssistant?.loadMasterIngredientsFromFireStore(authorization, fireStore)
        var temp = 0
        var tempAdd = 0
        for (i in currentIndex-1 downTo 0) {
            temp += secondLevelIngredients!![i].size
        }
        tempAdd = temp + secondLevelIngredients!![currentIndex].size
        addToExpandableList(secondLevelIngredients, thirdLevelIngredients, temp, tempAdd)

    }

    private fun addToExpandableList(secondLevel: MutableList<MutableList<String>>?, thirdLevel: MutableList<MutableList<String>>?, indexStart: Int, indexEnd : Int) {
        expandable_ingredient_list_view.setAdapter(FirstExpandableListAdapter(this, secondLevel!![currentIndex], thirdLevel!!.slice(indexStart until indexEnd).toMutableList(), expandable_ingredient_list_view))
    }
}
