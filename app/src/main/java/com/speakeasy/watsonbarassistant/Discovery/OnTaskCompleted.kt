package com.speakeasy.watsonbarassistant.Discovery

import com.speakeasy.watsonbarassistant.DiscoveryRecipe


interface OnTaskCompleted {
    fun onTaskCompleted(recipes: MutableList<DiscoveryRecipe>)
}