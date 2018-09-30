package com.speakeasy.watsonbarassistant

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.serialization.Optional
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable

@kotlinx.serialization.Serializable
data class DiscoveryRecipe(@Optional var queueValue: Int = 0,
                           @Optional var imageBase64: String = DEFAULT_IMAGE_BASE64,
                           @Optional val description: String = "",
                           @Optional val instructionList: List<String> = emptyList(),
                           @Optional val prepTime: String = "",
                           @Optional val cookTime: String = "",
                           @Optional val totalTime: String = "",
                           @Optional val title: String = "",
                           @Optional val ingredientList: List<String> = emptyList(),
                           @Optional var imageUriString: String = ""): Serializable {

    fun calculatePercentAvailable(userIngredients: List<Ingredient>){
        var count = 0

        for(recipeIngredient in ingredientList) {
            for(userIngredient in userIngredients) {
                if(recipeIngredient.contains(userIngredient.name, ignoreCase = true)) {
                    count++
                    break
                }
            }
        }

        queueValue = (count * 100) / (ingredientList.count())
    }

    private fun createBitMap(): Bitmap {
        val imageId = Base64.decode(imageBase64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageId, 0, imageId.count())
    }

    fun createImageUri(context: Context?): String {
        if(imageBase64 == DEFAULT_IMAGE_BASE64) {
            return DEFAULT_IMAGE_URI
        } else if (imageUriString == "") {
            val bitmap = createBitMap()

            context?.filesDir?.mkdirs()
            val file = File(context?.filesDir, title)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()

            val returnPath = "file:///${file.path}"
            imageUriString = returnPath
            return returnPath
        }
        return imageUriString
    }
}