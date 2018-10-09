package com.speakeasy.watsonbarassistant.Discovery

import com.speakeasy.watsonbarassistant.Recipe.DiscoveryRecipe


interface OnTaskCompleted {
    fun onTaskCompleted(recipes: MutableList<DiscoveryRecipe>)
}