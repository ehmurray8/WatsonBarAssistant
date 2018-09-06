package com.speakeasy.watsonbarassistant

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class ImageViewer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)
        var photo = intent.getParcelableExtra("Image") as? Bitmap
        photo = Bitmap.createScaledBitmap(photo,690, 950, false)
        val imageView = findViewById<ImageView>(R.id.camera_image)
        imageView.setImageBitmap(photo)
        val title = findViewById<TextView>(R.id.ingredient_image_name)
        title.text = getString(R.string.test_camera_ingredient)
        val addButton = findViewById<Button>(R.id.add_camera_ingredient)
        addButton.setOnClickListener {
            handleAdd()
        }
    }

    private fun handleAdd() {
        Toast.makeText(baseContext, "Successfully added Whiskey.", Toast.LENGTH_SHORT).show()
        finish()
    }
}
