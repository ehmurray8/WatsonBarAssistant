package com.speakeasy.watsonbarassistant.Discovery

import com.speakeasy.watsonbarassistant.*


class HandleDiscovery(private val mainMenu: MainMenu?): OnTaskCompleted {

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