package com.speakeasy.watsonbarassistant

import android.graphics.Color

enum class RecipeTag(val title: String) {
    MISSING("Missing") {
        override fun getColor(): Int {
            return Color.parseColor("#e55b5b")
        }
    },
    WHISKEY("Whiskey") {
        override fun getColor(): Int {
            return Color.parseColor("#af602b")
        }
    },
    VODKA("Vodka") {
        override fun getColor(): Int {
            return Color.parseColor("#1db2f2")
        }
    },
    TEQUILA("Tequila") {
        override fun getColor(): Int {
            return Color.parseColor("#e8e009")
        }
    };

    abstract fun getColor(): Int
}