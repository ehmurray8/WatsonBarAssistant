package com.speakeasy.watsonbarassistant

import android.content.Context
import android.support.v7.widget.LinearLayoutManager

class AutoLinearLayoutManager(context: Context): LinearLayoutManager(context) {

    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }

}