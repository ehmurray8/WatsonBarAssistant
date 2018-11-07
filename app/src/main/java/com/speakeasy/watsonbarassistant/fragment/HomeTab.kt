package com.speakeasy.watsonbarassistant.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.activity.MainMenu
import com.speakeasy.watsonbarassistant.adapter.FeedAdapter
import kotlinx.android.synthetic.main.fragment_home_tab.*


class HomeTab : Fragment() {

    private var viewAdapter: FeedAdapter? = null
    private var manager: LinearLayoutManager? = null

    companion object {
        private var lastScrolledState = 0
        var homeScrollPositions: MutableMap<String, Int> =
                BarAssistant.homeCategories.map { it to 0 }.toMap().toMutableMap()
        var homeScrollManagers = mutableListOf<LinearLayoutManager?>(null, null)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Bar Assistant"
        return inflater.inflate(R.layout.fragment_home_tab, container, false)
    }

    override fun onStop() {
        super.onStop()
        lastScrolledState = manager?.findFirstVisibleItemPosition() ?: 0
        homeScrollManagers.forEachIndexed { i, manager ->
            homeScrollPositions[BarAssistant.homeCategories[i]] = manager?.findFirstVisibleItemPosition() ?: 0
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        manager = LinearLayoutManager(activity)
        val mainMenu = activity as MainMenu
        viewAdapter = FeedAdapter(mainMenu)

        mainRefreshLayout.setOnRefreshListener {
            mainMenu.refreshDiscovery()
            mainRefreshLayout.isRefreshing = false
        }

        home_container.isNestedScrollingEnabled = true
        home_container.apply {
            setHasFixedSize(true)
            layoutManager = manager
            adapter = viewAdapter
        }

        manager?.scrollToPosition(lastScrolledState)
    }

    fun refresh() {
        viewAdapter?.notifyDataSetChanged()
    }
}
