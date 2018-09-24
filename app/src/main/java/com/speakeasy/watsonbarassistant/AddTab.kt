package com.speakeasy.watsonbarassistant


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore


class AddTab : Fragment() {

    private val fireStore = FirebaseFirestore.getInstance()
    private var addButton: Button? = null
    private var ingredientInput: EditText? = null

    private val CAMERA_REQUEST = 1888

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_add_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addButton = view.findViewById(R.id.add_ingredient_button)
        addButton?.setOnClickListener {
            handleAddButton()
        }
        val cameraAdd = view.findViewById<ImageButton>(R.id.camera_button)
        val speechAdd = view.findViewById<ImageButton>(R.id.speech_button)
        val discoveryButton = view.findViewById<Button>(R.id.discoveryButton)

        cameraAdd?.setOnClickListener {
            handleCameraAdd()
        }
        speechAdd?.setOnClickListener {
            handleSpeechAdd()
        }
        discoveryButton?.setOnClickListener {
            handleDiscoveryButton()
        }
    }

    private fun handleDiscoveryButton() {
    }

    private fun handleAddButton() {
        ingredientInput = view?.findViewById<EditText>(R.id.add_ingredient_input)
        val inputManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(ingredientInput?.windowToken, 0)
        val name = ingredientInput?.text.toString()
        val ingredient = Ingredient(name)
        addIngredient(ingredient)
    }

    private fun handleCameraAdd() {
        val mainMenu = activity as MainMenu
        mainMenu.loadIngredients()
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST)
    }

    private fun handleSpeechAdd() {
        val mainMenu = activity as MainMenu
        Handler().postDelayed({
            Toast.makeText(activity, "Successfully added Tito's.", Toast.LENGTH_SHORT).show()
        }, 3500)
        mainMenu.loadIngredients()
    }

    private fun addIngredient(ingredient: Ingredient) {
        val ingredients = (activity as MainMenu).ingredients
        if(ingredient in ingredients) {
            Toast.makeText(activity, "${ingredient.name} is already stored as an ingredient.", Toast.LENGTH_SHORT).show()
            return
        }
        addIngredientToFireStore(ingredient)
    }

    private fun addIngredientToFireStore(ingredient: Ingredient) {
        val mainMenu = (activity as? MainMenu)
        val uid = mainMenu?.currentUser?.uid ?: return
        fireStore.collection("app").document(uid)
                .collection("ingredients").add(ingredient).addOnSuccessListener { _ ->
            try {
                Toast.makeText(activity, "Successfully added ${ingredient.name}.", Toast.LENGTH_SHORT).show()
            } catch(exception: NullPointerException) { }
            ingredientInput?.text?.clear()
            mainMenu.loadIngredients()
        }.addOnFailureListener {
            try {
                Toast.makeText(activity, "Failed to add ${ingredient.name}.", Toast.LENGTH_SHORT).show()
            }
            catch(exception: NullPointerException) { }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            val photo = data.extras.get("data") as Bitmap
            val intent = Intent(activity, ImageViewer::class.java)
            intent.putExtra("Image", photo)
            startActivity(intent)
        }
    }
}
