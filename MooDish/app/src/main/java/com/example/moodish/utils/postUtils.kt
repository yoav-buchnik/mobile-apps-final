package com.example.moodish.utils

import android.util.Log
import com.example.moodish.data.AppDatabase
import com.example.moodish.data.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


object PostUtils {
    fun uploadPost(
        authorEmail: String,
        text: String,
        imageUrl: String,
        label: String,
        database: AppDatabase,
        context: android.content.Context
    ){
        val post = Post(
            id = UUID.randomUUID().toString(),
            email = authorEmail,
            text = text,
            imageUrl = imageUrl,
            label = label,
            lastUpdated = System.currentTimeMillis()
        )
        uploadPostToLocalDb(post, database)
        uploadPostToRemoteDb(post, context)
    }
    
    private fun uploadPostToLocalDb(post: Post, database: AppDatabase) {
        CoroutineScope(Dispatchers.IO).launch {
            database.postDao().insertPost(post)
        }
    }

    private fun uploadPostToRemoteDb(post: Post, context: android.content.Context) {
        val db = FirebaseFirestore.getInstance()
        val postsref = db.collection("posts")
        
        CoroutineScope(Dispatchers.Main).launch {
            android.widget.Toast.makeText(context, "Attempting to upload post...", android.widget.Toast.LENGTH_SHORT).show()
        }

        val postMap = hashMapOf(
            "id" to post.id,
            "email" to post.email,
            "text" to post.text,
            "imageUrl" to post.imageUrl,
            "label" to post.label,
            "lastUpdated" to post.lastUpdated
        )

        postsref.add(postMap)
            .addOnSuccessListener { documentReference ->
                // Use Main dispatcher for UI operations
                CoroutineScope(Dispatchers.Main).launch {
                    android.widget.Toast.makeText(
                        context,
                        "Post uploaded successfully! ID: ${documentReference.id}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                // Use Main dispatcher for UI operations
                CoroutineScope(Dispatchers.Main).launch {
                    android.widget.Toast.makeText(
                        context,
                        "Failed to upload post: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
