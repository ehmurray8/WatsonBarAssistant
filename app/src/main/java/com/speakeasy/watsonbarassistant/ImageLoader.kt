package com.speakeasy.watsonbarassistant

import android.content.Context
import android.net.Uri
import android.util.Log
import com.facebook.drawee.view.SimpleDraweeView


fun loadImage(context: Context, imageView: SimpleDraweeView, recipe: DiscoveryRecipe?) {
    imageView.hierarchy.setFailureImage(BarAssistant.defaultImage)
    imageView.hierarchy.setRetryImage(BarAssistant.defaultImage)
    if(recipe != null) {
        val imageUriString = recipe.recipeImageUriString
        if(imageUriString == "") {
            val imageName = recipe.getImageName()
            Log.d("imageName", imageName)
            val imageReference = BarAssistant.storageReference?.child(imageName)
            imageReference?.downloadUrl?.addOnSuccessListener {
                recipe.recipeImageUriString = it.toString()
                setImage(imageView, recipe, context, it)
            }
        } else {
            setImage(imageView, recipe, context)
        }
    } else {
        loadFailImage(context, imageView)
    }
}

fun loadIngredientImage(context: Context, imageView: SimpleDraweeView, ingredient: Ingredient?) {
    imageView.hierarchy.setFailureImage(R.mipmap.ic_cherry)
    imageView.hierarchy.setRetryImage(R.mipmap.ic_cherry)

    if(ingredient != null) {
        val imageUriString = ingredient.imageUri
        if(imageUriString == "") {
            val imageReference = BarAssistant.storageReference?.child(ingredient.getImageName())
            imageReference?.downloadUrl?.addOnSuccessListener {
                ingredient.imageUri = it.toString()
                imageView.setImageURI(ingredient.imageUri, context)
            }
        } else {
            imageView.setImageURI(ingredient.imageUri, context)
        }
    }
}

private fun setImage(imageView: SimpleDraweeView, recipe: DiscoveryRecipe, context: Context,
                     recipeUri: Uri? = null) {
    imageView.setImageURI(recipeUri ?: Uri.parse(recipe.recipeImageUriString), context)
}


private fun loadFailImage(context: Context, imageView: SimpleDraweeView) {
    imageView.setImageURI(DEFAULT_IMAGE_URI, context)
}
