package com.speakeasy.watsonbarassistant.Discovery

import com.speakeasy.watsonbarassistant.*
import com.speakeasy.watsonbarassistant.Recipe.DiscoveryRecipe
import com.speakeasy.watsonbarassistant.Recipe.MyRecipesTab


open class HandleDiscovery(private val overAllList: MutableList<MutableList<DiscoveryRecipe>>,
                      private val mainMenu: MainMenu?): OnTaskCompleted {

    override fun onTaskCompleted(recipes: MutableList<DiscoveryRecipe>) {
        overAllList[0].clear()
        overAllList[1].clear()
        overAllList[0].addAll(recipes)
        overAllList[1].addAll(recipes.shuffled().toMutableList())
        val fragment = mainMenu?.fragment
        if(fragment as? HomeTab != null) {
            fragment.refresh()
        } else if(fragment as? MyRecipesTab != null) {
            fragment.refresh()
        }
    }
}