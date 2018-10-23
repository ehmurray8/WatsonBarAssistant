package com.speakeasy.watsonbarassistant.vision

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
import android.widget.Button
import com.speakeasy.watsonbarassistant.*
import khttp.responses.Response
import kotlinx.android.synthetic.main.activity_vision.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*

class VisionActivity : AppCompatActivity() {

    companion object {
        var ingredientDelegate: IngredientDelegate? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vision)

        takePhoto.setOnClickListener{
            if (!checkCameraPermission()) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            }
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
            picture.setImageBitmap(bitmap)

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
        }
    }

    private fun handleVisionResponse(response: Response) {
        val responseList = ArrayList<String>()
        val responseJson = JSONObject(response.text)
        if (responseJson.has("responses")){
            val responseArray = responseJson.getJSONArray("responses")
            val responseObject = responseArray.getJSONObject(0)
            val webDetectionObject = responseObject.getJSONObject("webDetection")
            val webEntitiesArray = webDetectionObject.getJSONArray("webEntities")
            for (index in 0 until webEntitiesArray.length()){
                val item = webEntitiesArray.getJSONObject(index)
                if (item.has("description")){
                    responseList.add(item.getString("description"))
                }
            }
        }

        val size = if(responseList.size < 5) responseList.size else 5
        for (i in 0 until size){
            val button = choices.getChildAt(i) as Button
            button.setOnClickListener { _ ->
                val resultIntent = Intent(this@VisionActivity, MainMenu::class.java)
                ingredientDelegate?.addIngredient(responseList[i])
                this@VisionActivity.startActivity(resultIntent)
                finish()
            }
            runOnUiThread { kotlin.run { button.text = responseList[i] } }
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