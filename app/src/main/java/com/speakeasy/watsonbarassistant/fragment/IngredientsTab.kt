package com.speakeasy.watsonbarassistant.fragment


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.SwipeToDeleteCallback
import com.speakeasy.watsonbarassistant.activity.IngredientAdd
import com.speakeasy.watsonbarassistant.activity.MainMenu
import com.speakeasy.watsonbarassistant.activity.VisionActivity
import com.speakeasy.watsonbarassistant.adapter.IngredientGridAdapter
import com.speakeasy.watsonbarassistant.BarAssistant.Companion.currentIndex
import kotlinx.android.synthetic.main.fragment_ingredient_tab_radial.*



class IngredientsTab : Fragment() {

    private lateinit var menuAnimOpen: Animation
    private lateinit var menuAnimClose: Animation
    private lateinit var menuAnimRotateOut: Animation
    private lateinit var menuAnimRotateBack: Animation

    private var isAddMenuOpen: Boolean = false
    private var viewAdapter: IngredientGridAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Ingredients"
        return inflater.inflate(R.layout.fragment_ingredient_tab_radial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewManager = GridLayoutManager(activity, 2)
        val mainMenu = activity as MainMenu
        viewAdapter = IngredientGridAdapter(synchronized(BarAssistant.ingredients) { BarAssistant.ingredients }, activity as Activity)

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

        alcoholsButton.setOnClickListener {
            currentIndex = 0
            val intent = Intent(activity, IngredientAdd::class.java)
            startActivity(intent)
        }
        beveragesAndFlavoringButton.setOnClickListener {
            currentIndex = 1
            val intent = Intent(activity, IngredientAdd::class.java)
            startActivity(intent)
        }
        bittersButton.setOnClickListener {
            currentIndex = 2
            val intent = Intent(activity, IngredientAdd::class.java)
            startActivity(intent)
        }
        fruitsAndVegetablesButton.setOnClickListener {
            currentIndex = 3
            val intent = Intent(activity, IngredientAdd::class.java)
            startActivity(intent)
        }
        herbsAndSpicesButton.setOnClickListener {
            currentIndex = 4
            val intent = Intent(activity, IngredientAdd::class.java)
            startActivity(intent)
        }
        juicesButton.setOnClickListener {
            currentIndex = 5
            val intent = Intent(activity, IngredientAdd::class.java)
            startActivity(intent)
        }
        liqueursButton.setOnClickListener {
            currentIndex = 6
            val intent = Intent(activity, IngredientAdd::class.java)
            startActivity(intent)
        }
        miscellaneousButton.setOnClickListener {
            currentIndex = 7
            val intent = Intent(activity, IngredientAdd::class.java)
            startActivity(intent)
        }
        sweetsButton.setOnClickListener {
            currentIndex = 8
            val intent = Intent(activity, IngredientAdd::class.java)
            startActivity(intent)
        }
        syrupsButton.setOnClickListener {
            currentIndex = 9
            val intent = Intent(activity, IngredientAdd::class.java)
            startActivity(intent)
        }

        addViaCameraButton.setOnClickListener {
            val intent = Intent(activity, VisionActivity::class.java)
            startActivity(intent)
        }

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
        addViaCameraButton.startAnimation(menuAnimClose)
        addViaCameraButton.isClickable = false
        addViaCameraButton.hide()
        alcoholsLayout.visibility = View.GONE
        alcoholsButton.isClickable = false
        alcoholsButton.hide()
        beveragesAndFlavoringLayout.visibility = View.GONE
        beveragesAndFlavoringButton.isClickable = false
        beveragesAndFlavoringButton.hide()
        bittersLayout.visibility = View.GONE
        bittersButton.isClickable = false
        bittersButton.hide()
        fruitsAndVegetablesLayout.visibility = View.GONE
        fruitsAndVegetablesButton.isClickable = false
        fruitsAndVegetablesButton.hide()
        herbsAndSpicesButtonLayout.visibility = View.GONE
        herbsAndSpicesButton.isClickable = false
        herbsAndSpicesButton.hide()
        juicesLayout.visibility = View.GONE
        juicesButton.isClickable = false
        juicesButton.hide()
        liqueursLayout.visibility = View.GONE
        liqueursButton.isClickable = false
        liqueursButton.hide()
        miscellaneousLayout.visibility = View.GONE
        miscellaneousButton.isClickable = false
        miscellaneousButton.hide()
        sweetsLayout.visibility = View.GONE
        sweetsButton.isClickable = false
        sweetsButton.hide()
        syrupsLayout.visibility = View.GONE
        syrupsButton.isClickable = false
        syrupsButton.hide()
        ingredients_recycler_view.alpha = 1.0F
        isAddMenuOpen = false
    }

    private fun openMenus(){
        addMenuButton.startAnimation(menuAnimRotateOut)
        addViaCameraButton.startAnimation(menuAnimOpen)
        addViaCameraButton.isClickable = true
        addViaCameraButton.show()
        alcoholsLayout.visibility = View.VISIBLE
        alcoholsButton.isClickable = true
        alcoholsButton.show()
        beveragesAndFlavoringLayout.visibility = View.VISIBLE
        beveragesAndFlavoringButton.isClickable = true
        beveragesAndFlavoringButton.show()
        bittersLayout.visibility = View.VISIBLE
        bittersButton.isClickable = true
        bittersButton.show()
        fruitsAndVegetablesLayout.visibility = View.VISIBLE
        fruitsAndVegetablesButton.isClickable = true
        fruitsAndVegetablesButton.show()
        herbsAndSpicesButtonLayout.visibility = View.VISIBLE
        herbsAndSpicesButton.isClickable = true
        herbsAndSpicesButton.show()
        juicesLayout.visibility = View.VISIBLE
        juicesButton.isClickable = true
        juicesButton.show()
        liqueursLayout.visibility = View.VISIBLE
        liqueursButton.isClickable = true
        liqueursButton.show()
        miscellaneousLayout.visibility = View.VISIBLE
        miscellaneousButton.isClickable = true
        miscellaneousButton.show()
        sweetsLayout.visibility = View.VISIBLE
        sweetsButton.isClickable = true
        sweetsButton.show()
        syrupsLayout.visibility = View.VISIBLE
        syrupsButton.isClickable = true
        syrupsButton.show()
        ingredients_recycler_view.alpha = 0.4F
        isAddMenuOpen = true
    }

    /*private fun setupKeyboardListener() {
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
    }*/
}
