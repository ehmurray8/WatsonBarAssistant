package com.speakeasy.watsonbarassistant


import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore



class IngredientsTab : Fragment() {

    private val fireStore = FirebaseFirestore.getInstance()

    private lateinit var addMenuButton: FloatingActionButton
    private lateinit var addViaTextButton: FloatingActionButton
    private lateinit var addViaCameraButton: FloatingActionButton
    private lateinit var addViaVoiceButton: FloatingActionButton
    lateinit var ingredientInputView: View

    lateinit var menu_anim_open: Animation
    lateinit var menu_anim_close: Animation
    lateinit var menu_anim_rotate_out: Animation
    lateinit var menu_anim_rotate_back: Animation

    private var isAddMenuOpen: Boolean = false
    private lateinit var recyclerView: RecyclerView
    private var viewAdapter: IngredientsAdapter? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
       super.onCreateView(inflater, container, savedInstanceState)
       return inflater.inflate(R.layout.fragment_ingredient_tab, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewManager = LinearLayoutManager(activity)
        val mainMenu = activity as MainMenu
        viewAdapter = IngredientsAdapter(mainMenu.ingredients, mainMenu.documentsMap)

        recyclerView = view.findViewById<RecyclerView>(R.id.ingredients_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        addMenuButton = view.findViewById(R.id.addIngredientId) as FloatingActionButton
        addViaTextButton = view.findViewById(R.id.addingViaTextId) as FloatingActionButton
        addViaCameraButton = view.findViewById(R.id.addingViaCameraId) as FloatingActionButton
        addViaVoiceButton = view.findViewById(R.id.addingViaVoiceId) as FloatingActionButton
        ingredientInputView = view.findViewById(R.id.add_ingredient_input)

        menu_anim_open = AnimationUtils.loadAnimation(context, R.anim.menu_anim_open)
        menu_anim_close = AnimationUtils.loadAnimation(context, R.anim.menu_anim_close)
        menu_anim_rotate_out = AnimationUtils.loadAnimation(context, R.anim.menu_anim_rotate_out)
        menu_anim_rotate_back = AnimationUtils.loadAnimation(context, R.anim.menu_anim_rotate_back)

        addMenuButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                if (isAddMenuOpen) {
                    closeMenus()

                } else {
                    openMenus()
                }
            }
        })
        addViaTextButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                val ingredientInput = view.findViewById(R.id.add_ingredient_input) as EditText

                closeMenus()
                ingredientInputView.visibility = View.VISIBLE
                //ingredientInputView.requestFocus()



                ingredientInput.setOnEditorActionListener { _, actionId, _ ->
                    return@setOnEditorActionListener when (actionId) {
                        EditorInfo.IME_ACTION_DONE -> {
                            val name = ingredientInput.text.toString()
                            val ingredient = Ingredient(name)
                            addIngredient(ingredient)
                            ingredientInput.selectAll()
                            ingredientInput.setText("")
                            true
                        }
                        else -> false
                    }
                }

            }
        })
        addViaCameraButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                Toast.makeText(context, "Camera support to be added!", Toast.LENGTH_SHORT).show()

            }
        })
        addViaVoiceButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                Toast.makeText(context, "Voice support to be added!", Toast.LENGTH_SHORT).show()

            }
        })
    }

    fun addIngredient(ingredient: Ingredient){

        val ingredients = (activity as MainMenu).ingredients
        if(ingredient in ingredients) {
            Toast.makeText(activity, "${ingredient.name} is already stored as an ingredient.", Toast.LENGTH_SHORT).show()
            return
        }
        addIngredientToFireStore(ingredient)
    }

    private fun addIngredientToFireStore(ingredient: Ingredient) {
        Toast.makeText(context, "REEE", Toast.LENGTH_SHORT).show()
        val mainMenu = (activity as? MainMenu)
        val uid = mainMenu?.currentUser?.uid ?: return
        fireStore.collection("app").document(uid)
                .collection("ingredients").add(ingredient).addOnSuccessListener { _ ->
                    Toast.makeText(context, "Successfully added ${ingredient.name}.", Toast.LENGTH_SHORT).show()
                    mainMenu.loadIngredients()
                    Log.d("FIRESTORE", "Successfully added ${ingredient.name}")
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to add ${ingredient.name}.", Toast.LENGTH_SHORT).show()
                    Log.d("FIRESTORE", "Failed to add ${ingredient.name}")
                }
    }


    private fun closeMenus(){
        addMenuButton.startAnimation(menu_anim_rotate_back)
        addViaTextButton.startAnimation(menu_anim_close)
        addViaCameraButton.startAnimation(menu_anim_close)
        addViaVoiceButton.startAnimation(menu_anim_close)
        addViaTextButton.isClickable = false
        addViaTextButton.visibility = View.INVISIBLE
        addViaCameraButton.isClickable = false
        addViaCameraButton.visibility = View.INVISIBLE
        addViaVoiceButton.isClickable = false
        addViaVoiceButton.visibility = View.INVISIBLE
        ingredientInputView.visibility = View.GONE
        isAddMenuOpen = false
    }

    private fun openMenus(){
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
        isAddMenuOpen = true
    }

}


