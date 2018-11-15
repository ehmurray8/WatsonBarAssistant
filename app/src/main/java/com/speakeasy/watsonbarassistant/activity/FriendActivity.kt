package com.speakeasy.watsonbarassistant.activity

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
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
    private var authorization = FirebaseAuth.getInstance()
    private var auth = FirebaseAuth.getInstance()

    // Currently displayed users
    private var users = mutableListOf<UserInfo>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend)
        friendTabs.getTabAt(tabIndex)?.select()
        friendTabs.addOnTabSelectedListener(this)

        val viewManager = LinearLayoutManager(this)
        viewAdapter = FriendAdapter(applicationContext, users, this::refresh)

        userRecycler.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
        }

        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        userRecycler?.addItemDecoration(itemDecorator)
        showCurrentTab()
        friendSearch.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers(s.toString())
            }

        })
    }

    private fun searchUsers(query: String) {
        if(query != "") {
            val newUsers = BarAssistant.allUsers.filter { query in it.username }
            users.clear()
            users.addAll(newUsers)
        } else {
            users.clear()
            users.addAll(BarAssistant.allUsers)
        }
        refresh()
    }

    override fun onResume() {
        super.onResume()
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
                FriendAdapter(applicationContext, users, this::refresh)
            } else -> {
                val uid = auth.currentUser?.uid
                users.addAll(BarAssistant.allUsers.asSequence().filter { it.userId != uid }
                        .filter { it !in users && it !in BarAssistant.blockedUsers}.toList())
                FriendAdapter(applicationContext, users, this::refresh)
            }
        }
        userRecycler.adapter = viewAdapter
        userRecycler.refreshDrawableState()
        refresh()
    }

    override fun onPause() {
        super.onPause()
        (application as? BarAssistant)?.loadUserInfo(authorization, fireStore)
    }

    private fun refresh() {
        viewAdapter = FriendAdapter(applicationContext, users, this::refresh)
        userRecycler.adapter = viewAdapter
        viewAdapter?.notifyDataSetChanged()
        userRecycler.refreshDrawableState()
    }
}
