package com.speakeasy.watsonbarassistant.Discovery

import android.os.AsyncTask
import android.util.Log
import com.ibm.watson.developer_cloud.discovery.v1.Discovery
import com.ibm.watson.developer_cloud.discovery.v1.model.QueryOptions
import com.ibm.watson.developer_cloud.discovery.v1.model.QueryResponse
import com.ibm.watson.developer_cloud.service.exception.NotFoundException
import com.speakeasy.watsonbarassistant.*
import com.speakeasy.watsonbarassistant.Recipe.DiscoveryRecipe
import kotlinx.serialization.json.JSON

class SearchDiscovery(private val inputListener: OnTaskCompleted):
        AsyncTask<Array<Ingredient>, Void, MutableList<DiscoveryRecipe>>() {

    override fun doInBackground(vararg args: Array<Ingredient>): MutableList<DiscoveryRecipe> {
<<<<<<< HEAD
        val ingredients = args[0]
        val discovery = Discovery(VERSION_DIS, USERNAME_MIKE_DIS, PASSWORD_MIKE_DIS)
        discovery.endPoint = URL_MIKE_DIS
=======
        if(BarAssistant.isInternetConnected()) {
            val ingredients = args[0]
            val recipes = mutableListOf<DiscoveryRecipe>()
            val discovery = Discovery(VERSION_DIS, USERNAME_MIKE_DIS, PASSWORD_MIKE_DIS)
            discovery.endPoint = URL_MIKE_DIS
>>>>>>> master

            val queryBuilder = QueryOptions.Builder(ENV_ID_MIKE_DIS, COL_ID_MIKE_DIS)

<<<<<<< HEAD
        queryBuilder.query(buildIngredientQuery(ingredients)).count(DIS_COUNT)

        try {
            val queryResponse = discovery.query(queryBuilder.build()).execute()

            return this.processResponse(queryResponse, ingredients)
        } catch (exception: NotFoundException) {
            Log.d("Discovery Down", "Discovery service is not working.")
=======
            queryBuilder.query(buildIngredientQuery(ingredients)).count(50)
            try {
                val queryResponse = discovery.query(queryBuilder.build()).execute()
                for (response in queryResponse.results) {
                    val recipe = JSON.nonstrict.parse<DiscoveryRecipe>(response.toString())
                    recipe.calculatePercentAvailable(ingredients)
                    recipes.add(recipe)
                }
                recipes.filter {
                    it.title != "" && it.ingredientList.count() > 0
                }
                return recipes.sortedBy { it.percentOfIngredientsOwned }.reversed().toMutableList()
            } catch (exception: NotFoundException) {
                Log.d("Discovery Down", "Discovery service is not working.")
            }
>>>>>>> master
        }
        return mutableListOf()
    }

    override fun onPostExecute(result: MutableList<DiscoveryRecipe>){
        super.onPostExecute(result)
        inputListener.onTaskCompleted(result)
    }

    fun processResponse(input: QueryResponse, ingredients: Array<Ingredient>): MutableList<DiscoveryRecipe>{

        val orderedRecipes = PriorityQueue<DiscoveryRecipe>(10) { a, b ->
            when {
                a.queueValue < (b.queueValue) -> 1
                else -> -1
            }
        }

        for (response in input.results) {
            val recipe = JSON.nonstrict.parse<DiscoveryRecipe>(response.toString())
            recipe.calculatePercentAvailable(ingredients)
            orderedRecipes.add(recipe)
        }
        orderedRecipes.filter {
            it.title != "" && it.ingredientList.count() > 0
        }

        return orderedRecipes.toMutableList()
    }

    fun buildIngredientQuery(ingredients: Array<Ingredient>): String{
        return ingredients.asSequence().filter { it.name != "" }
                .joinToString("|", "ingredientList:") { it.name }
    }

    fun buildPowerSet(ingredients: Array<Ingredient>, length: Int): String{
        var query = ""

        
        return query
    }

}