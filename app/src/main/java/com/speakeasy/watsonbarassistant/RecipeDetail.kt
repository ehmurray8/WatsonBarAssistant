package com.speakeasy.watsonbarassistant

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.R.layout.activity_recipe_detail
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_recipe_detail.*
import kotlinx.android.synthetic.main.activity_recipe_detail.view.*
import java.util.ArrayList

class RecipeDetail : AppCompatActivity() {

    private val picasso = Picasso.get()
    private var favorited: Boolean = false
    private lateinit var favoriteAnim: Animation
    private val favoritedItems = mutableListOf<DiscoveryRecipe>()

    private var viewAdapter: MyRecipeAdapter? = null
    private var authorization = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()

    @Override
    override fun onPause() {
        super.onPause()
        updateFirebaseFavoriteStatus()
    }

    @Override
    override fun onStop() {
        super.onStop()
        updateFirebaseFavoriteStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        activity_recipe_detail.apply{

        }
        val viewManager = LinearLayoutManager(activity)
        //loadFromFireStore()

        val recipe = intent.getSerializableExtra("Recipe") as? DiscoveryRecipe

        val recipeTitle = findViewById<TextView>(R.id.recipe_title)
        recipeTitle.text = recipe?.title

        var recipeIngredientsString = ""
        recipe?.ingredientList?.forEachIndexed { i, element ->
            recipeIngredientsString += "${i+1}. $element\n"
        }
        recipe_ingredients.text = recipeIngredientsString
        description_content.text = recipe?.description

        loadImage(assets, drink_detail_image, recipe, picasso)

        favoriteAnim = AnimationUtils.loadAnimation(baseContext, R.anim.anim_favorite)
        //recipe.queueValue
        /* Set favorite button after checking if recipe is favorited or not */
        for (item: DiscoveryRecipe in favoritedItems) {
            if (recipeTitle.text == item.title) { // If drink name on list, set button_favorite to favorited
                button_favorite.startAnimation(favoriteAnim)
                favorited = true
            }
        }

        /* NOT favorited previously */
        button_favorite.setOnClickListener{
            if (!favorited){
                favorited = true
                button_favorite.startAnimation(favoriteAnim)
                Toast.makeText(baseContext, "Added to Favorites", Toast.LENGTH_SHORT).show()
            }
            else{
                favorited = false
                Toast.makeText(baseContext, "Removed from Favorites", Toast.LENGTH_SHORT).show()
            }
            // Wait ~15 (?) seconds before putting on to firebase favorites list
        }

        /* Or wait until page is navigated away from/ app is closed (???) */
    }

   private fun updateFirebaseFavoriteStatus(recipe: Int){

        //Send to firebase
       val mainMenu = (activity as? MainMenu)
       val uid = mainMenu?.currentUser?.uid ?: return
       fireStore.collection("app").document(uid)
               .collection("favorites").add(recipe).addOnSuccessListener { _ ->
                   mainMenu.favorites.add(recipe)
                   mainMenu.favorites.sortBy { it.number) }
                   //refresh()
               }

    }


    /*private fun loadFromFireStore() {
        val uid = authorization.currentUser?.uid
        if(uid != null) {
            fireStore.collection(MAIN_COLLECTION).document(uid).collection(FAVORITES_COLLECTION)
                    .document("main").get().addOnSuccessListener {
                        val favorites = it.get(FAVORITES) as? ArrayList<*>
                        favorites?.forEachIndexed { i, element ->
                            val favorite = DiscoveryRecipe(element as String)
                            if(!orderedItems.contains(favorite)) {
                                orderedItems.add(favorite)
                                shoppingCartItems[favorite] = (neededList?.get(i) as? Boolean) ?: true
                            }
                        }
                        viewAdapter?.notifyDataSetChanged()
                    }
        }
    }*/

}
