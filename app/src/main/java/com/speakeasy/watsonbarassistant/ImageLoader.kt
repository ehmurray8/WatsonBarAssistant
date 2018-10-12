package com.speakeasy.watsonbarassistant

import android.content.Context
import android.net.Uri
import com.facebook.drawee.view.SimpleDraweeView


fun loadImage(context: Context, imageView: SimpleDraweeView, recipe: DiscoveryRecipe?) {
    imageView.hierarchy.setFailureImage(BarAssistant.defaultImage)
    imageView.hierarchy.setRetryImage(BarAssistant.defaultImage)
    if(recipe != null) {
        val imageUriString = recipe.recipeImageUriString
        if(imageUriString == "") {
            val imageReference = BarAssistant.storageReference?.child(recipe.getImageName())
            imageReference?.downloadUrl?.addOnSuccessListener {
                setImage(imageView, recipe, context, it)
                recipe.recipeImageUriString = it.toString()
            }
        } else {
            setImage(imageView, recipe, context)
        }
    } else {
        loadFailImage(context, imageView)
    }
}


private fun setImage(imageView: SimpleDraweeView, recipe: DiscoveryRecipe, context: Context,
                     recipeUri: Uri? = null) {
    imageView.setImageURI(recipeUri ?: Uri.parse(recipe.recipeImageUriString), context)
}


private fun loadFailImage(context: Context, imageView: SimpleDraweeView) {
    imageView.setImageURI(DEFAULT_IMAGE_URI, context)
}
