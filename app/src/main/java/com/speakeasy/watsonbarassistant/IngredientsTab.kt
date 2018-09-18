package com.speakeasy.watsonbarassistant


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.view.animation.Animation
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import android.graphics.drawable.AnimationDrawable
import android.view.animation.AnimationUtils
import android.widget.Toolbar
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.*


class IngredientsTab : Fragment() {

    private val fireStore = FirebaseFirestore.getInstance()
    //private var addButton: Button? = null
    lateinit var addMenuButton: FloatingActionButton
    lateinit var addViaTextButton: FloatingActionButton
    lateinit var addViaCameraButton: FloatingActionButton
    lateinit var addViaVoiceButton: FloatingActionButton
    val ingredientInput = view?.findViewById<EditText>(R.id.add_ingredient_input)
    //lateinit var toolbar: Toolbar

    lateinit var menu_anim_open: Animation
    lateinit var menu_anim_close: Animation
    lateinit var menu_anim_rotate_out: Animation
    lateinit var menu_anim_rotate_back: Animation

    var ingredients = mutableListOf<Ingredient>()
    var documentsMap = mutableMapOf<String, String>()
    var currentUser: FirebaseUser? = null
    private var fragment: Fragment? = null
    private var isAddMenuOpen: Boolean = false
    private var authorization = FirebaseAuth.getInstance()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ingredient_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //loadUserData()
        //loadIngredients()

        //toolbar = findViewById(R.id.toolbar) as Toolbar
        addMenuButton = view.findViewById(R.id.addIngredientId) as FloatingActionButton
        addViaTextButton = view.findViewById(R.id.addingViaTextId) as FloatingActionButton
        addViaCameraButton = view.findViewById(R.id.addingViaCameraId) as FloatingActionButton
        addViaVoiceButton = view.findViewById(R.id.addingViaVoiceId) as FloatingActionButton

        menu_anim_open = AnimationUtils.loadAnimation(context, R.anim.menu_anim_open)
        menu_anim_close = AnimationUtils.loadAnimation(context, R.anim.menu_anim_close)
        menu_anim_rotate_out = AnimationUtils.loadAnimation(context, R.anim.menu_anim_rotate_out)
        menu_anim_rotate_back = AnimationUtils.loadAnimation(context, R.anim.menu_anim_rotate_back)

        addMenuButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                if (isAddMenuOpen) {

                    addMenuButton.startAnimation(menu_anim_rotate_back)
                    addViaTextButton.startAnimation(menu_anim_close)
                    addViaCameraButton.startAnimation(menu_anim_close)
                    addViaVoiceButton.startAnimation(menu_anim_close)
                    addViaTextButton.isClickable = false
                    addViaTextButton.visibility = View.GONE
                    addViaCameraButton.isClickable = false
                    addViaCameraButton.visibility = View.GONE
                    addViaVoiceButton.isClickable = false
                    addViaVoiceButton.visibility = View.GONE
                    ingredientInput.to(View.GONE)
                    isAddMenuOpen = false
                    //Log.d("Raj", "close")

                } else {

                    addMenuButton.startAnimation(menu_anim_rotate_out)
                    addViaTextButton.startAnimation(menu_anim_open)
                    addViaCameraButton.startAnimation(menu_anim_open)
                    addViaVoiceButton.startAnimation(menu_anim_open)
                    addViaTextButton.isClickable = true
                    addViaTextButton.visibility = View.VISIBLE
                    addViaCameraButton.isClickable = true
                    addViaCameraButton.visibility = View.VISIBLE
                    addViaVoiceButton.isClickable = true
                    addViaVoiceButton.visibility = View.VISIBLE
                    ingredientInput.to(View.VISIBLE)
                    isAddMenuOpen = true
                    //Log.d("Raj","open")

                }
            }
        })
        addViaTextButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                val name = ingredientInput?.text.toString()
                val ingredient = Ingredient(name)
                addIngredient(ingredient)

            }
        })
        addViaCameraButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                Toast.makeText(context, "To be added!", Toast.LENGTH_SHORT).show()

            }
        })
        addViaVoiceButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                Toast.makeText(context, "To be added!", Toast.LENGTH_SHORT).show()

            }
        })
    }
    /*addButton = view.findViewById(R.id.add_ingredient_button)
        addButton?.setOnClickListener {
            handleAddButton()
        }*/

    /*private fun handleAddButton() {
        val ingredientInput = view?.findViewById<EditText>(R.id.add_ingredient_input)
        val name = ingredientInput?.text.toString()
        val ingredient = Ingredient(name)
        addIngredient(ingredient)
    }*/


    private fun addIngredient(ingredient: Ingredient) {
        val ingredients = (activity as IngredientsTab).ingredients
        if (ingredient in ingredients) {
            Toast.makeText(activity, "${ingredient.name} is already stored as an ingredient.", Toast.LENGTH_SHORT).show()
            return
        }
        //addIngredientToFireStore(ingredient)
    }
}

    /*private fun addIngredientToFireStore(ingredient: Ingredient) {
        val ingredientsTab = (activity as? IngredientsTab)
        val uid = ingredientsTab?.currentUser?.uid ?: return
        fireStore.collection("app").document(uid)
                .collection("ingredients").add(ingredient).addOnSuccessListener { _ ->
                    Toast.makeText(context, "Successfully added ${ingredient.name}.", Toast.LENGTH_SHORT).show()
                    ingredientsTab.loadIngredients()
                    Log.d("FIRESTORE", "Successfully added ${ingredient.name}")
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to add ${ingredient.name}.", Toast.LENGTH_SHORT).show()
                    Log.d("FIRESTORE", "Failed to add ${ingredient.name}")
                }
        var undoOnClickListener: View.OnClickListener = View.OnClickListener { view ->
            listItems.removeAt(listItems.size - 1)
            adapter?.notifyDataSetChanged()
            Snackbar.make(view, "Item removed", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    private fun loadUserData() {
        currentUser = authorization.currentUser
        loadIngredients()
    }

    private fun loadIngredients() {
        val uid = currentUser?.uid
        ingredients.clear()
        if(uid != null) {
            fireStore.collection("app").document(uid)
                    .collection("ingredients").get().addOnCompleteListener {
                        if (it.isSuccessful) {
                            it.result.forEach { snapshot ->
                                parseSnapshot(snapshot)
                            }
                        } else {
                            Log.d("FIRESTORE", "Failed to load ingredients.")
                        }
                        (fragment as? IngredientsTab)?.refresh()
                    }
        }
    }

    private fun parseSnapshot(snapshot: QueryDocumentSnapshot) {
        val name = snapshot.get("name") as? String
        val id = snapshot.id
        if(name != null) {
            documentsMap[name] = id
            val ingredient = Ingredient(name)
            ingredients.add(ingredient)
            Log.d("FIRESTORE", "Successfully retrieved ${ingredient.name}.")
        }
    }

    /*fun refresh() {
        viewAdapter?.notifyDataSetChanged()
    }*/
}
*/

