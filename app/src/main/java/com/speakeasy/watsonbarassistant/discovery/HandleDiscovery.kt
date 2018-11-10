package com.speakeasy.watsonbarassistant.discovery

import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.DiscoveryRecipe
import com.speakeasy.watsonbarassistant.activity.MainMenu


open class HandleDiscovery(private val mainMenu: MainMenu?): CompletedDiscovery {

    override fun onTaskCompleted(recipes: MutableList<DiscoveryRecipe>) {
        synchronized(BarAssistant.recipes) {
            BarAssistant.recipes[0].clear()
            BarAssistant.recipes [0].addAll(recipes)
            mainMenu?.loadFeed()
        }
    }
}