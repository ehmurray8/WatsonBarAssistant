package com.speakeasy.watsonbarassistant.Discovery

import android.os.AsyncTask
import android.util.Log
import com.ibm.watson.developer_cloud.discovery.v1.Discovery
import com.ibm.watson.developer_cloud.discovery.v1.model.QueryOptions
import com.speakeasy.watsonbarassistant.*
import kotlinx.serialization.json.JSON
import java.util.*

class SearchDiscovery(inputListener: OnTaskCompleted) : AsyncTask<List<Ingredient>, Void, MutableList<DiscoveryRecipe>>() {

    private var listener: OnTaskCompleted

    init {
        this.listener = inputListener
    }

    override fun doInBackground(vararg args: List<Ingredient>): MutableList<DiscoveryRecipe> {
        var ingredients = args[0]
        var orderedRecipes = PriorityQueue<DiscoveryRecipe>(10, CompareLists)

        Log.i("Discovery", "Start.")
        val discovery = Discovery(
                VERSION_DIS,
                USERNAME_MIKE_DIS,
                PASSWORD_MIKE_DIS
        )
        discovery.endPoint = URL_MIKE_DIS

        val queryBuilder = QueryOptions.Builder(ENV_ID_MIKE_DIS, COL_ID_MIKE_DIS)

        queryBuilder.query(buildIngredientQuery(ingredients)).count(50)
        val queryResponse = discovery.query(queryBuilder.build()).execute()

        Log.i("Discovery", "Serialization Start.")
        //Log.i("Discovery", "Response: " + queryResponse.results[0])
        for(response in queryResponse.results){
            val recipe = JSON.nonstrict.parse<DiscoveryRecipe>(response.toString())
            if(recipe.imageBase64 == ""){
                Log.i("Discovery","Failed: " + recipe.title + ": " + response.toString() + "\n")
            }
            recipe.calculatePercentAvailable(ingredients)
            orderedRecipes.add(recipe)
        }

        Log.i("Discovery", "Serialization Finished.")

        return orderedRecipes.toMutableList()
    }

    protected fun onProgressUpdate(vararg progress: Int) {
    }

    override fun onPostExecute(result: MutableList<DiscoveryRecipe>){
        super.onPostExecute(result)

        this.listener.onTaskCompleted(result, MainMenu())
    }

    private fun buildIngredientQuery(ingredients: List<Ingredient>): String{
        val queryString = "ingredientList:"
        val ingredients = ingredients
        val sb = StringBuilder()
        sb.append(queryString)

        for (ingredient in ingredients){
            if(ingredient.name != "") {
                sb.append(ingredient.name)
                sb.append("|")
            }
        }

        sb.replace(sb.length-1,sb.length, "")
        Log.i("Discovery", "Ingredient Query: " + sb.toString())
        return sb.toString()
    }
}