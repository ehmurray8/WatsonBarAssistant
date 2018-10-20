package com.speakeasy.watsonbarassistant.Discovery

import android.os.AsyncTask
import android.util.Log
import com.ibm.watson.developer_cloud.discovery.v1.Discovery
import com.ibm.watson.developer_cloud.discovery.v1.model.QueryOptions
import com.speakeasy.watsonbarassistant.*
import kotlinx.serialization.json.JSON
import java.lang.Exception
import java.util.*

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

    private fun buildIngredientQuery(ingredients: Array<Ingredient>): String{
        var depth = 3
        var ans = "ingredientList:"

        if (depth > ingredients.count()){
            depth = ingredients.count()
        }
        
        for (i in 0..ingredients.count() - 1){
            var pos = i % ingredients.count()
            ans += "(" + ingredients[pos].name

            for (extra in 1..depth){
                ans += "," + ingredients[(pos + extra) % ingredients.count()].name
            }
            ans += ")|"
        }

        ans = ans.dropLast(1)
        Log.i("DiscoveryString", ans)
        return ans
    }

}