package com.speakeasy.watsonbarassistant.com.speakeasy.watsonbarassistant.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.*
import kotlinx.android.synthetic.main.activity_add_recipe.*
import java.io.ByteArrayOutputStream

class AddRecipeActivity : AppCompatActivity() {
    var newImageBitmap: Bitmap? = null
    var titleNotNull = false
    var descriptionNotNull = false
    var pictureNotDefualt = false
    var ingredientsUnique = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        addRecipeButton.visibility = View.INVISIBLE

        title_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                setTitleTextState(!p0.isNullOrEmpty())
            }
        })

        description_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                setDescriptionTextState(!p0.isNullOrEmpty())
            }
        })

        newPic.setOnClickListener{
            if (!checkCameraPermission()) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            }
        }

        addRecipeButton.setOnClickListener {
            if (this.newImageBitmap != null) {
                addRecipe()
                this.finish()
            } else {
                //Toast.makeText(Context,"Add a Picture!",Toast.LENGTH_SHORT)
                Log.i("AddRecipeActivity", "Add a new recipe.")
            }
        }
    }

    private fun setTitleTextState(state: Boolean){
        this.titleNotNull = state
        Log.i("AddRecipeActivity", "Title state: " + this.titleNotNull.toString())
        //TODO check title uniquness
        setAddButtonVisibility()
    }

    private fun setDescriptionTextState(state: Boolean){
        this.descriptionNotNull = state
        Log.i("AddRecipeActivity", "Description state: " + this.descriptionNotNull.toString())
        setAddButtonVisibility()
    }

    private fun setAddButtonVisibility(){
        val buttonVisible = this.ingredientsUnique and this.titleNotNull and this.descriptionNotNull and this.pictureNotDefualt
        if (buttonVisible){
            addRecipeButton.visibility = View.VISIBLE
        } else {
            addRecipeButton.visibility = View.INVISIBLE
        }
    }

    private fun addRecipe(){
        Log.i("AddRecipeActivity","Adding the recipe.")
        val assistant = BarAssistant()

        val stream = ByteArrayOutputStream()
        this.newImageBitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val newImageByteArray = stream.toByteArray()

        val newImageId = assistant.addNewImageToFireStore(newImageByteArray)

        if (newImageId == "-1"){
            //TODO failed to save image message
        }else{
            val newRecipe = DiscoveryRecipe(title = title_text.text.toString(), description = description_text.text.toString(), imageId = newImageId, ingredientList = emptyList())
            Log.i("AddRecipeActivity",newRecipe.toString())
            assistant.storeNewRecipeInFireStore(FirebaseAuth.getInstance(),FirebaseFirestore.getInstance(), newRecipe)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also { _ ->
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(baseContext, android.Manifest.permission_group.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            this.newImageBitmap = bitmap
            this.pictureNotDefualt = true
            newPic.setImageBitmap(bitmap)
            setAddButtonVisibility()
        }
    }
}