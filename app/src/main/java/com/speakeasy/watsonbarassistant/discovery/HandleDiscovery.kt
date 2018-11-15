package com.speakeasy.watsonbarassistant.discovery

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.DiscoveryRecipe
import com.speakeasy.watsonbarassistant.FireStoreRecipe
import com.speakeasy.watsonbarassistant.extensions.recipeDocument
import kotlin.concurrent.thread


open class HandleDiscovery: CompletedDiscovery {

    override fun onTaskCompleted(recipes: MutableList<DiscoveryRecipe>) {
        thread {
            val tasks = mutableListOf<Task<DocumentSnapshot>>()
            synchronized(BarAssistant.recipes) {
                BarAssistant.recipes[0].clear()
            }
            for (recipe in recipes) {
                var id = recipe.imageId
                val dotIndex = id.indexOf(".")
                if(dotIndex > 0) {
                    id = id.slice(0 until dotIndex)
                }
                val fireStore = FirebaseFirestore.getInstance()
                val task = fireStore.recipeDocument(id).get()
                tasks.add(task)
                task.addOnSuccessListener {
                    it.toObject(FireStoreRecipe::class.java)?.toDiscoveryRecipe()?.let { it1 ->
                        BarAssistant.recipes[0].add(it1)
                    }
                }
            }
            Tasks.await(Tasks.whenAllComplete(tasks))
        }
    }
}