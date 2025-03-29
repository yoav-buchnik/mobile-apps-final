package com.example.moodish.utils


import com.example.moodish.data.AppDatabase
import com.example.moodish.data.model.Post
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


object PostUtils {
    val remoteDb = Firebase.firestore

    fun uploadPostToLocalDb(
        authorEmail: String,
        text: String,
        imageUri: String,
        label: String,
        database: AppDatabase
    ) {
        val post = Post(
            id = UUID.randomUUID().toString(),
            email = authorEmail,
            text = text,
            imageUrl = imageUri,
            label = label,
            lastUpdated = System.currentTimeMillis()
        )

        CoroutineScope(Dispatchers.IO).launch {
            database.postDao().insertPost(post)
        }
    }

    fun uploadPostToRemoteDb(authorEmail: String,
                             text: String,
                             imageUri: String,
                             label: String,
                             onSuccess: () -> Unit,
                             onFailure: (Exception) -> Unit){
        // Create a new Post
        val postId = UUID.randomUUID().toString()

        val post = hashMapOf(
            "id" to postId,
            "email" to authorEmail,
            "text" to text,
            "imageUrl" to imageUri,
            "label" to label,
            "lastUpdated" to System.currentTimeMillis()
        )

        // Add a new document with a generated ID
        remoteDb.collection("posts").document(postId) // Using set() instead of add()
            .set(post)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Post uploaded successfully")
                    onSuccess()
                } else {
                    val exception = task.exception ?: Exception("Unknown Firestore error")
                    println("Error uploading post: ${exception.message}")
                    onFailure(exception)
                }
            }
    }
}
