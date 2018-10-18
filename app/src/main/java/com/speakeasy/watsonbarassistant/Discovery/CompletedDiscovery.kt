package com.speakeasy.watsonbarassistant.Discovery

import com.speakeasy.watsonbarassistant.DiscoveryRecipe


interface CompletedDiscovery {
    fun onTaskCompleted(recipes: MutableList<DiscoveryRecipe>)
}