package com.speakeasy.watsonbarassistant

import android.content.Context
import com.speakeasy.watsonbarassistant.discovery.HandleDiscovery
import com.speakeasy.watsonbarassistant.discovery.SearchDiscovery
import com.speakeasy.watsonbarassistant.extensions.toast
import java.util.*

private var lastDiscoveryRefreshTime = -1L

fun refreshDiscovery(forceRefresh: Boolean = false) {
    synchronized(BarAssistant.ingredients) {
        if (BarAssistant.ingredients.count() > 0 ) {
            if (forceRefresh || lastDiscoveryRefreshTime == -1L ||
                    Date().time - lastDiscoveryRefreshTime >= 30_000) {
                lastDiscoveryRefreshTime = Date().time
                val discovery = SearchDiscovery(HandleDiscovery())
                discovery.execute(BarAssistant.ingredients.toTypedArray())
            }
        }
    }
}

fun addIngredient(name: String = "", newIngredient: Ingredient? = null, context: Context, refreshDiscovery: Boolean = false) {
    val ingredient = if(name != "") {
        Ingredient(name)
    } else {
        newIngredient ?: return
    }
    synchronized(BarAssistant.ingredients) {
        if (BarAssistant.ingredients.any { it.name.toLowerCase() == ingredient.name.toLowerCase() }) {
            context.toast("${ingredient.name} is already stored as an ingredient.")
        } else {
            BarAssistant.ingredients.add(ingredient)
            refreshDiscovery(refreshDiscovery)
        }
    }
}
