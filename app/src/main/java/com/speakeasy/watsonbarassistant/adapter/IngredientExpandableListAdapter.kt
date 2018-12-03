package com.speakeasy.watsonbarassistant.adapter

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.activity.IngredientAdd

import kotlinx.android.synthetic.main.fragment_ingredient_add_main.*
import java.util.ArrayList

class IngredientExpandableListAdapter(var activity: Activity, var firstLevel : MutableList<String>?, var secondLevel : MutableList<MutableList<String>>?, var expandableListView: ExpandableListView, var currentIndex:Int) : BaseExpandableListAdapter() {

    val addedIngredients: MutableList<String> = mutableListOf()
    var parentCheckedBoolean: MutableList<Boolean> = mutableListOf()
    var childCheckedBoolean: MutableList<MutableList<Boolean>> = mutableListOf()
    var parentCheckBoxes:MutableList<CheckedTextView> = mutableListOf()

    override fun getGroup(groupPosition: Int): String {
        if(currentIndex==5){
            return firstLevel!![groupPosition]+" Juice"
        }
        else if(currentIndex==6){
            return firstLevel!![groupPosition]+" Liqueur"
        }
        else if(currentIndex==9){
            return firstLevel!![groupPosition]+" Syrup"
        }
        else {
            return firstLevel!![groupPosition]
        }
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return secondLevel!![groupPosition][childPosition].isNotEmpty()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View? {
        parentCheckedBoolean = MutableList(firstLevel!!.size) {index -> false }
        var v: View
        if(convertView == null){
            val inflater = activity.baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = inflater.inflate(R.layout.fragment_ingredient_add_group, null)
        }
        else{
            v=convertView
        }
        val title = v?.findViewById<CheckedTextView>(R.id.listTitle)
        title?.text = getGroup(groupPosition)
        title?.setOnClickListener{
            if(expandableListView.isGroupExpanded(groupPosition)){
                //title.isChecked=false
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
                //title.isChecked=true
                parentCheckedBoolean[groupPosition]=true
                addedIngredients.add(getGroup(groupPosition))
                Toast.makeText(activity.baseContext, "Added " + getGroup(groupPosition), Toast.LENGTH_SHORT).show()
                expandableListView.expandGroup(groupPosition)
            }

            /*Log.d("SIZE", parentCheckedBoolean.size.toString())
            parentCheckedBoolean.forEach { i ->
                Log.d("TAG", i.toString())
            }*/
        }
        return v
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return secondLevel!![groupPosition].size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): String {
        return secondLevel!![groupPosition][childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View? {

        childCheckedBoolean = MutableList(secondLevel!!.size) {index -> MutableList(secondLevel!![groupPosition].size) {index2-> false} }

        var v : View
        if(convertView == null){
            val inflater = activity.baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = inflater.inflate(R.layout.fragment_ingredient_add_item, null)
        }
        else{
            v=convertView
        }
        val title = convertView?.findViewById<CheckedTextView>(R.id.expandedListItem)
        title?.text = getChild(groupPosition, childPosition)
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

        Log.d("SIZECHILD", childCheckedBoolean.size.toString())
        childCheckedBoolean.forEach { i ->
            //Log.d("OUTER", i.toString())
            i.forEach {j->
                //Log.d("INNER", j.toString())
            }
        }

        return v
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return firstLevel!!.size
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