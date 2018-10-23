package com.speakeasy.watsonbarassistant.discovery

import com.speakeasy.watsonbarassistant.*


open class HandleDiscovery(private val mainMenu: MainMenu?): CompletedDiscovery {

    override fun onTaskCompleted(recipes: MutableList<DiscoveryRecipe>) {
        BarAssistant.recipes[0].clear()
        BarAssistant.recipes[0].addAll(recipes)
        val fragment = mainMenu?.fragment
        if(fragment as? HomeTab != null) {
            fragment.refresh()
        } else if(fragment as? MyRecipesTab != null) {
            fragment.refresh()
        }
    }
}