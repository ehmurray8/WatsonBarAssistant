package com.speakeasy.watsonbarassistant.discovery

import com.speakeasy.watsonbarassistant.DiscoveryRecipe


interface CompletedDiscovery {
    fun onTaskCompleted(recipes: MutableList<DiscoveryRecipe>)
}