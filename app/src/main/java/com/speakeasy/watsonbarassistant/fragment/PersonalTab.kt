package com.speakeasy.watsonbarassistant.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.DiscoveryRecipe
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.activity.UserProfile
import com.speakeasy.watsonbarassistant.adapter.HorizontalRecipeAdapter
import kotlinx.android.synthetic.main.fragment_personal_tab.*

class PersonalTab : Fragment(), TabLayout.OnTabSelectedListener {

    private var tabIndex = 0
    private var viewAdapter: HorizontalRecipeAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        setUserInfo()

        accountTabs.addOnTabSelectedListener(this)
        accountTabs?.getTabAt(tabIndex)?.select()

        profileRecipeRecycler.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
        }

        fullAccountButton.setOnClickListener {
            val intent = Intent(activity, UserProfile::class.java)
            startActivity(intent)
        }
        updateRecyclerView()
    }

    private fun setUserInfo() {
        val firstName = BarAssistant.userInfo?.firstName ?: ""
        val lastName = BarAssistant.userInfo?.lastName ?: ""
        val fullName = "$firstName $lastName"
        fullNameAccount.text = fullName
        usernameAccount.text = BarAssistant.userInfo?.username
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_personal_tab, container, false)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) { }

    override fun onTabUnselected(tab: TabLayout.Tab?) { }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        tabIndex = tab?.position ?: 0
        updateRecyclerView()
    }

    private fun updateRecyclerView() {
        val recipes: MutableList<DiscoveryRecipe>
        val collectionName: String
        when(tabIndex) {
            0 -> {
                recipes = BarAssistant.recipes[0]
                collectionName = "Suggestions"
            }
            1 -> {
                recipes = BarAssistant.favoritesList.toMutableList()
                collectionName = "Favorites"
            }
            else -> {
                recipes = BarAssistant.recipes[1]
                collectionName = "Recently Viewed"
            }
        }
        viewAdapter = HorizontalRecipeAdapter(recipes, activity as Activity, collectionName)
        profileRecipeRecycler.adapter = viewAdapter
        refresh()
    }

    fun refresh() {
        viewAdapter?.notifyDataSetChanged()
        setUserInfo()
    }
}
