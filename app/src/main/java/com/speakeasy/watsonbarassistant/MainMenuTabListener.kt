package com.speakeasy.watsonbarassistant

import android.support.design.widget.TabLayout
import com.speakeasy.watsonbarassistant.activity.MainMenu

class MainMenuTabListener(private val mainMenu: MainMenu):
        TabLayout.OnTabSelectedListener {

    override fun onTabReselected(tab: TabLayout.Tab?) { }

    override fun onTabUnselected(tab: TabLayout.Tab?) { }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        mainMenu.tabIndex = tab?.position ?: 0
        mainMenu.showCurrentFragment()
    }
}