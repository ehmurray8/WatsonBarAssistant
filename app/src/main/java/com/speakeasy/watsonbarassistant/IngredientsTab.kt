package com.speakeasy.watsonbarassistant


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_ingredient_tab.*


class IngredientsTab : Fragment() {

    private val fireStore = FirebaseFirestore.getInstance()

    private lateinit var menuAnimOpen: Animation
    private lateinit var menuAnimClose: Animation
    private lateinit var menuAnimRotateOut: Animation
    private lateinit var menuAnimRotateBack: Animation

    private var isAddMenuOpen: Boolean = false
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

        ingredients_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        menuAnimOpen = AnimationUtils.loadAnimation(context, R.anim.menu_anim_open)
        menuAnimClose = AnimationUtils.loadAnimation(context, R.anim.menu_anim_close)
        menuAnimRotateOut = AnimationUtils.loadAnimation(context, R.anim.menu_anim_rotate_out)
        menuAnimRotateBack = AnimationUtils.loadAnimation(context, R.anim.menu_anim_rotate_back)

        addMenuButton.setOnClickListener {
            when {
                isAddMenuOpen -> closeMenus()
                else -> openMenus()
            }
        }
        addViaTextButton.setOnClickListener {
            closeMenus()
            ingredientInputView.visibility = View.VISIBLE
            ingredientInputView.setOnEditorActionListener { _, actionId, _ ->

                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        val name = ingredientInputView.text.toString()
                        val ingredient = Ingredient(name)
                        addIngredient(ingredient)
                        ingredientInputView.selectAll()
                        ingredientInputView.setText("")
                        true
                    }
                    else -> false
                }
            }
        }
        
        ingredientInputView.setOnTouchListener { _, event ->
            if(ingredientInputView.hasFocus()) {
                addMenuButton.hide()
            } else {
                addMenuButton.show()
            }
            ingredientInputView.onTouchEvent(event)
        }

        addViaCameraButton.setOnClickListener { Toast.makeText(context, "Camera support to be added!", Toast.LENGTH_SHORT).show() }
        addViaVoiceButton.setOnClickListener { Toast.makeText(context, "Voice support to be added!", Toast.LENGTH_SHORT).show() }
        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        ingredients_recycler_view.addItemDecoration(itemDecorator)

        setupSwipeHandler()
    }

    private fun addIngredient(ingredient: Ingredient) {
        val ingredients = (activity as MainMenu).ingredients
        if(ingredients.any { it.name.toLowerCase() == ingredient.name.toLowerCase() }) {
            Toast.makeText(activity, "${ingredient.name} is already stored as an ingredient.", Toast.LENGTH_SHORT).show()
        } else {
            addIngredientToFireStore(ingredient)
        }
    }

     private fun setupSwipeHandler() {
         val context = activity?.baseContext ?: return
         val swipeHandler = object : SwipeToDeleteCallback(context) {

             override fun onSwiped(p0: RecyclerView.ViewHolder, direction: Int) {
                 val position = p0.adapterPosition
                 viewAdapter?.removeAt(position)
             }
         }
         val itemTouchHelper = ItemTouchHelper(swipeHandler)
         itemTouchHelper.attachToRecyclerView(ingredients_recycler_view)
     }

    private fun addIngredientToFireStore(ingredient: Ingredient) {
        val mainMenu = (activity as? MainMenu)
        val uid = mainMenu?.currentUser?.uid ?: return
        fireStore.collection(MAIN_COLLECTION).document(uid)
                .collection(INGREDIENT_COLLECTION).add(ingredient).addOnSuccessListener { _ ->
                    Toast.makeText(context, "Successfully added ${ingredient.name}.", Toast.LENGTH_SHORT).show()
                    mainMenu.ingredients.add(ingredient)
                    mainMenu.ingredients.sortBy { it.name.toLowerCase().replace("\\s".toRegex(), "") }
                    refresh()
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to add ${ingredient.name}.", Toast.LENGTH_SHORT).show()
                }
    }

    private fun refresh() {
        viewAdapter?.notifyDataSetChanged()
    }

    private fun closeMenus(){
        addMenuButton.startAnimation(menuAnimRotateBack)
        addViaTextButton.startAnimation(menuAnimClose)
        addViaCameraButton.startAnimation(menuAnimClose)
        addViaVoiceButton.startAnimation(menuAnimClose)
        addViaTextButton.isClickable = false
        addViaTextButton.hide()
        addViaCameraButton.isClickable = false
        addViaCameraButton.hide()
        addViaVoiceButton.isClickable = false
        addViaVoiceButton.hide()
        ingredientInputView.visibility = View.GONE
        isAddMenuOpen = false
    }

    private fun openMenus(){
        addMenuButton.startAnimation(menuAnimRotateOut)
        addViaTextButton.startAnimation(menuAnimOpen)
        addViaCameraButton.startAnimation(menuAnimOpen)
        addViaVoiceButton.startAnimation(menuAnimOpen)
        addViaTextButton.isClickable = true
        addViaTextButton.show()
        addViaCameraButton.isClickable = true
        addViaCameraButton.show()
        addViaVoiceButton.isClickable = true
        addViaVoiceButton.show()
        isAddMenuOpen = true
    }
}
