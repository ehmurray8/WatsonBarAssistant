package com.speakeasy.watsonbarassistant

import java.io.Serializable

data class Recipe(val name: String, val imageId: Int, val ingredients: List<String>): Serializable