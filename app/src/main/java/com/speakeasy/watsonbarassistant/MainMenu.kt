package com.speakeasy.watsonbarassistant

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot


const val USERNAME = "test@gmail.com"
const val PASSWORD = "test123"

class MainMenu : AppCompatActivity() {

    var ingredients = mutableListOf<Ingredient>()
    var currentUser: FirebaseUser? = null
    var tabIndex = 0

    private val fireStore = FirebaseFirestore.getInstance()
    private var authorization = FirebaseAuth.getInstance()
    private var fragment: Fragment? = null

    init {
        authorization.signOut()
        authorizeUser()
    }

    private fun authorizeUser() {
        authorization.signInWithEmailAndPassword(USERNAME, PASSWORD).addOnCompleteListener {
            if(it.isSuccessful) {
                loadUserData()
            } else {
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
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

        showCurrentFragment()

        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.addOnTabSelectedListener(MainMenuTabListener(this))
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
                }
                (fragment as? HomeTab)?.displayIngredients()
            }
        }
    }

    private fun parseSnapshot(snapshot: QueryDocumentSnapshot) {
        val name = snapshot.get("name") as? String
        if(name != null) {
            val ingredient = Ingredient(name)
            ingredients.add(ingredient)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        authorization.signOut()
    }
}
