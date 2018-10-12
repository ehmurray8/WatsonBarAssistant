package com.speakeasy.watsonbarassistant

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.design.widget.TabItem
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.gson.Gson
import com.speakeasy.watsonbarassistant.Discovery.HandleDiscovery
import com.speakeasy.watsonbarassistant.Discovery.SearchDiscovery
import com.speakeasy.watsonbarassistant.Recipe.MyRecipesTab
import kotlinx.android.synthetic.main.activity_main_menu.*
import java.util.*


class MainMenu : AppCompatActivity() {

    var permissionToRecordAccepted = false
    var ingredients = mutableListOf<Ingredient>()
    var recipes = mutableListOf<MutableList<DiscoveryRecipe>>()
    var documentsMap = mutableMapOf<String, String>()
    var currentUser: FirebaseUser? = null
    var tabIndex = 1
    var fragment: Fragment? = null

    private var tabsItems: Array<TabItem>? = null

    private val fireStore = FirebaseFirestore.getInstance()
    private var authorization = FirebaseAuth.getInstance()
    private var lastDiscoveryRefreshTime = -1L


    companion object {
        var homeCategories = mutableListOf("Suggestions", "Recently Viewed")
    }

    init {
        homeCategories.forEach { _ ->
            recipes.add(mutableListOf())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        loadSharedPreferences()
        loadUserData()
        tabs.getTabAt(tabIndex)?.select()
        tabs.addOnTabSelectedListener(MainMenuTabListener(this))
        setSupportActionBar(toolbar as Toolbar)


        //TODO does this go here?
        //Checking for audio access
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("Main Menu", "Permission to record denied")
            makeRequest()

            if (!BarAssistant.isInternetConnected()) {
                Toast.makeText(baseContext, "Failed to download user data from the internet.", Toast.LENGTH_SHORT).show()

            }
        }
    }
        private fun loadSharedPreferences() {
            val preferences = getSharedPreferences(SHARED_PREFERENCES_SETTINGS, Context.MODE_PRIVATE)
            tabIndex = preferences.getInt(TAB_INDEX, 1)
            val gson = Gson()
            homeCategories.forEachIndexed { i, category ->
                val recipeJson = preferences.getString(category, "")
                val storedRecipes = gson.fromJson(recipeJson, Array<DiscoveryRecipe>::class.java)
                val ingredientsJson = preferences.getString(INGREDIENT_PREFERENCES_ID, "")
                val storedIngredients = gson.fromJson(ingredientsJson, Array<Ingredient>::class.java)
                if (storedRecipes != null && storedRecipes.count() > 0) {
                    recipes[i].addAll(storedRecipes.toList())
                }
                if (storedIngredients != null && storedIngredients.count() > 0) {
                    ingredients.addAll(storedIngredients)
                }
            }
        }

        override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.main_toolbar_menu, menu)
            return true
        }

        override fun onPause() {
            super.onPause()
            val preferences = getSharedPreferences(SHARED_PREFERENCES_SETTINGS, Context.MODE_PRIVATE)
            val editor = preferences.edit()
            val gson = Gson()
            homeCategories.forEachIndexed { i, category ->
                val json = gson.toJson(recipes[i].toTypedArray())
                editor.putString(category, json)
            }
            val ingredientJson = gson.toJson(ingredients.toTypedArray())
            editor.putString(INGREDIENT_PREFERENCES_ID, ingredientJson)
            editor.apply()
        }

        private fun loadUserData() {
            currentUser = authorization.currentUser
            loadIngredients()
        }

        private fun loadIngredients() {
            if (BarAssistant.isInternetConnected()) {
                val uid = currentUser?.uid
                refreshDiscovery()
                val oldIngredients = ingredients.toTypedArray()
                if (uid != null) {
                    fireStore.collection("app").document(uid)
                            .collection("ingredients").get().addOnCompleteListener {
                                ingredients.clear()
                                if (it.isSuccessful) {
                                    it.result?.forEach { snapshot ->
                                        parseSnapshot(snapshot)
                                    }
                                    if (!oldIngredients.toMutableList().containsAll(ingredients)) {
                                        refreshDiscovery(true)
                                    }
                                }
                            }
                }
            }
        }

        fun refreshDiscovery(forceRefresh: Boolean = false) {
            if (ingredients.count() > 0) {
                if (forceRefresh || lastDiscoveryRefreshTime == -1L ||
                        Date().time - lastDiscoveryRefreshTime >= 60_000) {
                    lastDiscoveryRefreshTime = Date().time
                    val discovery = SearchDiscovery(HandleDiscovery(recipes, this))
                    discovery.execute(ingredients.toTypedArray())
                    Log.d("Discovery", "Refreshing Discovery...")
                }
            }
        }

        override fun onStart() {
            super.onStart()
            showCurrentFragment()
        }

        fun showCurrentFragment() {
            when (tabIndex) {
                0 -> fragment = IngredientsTab()
                1 -> fragment = HomeTab()
                2 -> fragment = MyRecipesTab()
            }
            replaceFragment()
        }

        private fun replaceFragment() {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment ?: return)
            transaction.commit()
            tabsItems?.get(tabIndex)?.isSelected = true
        }

        private fun parseSnapshot(snapshot: QueryDocumentSnapshot) {
            val name = snapshot.get("name") as? String
            val id = snapshot.id
            if (name != null) {
                documentsMap[name] = id
                val ingredient = Ingredient(name)
                ingredients.add(ingredient)
                ingredients.sortBy { it.name.toLowerCase().replace("\\s".toRegex(), "") }
            }
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            if (item.itemId == R.id.user_profile) {
                val intent = Intent(this, UserProfile::class.java)
                startActivity(intent)
                return true
            }
            return super.onOptionsItemSelected(item)
        }

        fun showMicText(text: String) {
            runOnUiThread {
                fun run() {
                    //inputMessage.setText(text)
                }
            }
        }

        fun enableMicButton(btnRecord: ImageButton) {
            runOnUiThread {
                @Override
                fun run() {
                    btnRecord.setEnabled(true)
                }
            }
        }

        fun showError(e: Exception) {
            runOnUiThread {
                @Override
                fun run() {
                    Toast.makeText(this@MainMenu, e.message, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }


        private fun makeRequest() {
            var permission = Array(32) { "Manifest.permission.RECORD_AUDIO" }
            ActivityCompat.requestPermissions(this, permission, RECORD_REQUEST_CODE)
        }

    // Speech-to-Text Record Audio permission
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> {
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            }
            RECORD_REQUEST_CODE -> {
                if (grantResults.size == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("Speech to Text", "Permission has been denied by user")
                } else {
                    Log.i("Speech to Text", "Permission has been granted by user")
                }
                return
            }
        }
        if (!permissionToRecordAccepted) finish()
    }

}

