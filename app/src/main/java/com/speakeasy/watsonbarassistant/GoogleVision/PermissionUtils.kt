package com.speakeasy.watsonbarassistant.GoogleVision

import android.app.Activity
import android.content.pm.PackageManager
import android.support.annotation.IntegerRes
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

public class PermissionUtils() {
    companion object {
        public fun requestPermission(activity: Activity, requestCode: Int, vararg permissions: String): Boolean {
            var granted = true
            var permissionsNeeded = ArrayList<String>()

            for (permission in permissions) {
                var permissionCheck = ContextCompat.checkSelfPermission(activity, permission)
                var hasPermission = (permissionCheck == PackageManager.PERMISSION_GRANTED)
                granted = granted && hasPermission
                if (!hasPermission) {
                    permissionsNeeded.add(permission)
                }
            }

            if (granted) {
                return true
            } else {
                ActivityCompat.requestPermissions(activity, permissionsNeeded.toArray(arrayOfNulls<String>(permissionsNeeded.size)), requestCode)
                return false
            }

        }

        public fun permissionGranted(requestCode: Int, permissionCode:Int, grantResults: Array<Int>):Boolean{
            return requestCode == permissionCode && grantResults.size >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }

}

