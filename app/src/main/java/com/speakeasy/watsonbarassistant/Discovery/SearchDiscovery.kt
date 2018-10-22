package com.speakeasy.watsonbarassistant.Discovery

import android.os.AsyncTask
import android.util.Log
import com.ibm.watson.developer_cloud.discovery.v1.Discovery
import com.ibm.watson.developer_cloud.discovery.v1.model.QueryOptions
import com.speakeasy.watsonbarassistant.*
import kotlinx.serialization.json.JSON
import java.lang.Exception

class SearchDiscovery(private val inputListener: CompletedDiscovery):
        AsyncTask<Array<Ingredient>, Void, MutableList<DiscoveryRecipe>>() {

    override fun doInBackground(vararg args: Array<Ingredient>): MutableList<DiscoveryRecipe> {
        if(BarAssistant.isInternetConnected()) {
            val ingredients = args[0]
            val recipes = mutableListOf<DiscoveryRecipe>()
            val discovery = Discovery(VERSION_DIS, USERNAME_MIKE_DIS, PASSWORD_MIKE_DIS)
            discovery.endPoint = URL_MIKE_DIS

            val queryBuilder = QueryOptions.Builder(ENV_ID_MIKE_DIS, COL_ID_MIKE_DIS)

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
            } catch (exception: Exception) {
                Log.d("Discovery Down", "Discovery service is not working.")
            }
        }
        return mutableListOf()
    }

    override fun onPostExecute(result: MutableList<DiscoveryRecipe>){
        super.onPostExecute(result)
        inputListener.onTaskCompleted(result)
    }

    /*
    private fun buildIngredientQuery(ingredients: Array<Ingredient>): String{
        var extension = 3
        var ans = "ingredientList:"
        var evenOffset = 0

        if (ingredients.count() % 2 == 0){
            evenOffset = 1
        }

        if (extension > ingredients.count()/2 - evenOffset){
            extension = ingredients.count()/2 - evenOffset
        }
        
        for (i in 0..ingredients.count() - 1){
            var pos = i % ingredients.count()
            ans += "(" + ingredients[pos].name

            for (extra in 1..extension){
                ans += "," + ingredients[(pos + extra) % ingredients.count()].name
            }
            ans += ")|"
        }
        var size = 1
        Log.i("CreateAndSets", createAndSets(ingredients,0,size - 1).toString())

        ans = ans.dropLast(1)
        Log.i("DiscoveryString", ans)
        return ans
    }
    */


    fun buildIngredientQuery(ingredients: Array<Ingredient>): String {
        var ans = "ingredientList:"
        var queryString = buildAndString(createAndSets(ingredients,0,0))
        var threshold = 5
        var optimalSetSize = 4


        if (ingredients.count() > threshold){
            queryString = buildAndString(createAndSets(ingredients,0,optimalSetSize - 1)) + "|" + queryString
        } else {
            for (size in 1..ingredients.count() - 1){
                queryString = buildAndString(createAndSets(ingredients,0,size)) + "|" + queryString
            }
        }

        Log.i("DiscoveryString", ans + queryString)
        return ans + queryString
    }

    fun buildAndString(sets: MutableList<MutableSet<String>>): String{
        var ans = ""

        for (set in sets){
            ans += "("
            for (element in set){
                ans += element + ","
            }
            ans = ans.dropLast(1)
            ans += ")|"
        }

        ans = ans.dropLast(1)
        return ans
    }


    fun createAndSets(ingredients: Array<Ingredient>, start: Int, size: Int): MutableList<MutableSet<String>>{
        var ans = mutableListOf<MutableSet<String>>()

        for (pos in start..ingredients.count() - size - 1){
            var subSet = mutableSetOf<String>()
            subSet.add(ingredients[pos].name)
            ans.add(subSet)
        }

        if (size > 0){
            var count = ans.count() - 1
            for (element in 0..count){
                var superElement = ans.removeAt(0)
                for (subSet in createAndSets(ingredients, start + 1 + element, size - 1)){
                    ans.add(ans.count(), subSet.union(superElement).toMutableSet())
                }
            }
        }

        return ans
    }

}