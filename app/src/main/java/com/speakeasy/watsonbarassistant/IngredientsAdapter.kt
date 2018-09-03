package com.speakeasy.watsonbarassistant

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class IngredientsAdapter(private var dataSet: MutableList<Ingredient>,
                         private var documentsMap: MutableMap<String, String>):
        RecyclerView.Adapter<IngredientsAdapter.ViewHolder>() {

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    private val fireStore = FirebaseFirestore.getInstance()
    private var authorization = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientsAdapter.ViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.ingredient_view,
                parent, false) as TextView
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = dataSet[position].name
    }

    override fun getItemCount(): Int = dataSet.count()

    fun removeAt(position: Int) {
        val ingredient = dataSet.removeAt(position)
        notifyItemRemoved(position)
        val uid = authorization.currentUser?.uid ?: return
        val documentId = documentsMap[ingredient.name] ?: return

        fireStore.collection("app").document(uid).collection("ingredients")
                .document(documentId).delete().addOnSuccessListener {
                    Log.d("FIRESTORE", "Deletion success ${ingredient.name}.")
                }.addOnFailureListener {
                    Log.d("FIRESTORE", "Deletion failure ${ingredient.name}.")
                }
    }
}
