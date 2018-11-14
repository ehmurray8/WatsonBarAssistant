package com.speakeasy.watsonbarassistant.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.speakeasy.watsonbarassistant.R

class FirstExpandableListAdapter(var context: Context, var firstLevel : MutableList<String>?, var secondLevel : MutableList<MutableList<String>>?, var expandableListView: ExpandableListView) : BaseExpandableListAdapter() {

    override fun getGroup(groupPosition: Int): String {
        return firstLevel!![groupPosition]
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        if(secondLevel!![groupPosition][childPosition].isNotEmpty()) {
            return true
        }
        else{
            return false
        }
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        if(convertView == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.fragment_ingredient_add_group, null)
        }
        val title = convertView?.findViewById<CheckBox>(R.id.listTitle)
        title?.text = getGroup(groupPosition)
        title?.setOnClickListener{
            if(expandableListView.isGroupExpanded(groupPosition)){
                expandableListView.collapseGroup(groupPosition)
            }
            else{
                expandableListView.expandGroup(groupPosition)
            }
            //Toast.makeText(context, getGroup(groupPosition), Toast.LENGTH_SHORT)
        }
        return convertView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return secondLevel!![groupPosition].size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): String {
        //SecondExpandableListAdapter(context, secondLevel, thirdLevel, expandableListView,childPosition)
        return secondLevel!![groupPosition][childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        if(convertView == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.fragment_ingredient_add_item, null)
        }
        val title = convertView?.findViewById<CheckBox>(R.id.expandedListItem)
        title?.text = getChild(groupPosition, childPosition)
        title?.setOnClickListener{
            //Toast.makeText(context, "Added "+getChild(groupPosition, childPosition), Toast.LENGTH_SHORT)
        }
        return convertView
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return firstLevel!!.size
    }
}