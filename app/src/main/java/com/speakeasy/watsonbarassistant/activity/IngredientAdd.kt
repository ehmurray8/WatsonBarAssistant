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
import java.util.*
import kotlin.collections.ArrayList


class IngredientAdd : AppCompatActivity() {

    private var authorization = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_ingredient_add_main)

        title = "Add Ingredients"

        getIngredientMasterList()

        /*firstLevel.add("Alcohols")
        firstLevel.add("Beverages & Flavoring")
        firstLevel.add("Bitters")

        val alcohols : MutableList<String> = ArrayList()
        alcohols.add("Absinthe")
        alcohols.add("Amaretto")
        alcohols.add("Beer")
        alcohols.add("Brandy")

        val beveragesAndFlavoring : MutableList<String> = ArrayList()
        beveragesAndFlavoring.add("Apple Cider")
        beveragesAndFlavoring.add("Bloody Mary")
        beveragesAndFlavoring.add("Coconut Milk")

        val bitters : MutableList<String> = ArrayList()
        bitters.add("Absinthe")
        bitters.add("Angostura")
        bitters.add("Aromatic")

        secondLevel.add(alcohols)
        secondLevel.add(beveragesAndFlavoring)
        secondLevel.add(bitters)*/



    }

    fun getIngredientMasterList(){

        val barAssistant = application as? BarAssistant
        barAssistant?.loadMasterIngredientsFromFireStore(authorization, fireStore)

        var firstLevel : MutableList<String> = ArrayList()
        firstLevel.add("Alcohols")
        firstLevel.add("Beverages & Flavoring")
        firstLevel.add("Bitters")
        firstLevel.add("Fruits & Vegetables")
        firstLevel.add("Herbs & Spices")
        firstLevel.add("Juices")
        firstLevel.add("Liqueurs")
        firstLevel.add("Miscellaneous")
        firstLevel.add("Sweets")
        firstLevel.add("Syrups")

        addToExpandableList(firstLevel, secondLevelIngredients, thirdLevelIngredients)

        thirdLevelIngredients?.forEach {index ->
            Log.d("TAG", index)
        }

        //addToExpandableList(thirdLevelIngredients, secondLevelIngredients)

    }

    private fun addToExpandableList(firstLevel: MutableList<String>?, secondLevel : MutableList<MutableList<String>>?, thirdLevel : MutableList<String>?){
        expandable_ingredient_list_view.setAdapter(FirstExpandableListAdapter(this, firstLevel, secondLevel, thirdLevel,expandable_ingredient_list_view ))
        //expandable_ingredient_list_view.setAdapter(FirstExpandableListAdapter(this, secondLevel!![0], thirdLevel, expandable_ingredient_list_view ))
    }
}
