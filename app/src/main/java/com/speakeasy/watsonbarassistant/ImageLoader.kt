package com.speakeasy.watsonbarassistant

import android.content.res.AssetManager
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

private val storageReference = FirebaseStorage.getInstance().reference

fun loadImage(assets: AssetManager, imageView: ImageView, recipe: DiscoveryRecipe?, picasso: Picasso) {
    if(recipe != null) {
        val imageReference = storageReference.child(recipe.getImageName())
        val drawable = Drawable.createFromStream(assets.open(DEFAULT_RECIPE_IMAGE_NAME), null)
        imageReference.downloadUrl.addOnSuccessListener {
            picasso.load(it).error(drawable).into(imageView)
        }.addOnFailureListener {
            loadFailImage(picasso, imageView)
        }
    } else {
        loadFailImage(picasso, imageView)
    }
}


private fun loadFailImage(picasso: Picasso, imageView: ImageView) {
    picasso.load(DEFAULT_IMAGE_URI).into(imageView)
}
