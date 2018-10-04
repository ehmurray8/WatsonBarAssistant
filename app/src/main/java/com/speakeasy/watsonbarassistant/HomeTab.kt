package com.speakeasy.watsonbarassistant

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home_tab.*


class HomeTab : Fragment() {

    private var viewAdapter: HomeAdapter? = null
    private var manager: LinearLayoutManager? = null

    companion object {
        private var lastScrolledState = 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_tab, container, false)
    }

    override fun onStop() {
        super.onStop()
        lastScrolledState = manager?.findFirstVisibleItemPosition() ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        manager = LinearLayoutManager(activity)
        val mainMenu = activity as MainMenu
        viewAdapter = HomeAdapter(mainMenu.recipes, mainMenu.homeCategories, mainMenu)

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
