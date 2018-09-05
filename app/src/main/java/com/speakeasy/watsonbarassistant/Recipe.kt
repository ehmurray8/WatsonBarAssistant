package com.speakeasy.watsonbarassistant

import java.io.Serializable

data class Recipe(val name: String, val image_id: Int, val ingredients: List<String>): Serializable