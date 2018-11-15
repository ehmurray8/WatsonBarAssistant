package com.speakeasy.watsonbarassistant.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.ExpandableListView
import com.speakeasy.watsonbarassistant.Ingredient
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.activity.IngredientAdd
import kotlinx.android.synthetic.main.fragment_ingredient_add_main.*

class IngredientExpandableListAdapter(var activity: Activity, var firstLevel : MutableList<String>?, var secondLevel : MutableList<MutableList<String>>?, var expandableListView: ExpandableListView, var currentIndex:Int) : BaseExpandableListAdapter() {

    private val addedIngredients: MutableList<String> = mutableListOf()

    override fun getGroup(groupPosition: Int): String {
        return when (currentIndex) {
            5 -> firstLevel?.get(groupPosition) + " Juice"
            6 -> firstLevel?.get(groupPosition) + " Liqueur"
            9 -> firstLevel?.get(groupPosition) + " Syrup"
            else -> firstLevel?.get(groupPosition) ?: ""
        }
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return secondLevel?.get(groupPosition)?.get(childPosition)?.isNotEmpty() ?: false
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View? {
        val view = convertView ?: {
            val inflater = activity.baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.fragment_ingredient_add_group, null)
        }()
        val title = view.findViewById<CheckBox>(R.id.listTitle)
        title?.text = getGroup(groupPosition)
        title?.setOnClickListener{
            if(expandableListView.isGroupExpanded(groupPosition)){
                expandableListView.collapseGroup(groupPosition)
                addedIngredients.remove(getGroup(groupPosition))
            }
            else{
                activity.confirmButton.visibility = View.VISIBLE
                activity.confirmButton.setOnClickListener{ _ ->
                    onConfirmClicked()
                }
                addedIngredients.add(getGroup(groupPosition))
                expandableListView.expandGroup(groupPosition)
            }
        }
        return view
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return secondLevel?.get(groupPosition)?.size ?: 0
    }

    override fun getChild(groupPosition: Int, childPosition: Int): String {
        return secondLevel?.get(groupPosition)?.get(childPosition) ?: ""
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View? {
        val view = convertView ?: {
            val inflater = activity.baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.fragment_ingredient_add_item, null)
        }()
        val title = view.findViewById<CheckBox>(R.id.expandedListItem)
        title.isChecked = false
        title?.text = getChild(groupPosition, childPosition)
        title?.setOnClickListener{
            if(title.isChecked) {
                addedIngredients.add(getChild(groupPosition, childPosition) + " " + getGroup(groupPosition))
            }
            else{
                addedIngredients.remove(getChild(groupPosition, childPosition) + " " + getGroup(groupPosition))
            }
        }
        return view
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return firstLevel?.size ?: 0
    }
    private fun onConfirmClicked(){
        IngredientAdd.addIngredientFunc?.invoke(addedIngredients.asSequence().map { Ingredient(it) }.toMutableList(), activity)
        addedIngredients.clear()
        activity.finish()
    }
}