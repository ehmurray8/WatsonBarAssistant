package com.speakeasy.watsonbarassistant

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenu : AppCompatActivity() {

    var ingredients = mutableListOf<Ingredient>()
    var recipes = mutableListOf<MutableList<Recipe>>()
    var homeCategories = mutableListOf<String>()
    var documentsMap = mutableMapOf<String, String>()
    var currentUser: FirebaseUser? = null
    var tabIndex = 0
    private var fragment: Fragment? = null

    private val fireStore = FirebaseFirestore.getInstance()
    private var authorization = FirebaseAuth.getInstance()

    init {
        addDefaultRecipes()
        homeCategories.add("Suggestions")
        homeCategories.add("Recently Viewed")
    }

    private fun addDefaultRecipes() {
        recipes.add(mutableListOf())
        recipes.add(mutableListOf())
        val bloodyMaryIngredients = arrayOf("Tabasco", "Salt", "3 parts Vodka", "Pepper",
                "Worcestershire Sauce", "6 parts Tomato Juice", "1 part Lemon Juice").asList()
        recipes[0].add(Recipe("Bloody Mary", R.mipmap.ic_bloody_mary, bloodyMaryIngredients))

        val mojitoIngredients = arrayOf("6 Leaves of Mint", "2 Teaspoons Sugar",
                "2 Parts White Rum", "1 oz. Fresh Lime Juice", "Soda Water").asList()
        recipes[0].add(Recipe("Mojito", R.mipmap.ic_mojito, mojitoIngredients))

        val oldFashionedIngrdients = arrayOf("1 Sugar Cube", "2 Parts Bourbon",
                "Few Dashes Plain Water", "2 Dashes Angostura Bitters").asList()
        recipes[0].add(Recipe("Old Fashioned", R.mipmap.ic_old_fashioned, oldFashionedIngrdients))

        val margaritaIngredients = arrayOf("1 oz Cointreau", "1 oz Lime Juice", "2 oz Tequila").asList()
        recipes[0].add(Recipe("Margarita", R.mipmap.ic_margarita, margaritaIngredients))
        recipes[0].reversed().forEach { recipes[1].add(it) }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadUserData()
        setContentView(R.layout.activity_main_menu)

        tabs.addOnTabSelectedListener(MainMenuTabListener(this))
        setSupportActionBar(toolbar as Toolbar)
    }

    private fun loadUserData() {
        currentUser = authorization.currentUser
        loadIngredients()
    }

    fun loadIngredients() {
        val uid = currentUser?.uid
        ingredients.clear()
        if(uid != null) {
            fireStore.collection("app").document(uid)
                    .collection("ingredients").get().addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result.forEach { snapshot ->
                        parseSnapshot(snapshot)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        showCurrentFragment()
    }

    fun showCurrentFragment() {
        when(tabIndex) {
            0 -> fragment = HomeTab()
            1 -> fragment = AddTab()
            2 -> fragment = RecipesTab()
        }
        replaceFragment()
    }

    private fun replaceFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    private fun parseSnapshot(snapshot: QueryDocumentSnapshot) {
        val name = snapshot.get("name") as? String
        val id = snapshot.id
        if(name != null) {
            documentsMap[name] = id
            val ingredient = Ingredient(name)
            ingredients.add(ingredient)
            ingredients.sortBy {
                it.name
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.user_profile) {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
