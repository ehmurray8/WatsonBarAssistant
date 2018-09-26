package com.speakeasy.watsonbarassistant

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.*
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.android.synthetic.main.fragment_my_recipes_tab.*

class MyRecipesTab : Fragment() {

    var recipes = mutableListOf<Recipe>()
    private var viewAdapter: MyRecipeAdapter? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):View? {
        return inflater.inflate(R.layout.fragment_my_recipes_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val viewManager = LinearLayoutManager(activity)
        val mainMenu = activity as MainMenu
//        val discoverList = mutableListOf<DiscoveryRecipe>(DiscoveryRecipe(title = "Drink",description = "Ante ullamcorper pharetra tempus ut purus mauris enim integer, felis at placerat lorem dignissim velit tincidunt, sociis rutrum senectus ad augue tortor porttitor."),DiscoveryRecipe(title = "Drink",description = "Interesting Drink to make"), DiscoveryRecipe(title = "Third Drink"))
        viewAdapter = MyRecipeAdapter(mainMenu.recipes[0],activity as Activity)

        recyclerView = recipes_list.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        recyclerView?.addItemDecoration(itemDecorator)

        setupOnClickListener()
    }

    private fun setupOnClickListener() {
        recyclerView?.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val intent = Intent(activity, RecipeDetail::class.java)
                val recipes = (activity as MainMenu).recipes
                intent.putExtra("Recipe", recipes[0][position])
                startActivity(intent)
            }
        })
    }
}