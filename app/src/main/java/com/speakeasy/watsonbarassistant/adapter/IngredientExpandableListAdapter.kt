package com.speakeasy.watsonbarassistant.adapter

import android.app.Activity
import android.app.Application
import android.content.Context
import android.support.v4.app.NotificationCompat.getGroup
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.activity.IngredientAdd

import kotlinx.android.synthetic.main.fragment_ingredient_add_main.*
import org.apache.commons.lang3.mutable.Mutable
import java.util.ArrayList

class IngredientExpandableListAdapter(var activity: Activity, var parentLevel : MutableList<String>?, private var parentCheckedBoolean: MutableList<Boolean>, private var childCheckedBoolean: MutableList<MutableList<Boolean>>,var childLevel : MutableList<MutableList<String>>?, var expandableListView: ExpandableListView, var currentIndex:Int) : BaseExpandableListAdapter() {

    private val addedIngredients: MutableList<String> = mutableListOf()


    override fun getGroup(groupPosition: Int): String {
        if(currentIndex==5){
            return parentLevel!![groupPosition]+" Juice"
        }
        else if(currentIndex==6){
            return parentLevel!![groupPosition]+" Liqueur"
        }
        else if(currentIndex==9){
            return parentLevel!![groupPosition]+" Syrup"
        }
        else {
            return parentLevel!![groupPosition]
        }
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return childLevel!![groupPosition][childPosition].isNotEmpty()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View? {
        var v: View
        v = if(convertView == null){
            val inflater = activity.baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.fragment_ingredient_add_group, null)
        }
        else{
            convertView
        }

        val title = v.findViewById<CheckedTextView>(R.id.listTitle)
        title?.text = getGroup(groupPosition)

        title.isChecked = parentCheckedBoolean[groupPosition]

        title?.setOnClickListener{
            if(expandableListView.isGroupExpanded(groupPosition)){
                title.isChecked=false
                parentCheckedBoolean[groupPosition]=false
                expandableListView.collapseGroup(groupPosition)
                addedIngredients.remove(getGroup(groupPosition))
                Toast.makeText(activity.baseContext, "Removed " + getGroup(groupPosition), Toast.LENGTH_SHORT).show()
            }
            else{
                activity.confirmButton.visibility = View.VISIBLE
                activity.confirmButton.setOnClickListener{
                    onConfirmClicked()
                }
                title.isChecked=true
                parentCheckedBoolean[groupPosition]=true
                addedIngredients.add(getGroup(groupPosition))
                Toast.makeText(activity.baseContext, "Added " + getGroup(groupPosition), Toast.LENGTH_SHORT).show()
                expandableListView.expandGroup(groupPosition)
            }

        }
        return v
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return childLevel!![groupPosition].size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): String {
        return childLevel!![groupPosition][childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View? {

        var v : View
        v= if(convertView == null){
            val inflater = activity.baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.fragment_ingredient_add_item, null)
        }
        else{
            convertView
        }

        val title = convertView?.findViewById<CheckedTextView>(R.id.expandedListItem)
        title?.text = getChild(groupPosition, childPosition)

        title?.isChecked = childCheckedBoolean[groupPosition][childPosition]

        title?.setOnClickListener{
            if(!title.isChecked) {
                title.isChecked=true
                childCheckedBoolean[groupPosition][childPosition] = true
                Toast.makeText(activity.baseContext, "Added " + getChild(groupPosition, childPosition) + " " + getGroup(groupPosition), Toast.LENGTH_SHORT).show()
                addedIngredients.add(getChild(groupPosition, childPosition) + " " + getGroup(groupPosition))
            }
            else{
                title.isChecked=false
                childCheckedBoolean[groupPosition][childPosition] = false
                addedIngredients.remove(getChild(groupPosition, childPosition) + " " + getGroup(groupPosition))
                Toast.makeText(activity.baseContext, "Removed " + getChild(groupPosition, childPosition) + " " + getGroup(groupPosition), Toast.LENGTH_SHORT).show()
            }
        }

        return v
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return parentLevel!!.size
    }
    fun onConfirmClicked(){
        Toast.makeText(activity.baseContext, "Ingredients Added!", Toast.LENGTH_SHORT).show()
        addedIngredients.forEach { name ->
            if (name != null) {
                IngredientAdd.addIngredient(name)
            }
        }
        addedIngredients.clear()
    }



}