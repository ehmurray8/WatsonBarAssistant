package com.speakeasy.watsonbarassistant

import java.io.Serializable

data class Ingredient(val name: String): Serializable, Comparable<Ingredient> {

    var imageUri = ""

    override fun compareTo(other: Ingredient): Int {
        return this.name.compareTo(other.name)
    }

    fun compareName(): String {
         return name.toLowerCase().replace("\\s".toRegex(), "")
     }

    fun getImageName(): String {
        return "ingredient_images/${name.capitalize().replace(" ", "_")}.jpg"
    }
}
