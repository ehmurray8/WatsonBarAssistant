package com.speakeasy.watsonbarassistant

import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment

class MainMenuTabListener(private val replaceFragment: (Fragment) -> Unit):
        TabLayout.OnTabSelectedListener {

    override fun onTabReselected(tab: TabLayout.Tab?) { }

    override fun onTabUnselected(tab: TabLayout.Tab?) { }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        when(tab?.position) {
            0 -> replaceFragment(HomeTab())
            1 -> replaceFragment(AddTab())
            2 -> replaceFragment(RecipesTab())
        }
    }

}