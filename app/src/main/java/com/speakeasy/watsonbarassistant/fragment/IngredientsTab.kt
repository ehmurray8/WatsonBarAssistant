package com.speakeasy.watsonbarassistant.fragment


import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
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
import android.view.inputmethod.InputMethodManager
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.SwipeToDeleteCallback
import com.speakeasy.watsonbarassistant.activity.MainMenu
import com.speakeasy.watsonbarassistant.activity.VisionActivity
import com.speakeasy.watsonbarassistant.adapter.IngredientsAdapter
import com.speakeasy.watsonbarassistant.com.speakeasy.watsonbarassistant.activity.AddRecipeActivity
import com.speakeasy.watsonbarassistant.extensions.toast
import kotlinx.android.synthetic.main.fragment_ingredient_tab.*


class IngredientsTab : Fragment() {

    private lateinit var menuAnimOpen: Animation
    private lateinit var menuAnimClose: Animation
    private lateinit var menuAnimRotateOut: Animation
    private lateinit var menuAnimRotateBack: Animation

    private var isAddMenuOpen: Boolean = false
    private var viewAdapter: IngredientsAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Ingredients"
        return inflater.inflate(R.layout.fragment_ingredient_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewManager = LinearLayoutManager(activity)
        val mainMenu = activity as MainMenu
        viewAdapter = IngredientsAdapter(synchronized(BarAssistant.ingredients) { BarAssistant.ingredients })

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
                        mainMenu.addIngredient(name)
                        refresh()
                        ingredientInputView.selectAll()
                        ingredientInputView.setText("")
                        true
                    }
                    else -> false
                }
            }
            ingredientInputView.post {
                ingredientInputView.requestFocus()
                ingredientInputView.setText("")
                ingredientInputView.setSelection(0)
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(ingredientInputView, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        
        ingredientInputView.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus) {
                addMenuButton.hide()
            } else {
                addMenuButton.show()
            }
        }
        addViaCameraButton.setOnClickListener {
            val intent = Intent(activity, VisionActivity::class.java)
            startActivity(intent)
        }
        addViaVoiceButton.setOnClickListener {
            val intent = Intent(activity, AddRecipeActivity::class.java)
            startActivity(intent)
            //activity?.applicationContext?.toast("Voice support to be added!")
        }

        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        ingredients_recycler_view.addItemDecoration(itemDecorator)

        setupSwipeHandler()
        setupKeyboardListener()
        refresh()
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

    fun refresh() {
        activity?.runOnUiThread {
            viewAdapter?.notifyDataSetChanged()
        }
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

    private fun setupKeyboardListener() {
        coordinateLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            if(coordinateLayout != null && ingredientInputView.hasFocus()) {
                coordinateLayout.getWindowVisibleDisplayFrame(rect)
                val screenHeight = coordinateLayout.rootView.height

                val keypadHeight = screenHeight - rect.bottom

                if (keypadHeight <= screenHeight * 0.15) {
                    ingredientInputView.clearFocus()
                    ingredientInputView.visibility = View.GONE
                }
            }
        }
    }
}
