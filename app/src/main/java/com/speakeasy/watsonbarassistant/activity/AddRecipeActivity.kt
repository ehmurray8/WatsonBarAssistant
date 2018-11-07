package com.speakeasy.watsonbarassistant.com.speakeasy.watsonbarassistant.activity

import android.app.Activity
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
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import com.speakeasy.watsonbarassistant.CAMERA_PERMISSION_REQUEST_CODE
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.REQUEST_TAKE_PHOTO
import com.speakeasy.watsonbarassistant.VISION_URL
import com.speakeasy.watsonbarassistant.activity.MainMenu
import khttp.responses.Response
import kotlinx.android.synthetic.main.activity_add_recipe.*
import kotlinx.android.synthetic.main.activity_vision.*
import kotlinx.android.synthetic.main.fragment_ingredient_tab.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*

class AddRecipeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        newPic.setOnClickListener{
            if (!checkCameraPermission()) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            }
        }
/*
        title_text.setOnClickListener {
            ingredientInputView.visibility = View.VISIBLE
            ingredientInputView.setOnEditorActionListener { _, actionId, _ ->

                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        /*
                        val name = ingredientInputView.text.toString()
                        mainMenu.addIngredient(name)
                        refresh()
                        */
                        ingredientInputView.selectAll()
                        ingredientInputView.setText("")
                        true
                    }
                    else -> false
                }
            }
            ingredientInputView.post {
                ingredientInputView.requestFocus()
                ingredientInputView.setText("")
                ingredientInputView.setSelection(0)
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(ingredientInputView, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        */
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
            newPic.setImageBitmap(bitmap)

            /*
            val encoded = encodeBitmapToBase64(bitmap)
            val jImgContent = JSONObject()
            jImgContent.put("content", encoded)
            val jRequestsBody = JSONObject()
            jRequestsBody.put("image", jImgContent)
            val jType = JSONObject(mapOf("type" to "WEB_DETECTION", "maxResults" to 10))
            val jFeaturesArray = JSONArray()
            jFeaturesArray.put(jType)
            val jRequestsArray = JSONArray()

            jRequestsBody.put("features", jFeaturesArray)
            jRequestsArray.put(jRequestsBody)
            val jRequest = JSONObject(mapOf("requests" to jRequestsArray))

            khttp.async.post(VISION_URL, data=jRequest, onResponse={
                handleVisionResponse(this)
            })
            */
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainMenu::class.java)
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encodeBitmapToBase64(bitmap: Bitmap):String{
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,30, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.getEncoder().encodeToString(byteArray)
    }
}