package com.speakeasy.watsonbarassistant

data class FeedElement(val recipe: DiscoveryRecipe, val type: Int = 1, val friendId: Int = -1)