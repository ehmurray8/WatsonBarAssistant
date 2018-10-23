package com.speakeasy.watsonbarassistant

import java.io.Serializable

data class Ingredient(val name: String): Serializable {
     fun compareName(): String {
         return name.toLowerCase().replace("\\s".toRegex(), "")
     }
}
