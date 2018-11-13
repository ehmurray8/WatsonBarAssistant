package com.speakeasy.watsonbarassistant.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import android.widget.Toast
import com.speakeasy.watsonbarassistant.R

class SecondExpandableListAdapter(var context: Context, var secondLevel : MutableList<MutableList<String>>?, var thirdLevel : MutableList<String>?, var expandableListView: ExpandableListView, var index : Int) : BaseExpandableListAdapter() {

    override fun getGroup(groupPosition: Int): String {
        return secondLevel!![groupPosition][index]
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
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
        val title = convertView?.findViewById<TextView>(R.id.listTitle)
        title?.text = getGroup(groupPosition)
        title?.setOnClickListener{
            if(expandableListView.isGroupExpanded(groupPosition)){
                expandableListView.collapseGroup(groupPosition)
            }
            else{
                expandableListView.expandGroup(groupPosition)
            }
            Toast.makeText(context, getGroup(groupPosition), Toast.LENGTH_SHORT)
        }
        return convertView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return thirdLevel!!.size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): String {
       return thirdLevel!![groupPosition]
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
        val title = convertView?.findViewById<TextView>(R.id.expandedListItem)
        title?.text = getChild(groupPosition, childPosition)
        title?.setOnClickListener{
            Toast.makeText(context, "Added "+getChild(groupPosition, childPosition), Toast.LENGTH_SHORT)
        }
        return convertView
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return secondLevel!!.size
    }
}