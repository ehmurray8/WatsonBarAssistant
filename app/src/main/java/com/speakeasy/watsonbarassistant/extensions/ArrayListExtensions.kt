package com.speakeasy.watsonbarassistant.extensions

fun <T> ArrayList<T>.toStringMutableList(): MutableList<String> {
    val newArray = mutableListOf<String>()
    this.forEach {
        (it as? String)?.let {
            newArray.add(it)
        }
    }
    return newArray
}