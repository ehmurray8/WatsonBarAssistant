package com.speakeasy.watsonbarassistant

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.metrics.MetricsManager


const val USERNAME = "test@gmail.com"
const val PASSWORD = "test123"

class MainMenu : AppCompatActivity() {

    var ingredients = mutableListOf<Ingredient>()
    var recipes = mutableListOf<Recipe>()
    var documentsMap = mutableMapOf<String, String>()
    var currentUser: FirebaseUser? = null
    var tabIndex = 0

    private val fireStore = FirebaseFirestore.getInstance()
    private var authorization = FirebaseAuth.getInstance()
    private var fragment: Fragment? = null

    init {
        authorization.signOut()
        authorizeUser()
        addDefaultRecipes()
    }

    private fun addDefaultRecipes() {
        val bloodyMaryIngredients = arrayOf("Tabasco", "Salt", "3 parts Vodka", "Pepper",
                "Worcestershire Sauce", "6 parts Tomato Juice", "1 part Lemon Juice").asList()
        recipes.add(Recipe("Bloody Mary", R.mipmap.ic_bloody_mary, bloodyMaryIngredients))

        val mojitoIngredients = arrayOf("6 Leaves of Mint", "2 Teaspoons Sugar",
                "2 Parts White Rum", "1 oz. Fresh Lime Juice", "Soda Water").asList()
        recipes.add(Recipe("Mojito", R.mipmap.ic_mojito, mojitoIngredients))

        val oldFashionedIngrdients = arrayOf("1 Sugar Cube", "2 Parts Bourbon",
                "Few Dashes Plain Water", "2 Dashes Angostura Bitters").asList()
        recipes.add(Recipe("Old Fashioned", R.mipmap.ic_old_fashioned, oldFashionedIngrdients))

        val margaritaIngredients = arrayOf("1 oz Cointreau", "1 oz Lime Juice", "2 oz Tequila").asList()
        recipes.add(Recipe("Margarita", R.mipmap.ic_margarita, margaritaIngredients))
    }

    private fun authorizeUser() {
        authorization.signInWithEmailAndPassword(USERNAME, PASSWORD).addOnCompleteListener {
            if(it.isSuccessful) {
                loadUserData()
                Log.d("FIRESTORE", "Authentication success, you are logged in as" +
                        "${authorization.currentUser?.email}")
            } else {
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                Log.d("FIRESTORE", "Authentication failed, for user $USERNAME")
                Log.d("FIRESTORE", "Authentication failure: ${it.exception}")
            }
        }
    }

    private fun loadUserData() {
        currentUser = authorization.currentUser
        loadIngredients()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.addOnTabSelectedListener(MainMenuTabListener(this))
        MetricsManager.register(application)
    }

    override fun onResume() {
        super.onResume()
        checkForCrashes()
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
                } else {
                    Log.d("FIRESTORE", "Failed to load ingredients.")
                }
                (fragment as? HomeTab)?.refresh()
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
            ingredients.sortBy {
                it.name
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        authorization.signOut()
    }

    private fun checkForCrashes() {
        CrashManager.register(this)
    }
}
