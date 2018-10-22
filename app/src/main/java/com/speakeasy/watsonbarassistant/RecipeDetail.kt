package com.speakeasy.watsonbarassistant

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.method.TextKeyListener.clear
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.R.id.button_favorite
import com.speakeasy.watsonbarassistant.R.layout.activity_recipe_detail
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_recipe_detail.*
import kotlinx.android.synthetic.main.activity_recipe_detail.view.*
import java.util.ArrayList

class RecipeDetail : AppCompatActivity() {

    private val picasso = Picasso.get()
    var favorited: Boolean = false
    lateinit var favoriteAnim: Animation

    var viewAdapter: MyRecipeAdapter? = null
    var authorization = FirebaseAuth.getInstance()
    var fireStore = FirebaseFirestore.getInstance()
    lateinit var favorite: Favorite
    var favorites = mutableListOf<Favorite>()

    @Override
    override fun onPause() {
        super.onPause()
        updateFirebaseFavoriteStatus(favorite)
    }

    @Override
    override fun onStop() {
        super.onStop()
        updateFirebaseFavoriteStatus(favorite)
    }

    @Override
    override fun onDestroy() {
        super.onDestroy()
        updateFirebaseFavoriteStatus(favorite)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_recipe_detail)

        activity_recipe_detail.apply{

        }

        loadFavoritesFromFireStore()
        checkIfFavorited(favorite.name)

        val recipe = intent.getSerializableExtra("Recipe") as? DiscoveryRecipe

        val recipeTitle = findViewById<TextView>(R.id.recipe_title)
        recipeTitle.text = recipe?.title

        favorite.name = recipeTitle.text.toString()

        var recipeIngredientsString = ""
        recipe?.ingredientList?.forEachIndexed { i, element ->
            recipeIngredientsString += "${i+1}. $element\n"
        }
        recipe_ingredients.text = recipeIngredientsString
        description_content.text = recipe?.description

        loadImage(assets, drink_detail_image, recipe, picasso)

        favoriteAnim = AnimationUtils.loadAnimation(baseContext, R.anim.anim_favorite)

        /* Set favorite button after checking if recipe is favorited or not */
        checkIfFavorited(favorite.name)

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

   private fun updateFirebaseFavoriteStatus(favorite: Favorite){

        //Send to firebase

       val uid = authorization.currentUser?.uid ?: return
       fireStore.collection("app").document(uid)
               .collection("favorites").add(favorite).addOnSuccessListener { _ ->
                   favorites.add(favorite)
                   favorites.sortBy { it.name }
                   //refresh()
               }

    }


    private fun loadFavoritesFromFireStore() {
        val uid = authorization.currentUser?.uid
        if(uid != null) {
            fireStore.collection("app").document(uid).collection("favorites")
                    .document("main").get().addOnSuccessListener {
                        val favorites = it.get("favorites") as? MutableList<Favorite>
                        favorites?.forEachIndexed { _, element ->
                            favorite = Favorite(element as String)
                            if(!favorites.contains(favorite)) {
                                favorites.add(favorite)
                            }
                        }
                        viewAdapter?.notifyDataSetChanged()
                    }
        }
    }

    private fun checkIfFavorited(recipeName: String){


        for (item: Favorite in favorites) {
            if (recipeName.equals(item.name)) { // If drink name on list, set button_favorite to favorited
                button_favorite.startAnimation(favoriteAnim)
                favorited = true
            }
        }

    }

    /*private fun loadFavorites(favorites: ) {
        val favoritesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                favorites.clear()
                dataSnapshot.children.mapNotNullTo(favorites) { it.getValue<Favorite>(Favorite::class.java) }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }
        fireStore.child("favorites").addListenerForSingleValueEvent(favoritesListener)
    }*/

}
