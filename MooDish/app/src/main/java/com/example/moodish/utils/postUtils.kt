package com.example.moodish.utils

import android.app.Activity
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import com.example.moodish.data.AppDatabase
import com.example.moodish.data.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import com.example.moodish.utils.ImageUtils


object PostUtils {
    fun uploadPost(
        authorEmail: String,
        text: String,
        imageUrl: String,
        label: String,
        database: AppDatabase,
        context: android.content.Context,
        onComplete: (success: Boolean) -> Unit
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
        uploadPostToRemoteDb(post, context) { success ->
            onComplete(success)
        }
    }
    
    private fun uploadPostToLocalDb(post: Post, database: AppDatabase) {
        CoroutineScope(Dispatchers.IO).launch {
            database.postDao().insertPost(post)
        }
    }

    private fun uploadPostToRemoteDb(post: Post, context: android.content.Context, onComplete: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val postsref = db.collection("posts")
        
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
                onComplete(true)
            }
            .addOnFailureListener { e ->
                onComplete(false)
            }
    }

    fun deletePost(
        post: Post,
        database: AppDatabase,
        context: android.content.Context,
        onSuccess: () -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val postsRef = db.collection("posts")

        // Query for the document with matching id
        postsRef.whereEqualTo("id", post.id)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Delete the document from Firebase
                    val document = documents.documents[0]
                    document.reference.delete()
                        .addOnSuccessListener {
                            // Delete from local database
                            CoroutineScope(Dispatchers.IO).launch {
                                database.postDao().deletePost(post.id)
                                
                                // Switch to Main dispatcher for UI operations
                                withContext(Dispatchers.Main) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Post deleted successfully",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    onSuccess()
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            CoroutineScope(Dispatchers.Main).launch {
                                android.widget.Toast.makeText(
                                    context,
                                    "Failed to delete post: ${e.message}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                CoroutineScope(Dispatchers.Main).launch {
                    android.widget.Toast.makeText(
                        context,
                        "Failed to find post: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    fun editPost(
        post: Post,
        newText: String,
        newLabel: String,
        newImageUrl: String?,
        database: AppDatabase,
        context: android.content.Context,
        onSuccess: () -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val postsRef = db.collection("posts")

        val updatedPost = post.copy(
            text = newText,
            label = newLabel,
            imageUrl = newImageUrl,
            lastUpdated = System.currentTimeMillis()
        )

        // Query for the document with matching id
        postsRef.whereEqualTo("id", post.id)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    document.reference.update(
                        mapOf(
                            "text" to newText,
                            "label" to newLabel,
                            "imageUrl" to newImageUrl,
                            "lastUpdated" to System.currentTimeMillis()
                        )
                    ).addOnSuccessListener {
                        // Update local database
                        CoroutineScope(Dispatchers.IO).launch {
                            database.postDao().insertPost(updatedPost)
                            
                            withContext(Dispatchers.Main) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Post updated successfully",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                onSuccess()
                            }
                        }
                    }.addOnFailureListener { e ->
                        CoroutineScope(Dispatchers.Main).launch {
                            android.widget.Toast.makeText(
                                context,
                                "Failed to update post: ${e.message}",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                CoroutineScope(Dispatchers.Main).launch {
                    android.widget.Toast.makeText(
                        context,
                        "Failed to find post: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
