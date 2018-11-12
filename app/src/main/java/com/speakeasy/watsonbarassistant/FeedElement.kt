package com.speakeasy.watsonbarassistant

enum class FeedType {
    SUGGESTION,
    RANDOM,
    POPULAR,
    FRIEND
}

data class FeedElement(val recipe: DiscoveryRecipe, val type: FeedType=FeedType.SUGGESTION, val friendUsername: String = "") {

    fun getDescription(): String {
        return when (type) {
            FeedType.SUGGESTION -> "Suggested Recipe"
            FeedType.RANDOM -> "Random Recipe"
            FeedType.POPULAR -> "Popular Recipe"
            FeedType.FRIEND -> "$friendUsername favorited"
        }
    }
}