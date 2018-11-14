package com.speakeasy.watsonbarassistant.com.speakeasy.watsonbarassistant

import android.os.AsyncTask
import com.algolia.search.saas.Client
import com.algolia.search.saas.RequestOptions
import com.speakeasy.watsonbarassistant.ALGOLIA_API_KEY
import com.speakeasy.watsonbarassistant.ALGOLIA_APP_ID
import com.speakeasy.watsonbarassistant.DiscoveryRecipe
import kotlinx.serialization.json.JSON
import org.json.JSONObject

class AddToAlgolia: AsyncTask<DiscoveryRecipe,Void,Unit>(){

    private val algoliaClient = Client(ALGOLIA_APP_ID, ALGOLIA_API_KEY)
    private val algoliaIndex = algoliaClient.getIndex("Recipe")

    override fun doInBackground(vararg params: DiscoveryRecipe) {

        val recipe = params[0].toFireStoreRecipe()
        val options = RequestOptions()
        algoliaIndex.addObject(JSONObject(JSON.stringify(recipe)), options)

    }
}