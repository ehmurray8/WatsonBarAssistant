package com.speakeasy.watsonbarassistant.activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
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
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import com.algolia.search.saas.Client
import com.algolia.search.saas.Query
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.*
import com.speakeasy.watsonbarassistant.adapter.IngredientAdapter
import com.speakeasy.watsonbarassistant.extensions.addIngredientHandlers
import com.speakeasy.watsonbarassistant.extensions.closeIngredientRadial
import com.speakeasy.watsonbarassistant.extensions.openIngredientRadial
import com.speakeasy.watsonbarassistant.extensions.toast
import kotlinx.android.synthetic.main.activity_add_recipe.*
import kotlinx.serialization.json.JSON
import java.io.ByteArrayOutputStream
import java.util.*

class AddRecipeActivity : AppCompatActivity() {
    private var newImageBitmap: Bitmap? = null
    private var titleNotNull = false
    private var descriptionNotNull = false
    private var pictureNotDefualt = false
    private var ingredientsUnique = true

    private var menuAnimOpen: Animation? = null
    private var menuAnimClose: Animation? = null
    private var menuAnimRotateOut: Animation? = null
    private var menuAnimRotateBack: Animation? = null

    private var isAddMenuOpen = false

    private var viewAdapter: IngredientAdapter? = null


    companion object {
        var ingredients = mutableListOf<Ingredient>()
        var normalIngredients = mutableListOf<String?>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        val clearRecipe = intent.getBooleanExtra("Clear", false)
        if(clearRecipe) {
            ingredients.clear()
            normalIngredients.clear()
        }

        addRecipeButton.visibility = View.INVISIBLE

        title_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

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

        val linearManager = LinearLayoutManager(applicationContext)
        addRecipeIngredients.apply {
            setHasFixedSize(true)
            layoutManager = linearManager
        }

        newPic.setOnClickListener{
            if (!checkCameraPermission()) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            }
        }

        addMenuButtonAddRecipe.setOnClickListener {
            when {
                isAddMenuOpen -> closeMenus()
                else -> openMenus()
            }
        }

        addRecipeButton.setOnClickListener {
            if (this.newImageBitmap != null) {
                addRecipe()
                this.finish()
            } else { }
        }
        menuAnimOpen = AnimationUtils.loadAnimation(applicationContext, R.anim.menu_anim_open)
        menuAnimClose = AnimationUtils.loadAnimation(applicationContext, R.anim.menu_anim_close)
        menuAnimRotateOut = AnimationUtils.loadAnimation(applicationContext, R.anim.menu_anim_rotate_out)
        menuAnimRotateBack = AnimationUtils.loadAnimation(applicationContext, R.anim.menu_anim_rotate_back)
        closeMenus()
        addIngredientHandlers(this::addShoppingCartIngredients)
    }

    override fun onResume() {
        super.onResume()
        closeMenus()
    }

    private fun addShoppingCartIngredients(newIngredients: MutableList<Ingredient>, context: Context) {
        ingredients.addAll(newIngredients.filter { it.name != "" })
        normalIngredients.addAll(newIngredients.asSequence().map { it.name }.filter { it != "" })
        viewAdapter = IngredientAdapter(ingredients, normalIngredients, applicationContext)
        addRecipeIngredients.adapter = viewAdapter
        addRecipeIngredients.refreshDrawableState()
    }

    private fun setTitleTextState(state: Boolean){
        this.titleNotNull = state
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
        val assistant = application as? BarAssistant

        val stream = ByteArrayOutputStream()
        this.newImageBitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val newImageByteArray = stream.toByteArray()

        val newImageId = assistant?.addNewImageToFireStore(newImageByteArray)

        if (newImageId == "-1" || newImageId == null) {
            //TODO failed to save image message
        } else {
            val newRecipe = DiscoveryRecipe(title = title_text.text.toString(), description = description_text.text.toString(),
                    imageId = newImageId, ingredientList = ingredients.map { it.name }, normalIngredients = normalIngredients)
            assistant.storeNewRecipeInFireStore(FirebaseAuth.getInstance(),FirebaseFirestore.getInstance(), newRecipe)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            val dialog = Dialog(this)
			dialog.setContentView(R.layout.picture_dialog)
			dialog.setTitle("Recipe Image")

			val cameraButton = dialog.findViewById<ImageButton>(R.id.selectCameraImage)
            val fileButton = dialog.findViewById<ImageButton>(R.id.selectFileImage)

            cameraButton.setOnClickListener {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    takePictureIntent.resolveActivity(packageManager)?.also { _ ->
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                    }
                }
                dialog.dismiss()
            }

            fileButton.setOnClickListener {
                val pickPhoto = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhoto , 8)
                dialog.dismiss()
            }

			dialog.show()
        }
    }

    private fun setupSwipeHandler() {
         val swipeHandler = object : SwipeToDeleteCallback(baseContext) {

             override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
                return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT)
             }

             override fun isLongPressDragEnabled(): Boolean {
                 return true
             }

             override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                 val from = p1.adapterPosition
                 val to = p2.adapterPosition
                 viewAdapter?.notifyItemMoved(from, to)
                 Collections.swap(ingredients, from, to)
                 Collections.swap(normalIngredients, from, to)
                 return true
             }

             override fun onSwiped(p0: RecyclerView.ViewHolder, direction: Int) {
                 val position = p0.layoutPosition
                 ingredients.removeAt(position)
                 normalIngredients.removeAt(position)
                 viewAdapter?.notifyDataSetChanged()
             }
         }
         val itemTouchHelper = ItemTouchHelper(swipeHandler)
         itemTouchHelper.attachToRecyclerView(addRecipeIngredients)
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

    private fun closeMenus(){
        addMenuButtonAddRecipe.startAnimation(menuAnimRotateBack)
        closeIngredientRadial()
        addRecipeScroll.alpha = 1.0F
        isAddMenuOpen = false
        title_text.isEnabled = true
        description_text.isEnabled = true
    }

    private fun openMenus(){
        addMenuButtonAddRecipe.startAnimation(menuAnimRotateOut)
        addRecipeIngredientRadial.bringToFront()
        openIngredientRadial()
        addRecipeScroll.alpha = 0.4F
        isAddMenuOpen = true
        description_text.isEnabled = false
        title_text.isEnabled = false
    }
}