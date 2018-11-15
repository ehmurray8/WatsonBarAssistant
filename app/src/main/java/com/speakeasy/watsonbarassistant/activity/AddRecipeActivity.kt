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
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.algolia.search.saas.Client
import com.algolia.search.saas.Query
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.*
import com.speakeasy.watsonbarassistant.extensions.toast
import kotlinx.android.synthetic.main.activity_add_recipe.*
import kotlinx.serialization.json.JSON
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
            override fun afterTextChanged(p0: Editable?) { }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

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
            } else { }
        }
    }

    private fun setTitleTextState(state: Boolean){
        this.titleNotNull = state
        //TODO check title uniquness
        val algoliaClient = Client(ALGOLIA_APP_ID, ALGOLIA_API_KEY)
        val algoliaIndex = algoliaClient.getIndex("Recipe")
        algoliaIndex.searchAsync(Query(title_text.text.toString())) { content, error ->
            if(error != null) {
                toast("Failed to add recipe, please try again.")
            }
            if (error == null && content != null) {
                val response = content.getJSONArray("hits")
                if (response.length() > 0) {
                    for (i in 0 until response.length()) {
                        val recipe = JSON.nonstrict.parse<DiscoveryRecipe>(response.getJSONObject(i).toString())
                        if(recipe.title == title_text.text.toString()) {
                            titleNotNull = false
                            AlertDialog.Builder(this).setCancelable(true)
                                    .setMessage("Title is not unique, please add a new recipe title.")
                                    .setTitle("Recipe Add Error").show()
                        }
                    }
                }
                if(titleNotNull) {
                    setAddButtonVisibility()
                }
            }
        }
    }

    private fun setDescriptionTextState(state: Boolean){
        this.descriptionNotNull = state
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
        val assistant = BarAssistant()

        val stream = ByteArrayOutputStream()
        this.newImageBitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val newImageByteArray = stream.toByteArray()

        val newImageId = assistant.addNewImageToFireStore(newImageByteArray)

        if (newImageId == "-1"){
            //TODO failed to save image message
        }else{
            val newRecipe = DiscoveryRecipe(title = title_text.text.toString(), description = description_text.text.toString(), imageId = newImageId, ingredientList = emptyList())
            assistant.storeNewRecipeInFireStore(FirebaseAuth.getInstance(),FirebaseFirestore.getInstance(), newRecipe)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //PictureDialog.cameraHandler = {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    takePictureIntent.resolveActivity(packageManager)?.also { _ ->
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                    }
                }
            //}
            //PictureDialog.fileHandler = {
            //    val pickPhoto = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            //    startActivityForResult(pickPhoto , 8)
            //}
            //PictureDialog().show(fragmentManager, "Picture Dialog")
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
        } else if (requestCode == 8 && resultCode == Activity.RESULT_OK) {
            val selectedImage = data?.data
            newPic.setImageURI(selectedImage)
        }
    }
}