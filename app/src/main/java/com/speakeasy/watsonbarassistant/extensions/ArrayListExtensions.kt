package com.speakeasy.watsonbarassistant.extensions

fun <T> ArrayList<T>.toStringMutableList(): MutableList<String> {
    val newArray = mutableListOf<String>()
    forEach { element ->
        (element.toString() as? String)?.let {
            newArray.add(it)
        }
    }
    return newArray
}