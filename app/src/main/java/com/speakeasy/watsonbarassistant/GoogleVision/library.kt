package com.speakeasy.watsonbarassistant.GoogleVision

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import java.util.jar.Manifest

class GoogleVisionAPI(var activity:Activity){

    val CLOUD_VISION_API_KEY = "c5c155f5bafb7667300b9a28967099272e5b95a6"
    val FILE_NAME = "temp.jpg"
    val GALLERY_PERMISSIONS_REQUEST = 0;
    val GALLERY_IMAGE_REQUEST = 1;
    val CAMERA_PERMISSION_REQUEST = 2
    val CAMERA_IMAGE_REQUEST = 3;

    fun startCamera(){

        if(PermissionUtils.requestPermission(activity,CAMERA_PERMISSION_REQUEST, android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.CAMERA)){
            var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoUri = FileProvider.getUriForFile(activity)


        }

    }

}