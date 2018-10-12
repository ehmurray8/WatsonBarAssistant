package com.speakeasy.watsonbarassistant

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.common.util.ByteConstants
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class BarAssistant: Application() {

    companion object {
        var defaultImage: Drawable? = null
        var networkInfo: NetworkInfo? = null
        var storageReference: StorageReference? = null

        fun isInternetConnected(): Boolean {
            return networkInfo?.isConnectedOrConnecting == true
        }
    }

    override fun onCreate() {
        super.onCreate()
        setupFresco()
        defaultImage = Drawable.createFromStream(assets.open(DEFAULT_RECIPE_IMAGE_NAME), null)
        networkInfo = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        storageReference = FirebaseStorage.getInstance().reference
    }

    private fun setupFresco() {
        val diskCacheConfig = DiskCacheConfig.newBuilder(baseContext).setBaseDirectoryPath(baseContext.cacheDir)
            .setBaseDirectoryName("v1").setMaxCacheSize(100 * ByteConstants.MB.toLong())
            .setMaxCacheSizeOnLowDiskSpace(10 * ByteConstants.MB.toLong())
            .setMaxCacheSizeOnVeryLowDiskSpace(5 * ByteConstants.MB.toLong()).setVersion(1).build()
        val imagePipelineConfig = ImagePipelineConfig.newBuilder(baseContext)
            .setMainDiskCacheConfig(diskCacheConfig).build()
        Fresco.initialize(baseContext, imagePipelineConfig)
    }
}