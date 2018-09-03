package com.speakeasy.watsonbarassistant


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore


class AddTab : Fragment() {

    private val fireStore = FirebaseFirestore.getInstance()
    private var addButton: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_add_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addButton = view.findViewById(R.id.add_ingredient_button)
        addButton?.setOnClickListener {
            handleAddButton()
        }
    }

    private fun handleAddButton() {
        val ingredientInput = view?.findViewById<EditText>(R.id.add_ingredient_input)
        val name = ingredientInput?.text.toString()
        val ingredient = Ingredient(name)
        addIngredient(ingredient)
    }

    private fun addIngredient(ingredient: Ingredient) {
        val mainMenu = (activity as? MainMenu)
        val uid = mainMenu?.currentUser?.uid
        if(uid != null) {
            fireStore.collection("app").document(uid)
                    .collection("ingredients").add(ingredient).addOnSuccessListener { _ ->
                Toast.makeText(context, "Successfully added ${ingredient.name}.",
                        Toast.LENGTH_SHORT).show()
                mainMenu.loadIngredients()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to add ${ingredient.name}.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }
}
