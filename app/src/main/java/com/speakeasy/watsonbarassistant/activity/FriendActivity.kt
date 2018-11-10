package com.speakeasy.watsonbarassistant.activity

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.UserInfo
import com.speakeasy.watsonbarassistant.adapter.FriendAdapter
import kotlinx.android.synthetic.main.activity_friend.*

class FriendActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener {

    private var tabIndex = 0
    private var viewAdapter: FriendAdapter? = null
    private var fireStore = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()

    // Currently displayed users
    private var users = mutableListOf<UserInfo>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend)
        friendTabs.getTabAt(tabIndex)?.select()
        friendTabs.addOnTabSelectedListener(this)

        val viewManager = LinearLayoutManager(this)
        viewAdapter = FriendAdapter(applicationContext, users)

        userRecycler.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
        }

        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        userRecycler?.addItemDecoration(itemDecorator)
    }

    override fun onResume() {
        super.onResume()
        (application as? BarAssistant)?.loadUserInfo(auth, fireStore)
        refresh()
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {}

    override fun onTabUnselected(tab: TabLayout.Tab?) { }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        tabIndex = tab?.position ?: 0
        showCurrentTab()
    }

    private fun showCurrentTab() {
        users.clear()
        viewAdapter = when (tabIndex) {
            0 -> {
                users.addAll(BarAssistant.friends)
                FriendAdapter(applicationContext, users)
            } else -> {
                val uid = auth.currentUser?.uid
                users.addAll(BarAssistant.allUsers.asSequence().filter { it.userId != uid }
                        .filter { it !in users && it !in BarAssistant.blockedUsers}.toList())
                FriendAdapter(applicationContext, users)
            }
        }
        userRecycler.adapter = viewAdapter
        refresh()
    }

    private fun refresh() {
        viewAdapter?.notifyDataSetChanged()
    }
}
