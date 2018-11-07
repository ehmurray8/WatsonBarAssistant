package com.speakeasy.watsonbarassistant

import java.io.Serializable

data class Ingredient(val name: String): Serializable, Comparable<Ingredient> {

    override fun compareTo(other: Ingredient): Int {
        return this.name.compareTo(other.name)
    }

    fun compareName(): String {
         return name.toLowerCase().replace("\\s".toRegex(), "")
     }
}
