package com.speakeasy.watsonbarassistant.GoogleVision

import android.app.Activity
import android.app.Fragment
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.speakeasy.watsonbarassistant.R
import kotlinx.android.synthetic.main.test_vision.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import khttp.*
import org.json.JSONArray
import org.json.JSONObject

class VisionTab : AppCompatActivity(), View.OnClickListener  {

    val REQUEST_TAKE_PHOTO = 1
    private val CROP_PHOTO = 2

    lateinit private var imageUri:Uri
    lateinit private var picture:ImageView
    lateinit private var takePhoto:Button
    lateinit private var mCurrentPhotoPath:String

    val visionAPI = "AIzaSyCTLNIEqcF99egsLSplk4Y_vucWdRd3ttQ"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_vision)

        takePhoto = findViewById<Button>(R.id.take_photo)
        picture = findViewById<ImageView>(R.id.picture)

        takePhoto.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission_group.STORAGE)
        != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission_group.STORAGE),
                        4)
        }

        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission_group.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission_group.CAMERA),
                    5)
        }

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    imageUri = FileProvider.getUriForFile(
                            this,
                            "com.speakeasy.watsonbarassistant",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    @Throws (IOException::class)
    private fun createImageFile(): File{
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }



//    private fun openCamera(activity:Activity) {
//        var currentVersion = android.os.Build.VERSION.SDK_INT
//        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//
//        if (currentVersion<24){
//            imageUri = Uri.fromFile(tempFile)
//            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
//        }else{
//            var contentValues = ContentValues(1)
//            contentValues.put(MediaStore.Images.Media.DATA, tempFile.absolutePath)
//            if (ContextCompat.checkSelfPermission(activity as Context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
//                ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),102)
//            }
//            imageUri = activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
//        }
//        activity.startActivityForResult(intent,TAKE_PHOTO)

//        var outputImage = File(Environment.getExternalStorageDirectory(),"tempImage.jpg")
//
//        //Delete or Create
//        if (outputImage.exists()) {
//            outputImage.delete()
//        }
//        outputImage.createNewFile()
//
//        imageUri = Uri.fromFile(outputImage)
//        var intent = Intent("android.media.action.IMAGE_CAPTURE")
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
//        startActivityForResult(intent, TAKE_PHOTO)
//    }
//

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode==REQUEST_TAKE_PHOTO){
//            if (resultCode== Activity.RESULT_OK){
//                var intent = Intent("com.android.camera.action.CROP")
//                intent.setDataAndType(imageUri,"image/*")
//                intent.putExtra("scale",true)
//                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
//                startActivityForResult(intent, CROP_PHOTO)
//            }
//        }
//
//        if (requestCode==CROP_PHOTO){
//            if (resultCode == Activity.RESULT_OK){
//                var bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
//                picture.setImageBitmap(bitmap)
//            }
//        }

        if (requestCode == REQUEST_TAKE_PHOTO){
            if (resultCode == Activity.RESULT_OK){
                var bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
                picture.setImageBitmap(bitmap)

                val encoded = encodeBitmapToBase64(bitmap)

                var jImgContent = JSONObject()
                jImgContent.put("content",encoded)
                var jRequestsBody = JSONObject()
                jRequestsBody.put("image",jImgContent)

                var jType = JSONObject()
                jType.put("type","WEB_DETECTION")
                jType.put("maxResults",10)

                var jFeaturesArray = JSONArray()
                jFeaturesArray.put(jType)
                jRequestsBody.put("features",jFeaturesArray)

                var jRequestsArray = JSONArray()
                jRequestsArray.put(jRequestsBody)

                var jRequest = JSONObject()
                jRequest.put("requests",jRequestsArray)


                khttp.async.post("https://vision.googleapis.com/v1/images:annotate?key="+visionAPI,data=jRequest, onResponse={
                    Log.i("Response","$statusCode")
                    Log.i("Response","$text")
                })




            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encodeBitmapToBase64(bitmap: Bitmap):String{
        var byteArrayOutputStream :ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val encoded : String = Base64.getEncoder().encodeToString(byteArray)

        return encoded
    }

}