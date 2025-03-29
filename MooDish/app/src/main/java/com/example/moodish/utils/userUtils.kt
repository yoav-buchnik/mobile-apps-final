package com.example.moodish.utils

import com.example.moodish.data.AppDatabase
import com.example.moodish.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


object UserUtils {
    fun uploadUser(
        user: User,
        database: AppDatabase
    ){
        uploadUserToLocalDb(user, database)
        uploadUserToRemoteDb(user)
    }
    
    private fun uploadUserToLocalDb(user: User, database: AppDatabase) {
        CoroutineScope(Dispatchers.IO).launch {
            database.userDao().insertUser(user)
        }
    }

    private fun uploadUserToRemoteDb(user: User) {
        val db = FirebaseFirestore.getInstance()
        val usersref = db.collection("users")
        
        CoroutineScope(Dispatchers.Main).launch {
            //Toast.makeText(context, "Attempting to upload user...", Toast.LENGTH_SHORT).show()
        }

        val userMap = hashMapOf(
            "id" to user.id,
            "email" to user.email,
            "password" to user.password,
            "name" to user.name,
            "profilePicUrl" to user.profilePicUrl,
            "lastLoginTimestamp" to user.lastLoginTimestamp
        )

        usersref.add(userMap)
            .addOnSuccessListener { documentReference ->
                // Use Main dispatcher for UI operations
                CoroutineScope(Dispatchers.Main).launch {
                    //Toast.makeText(
                    //    context,
                    //    "Post uploaded successfully! ID: ${documentReference.id}",
                    //   Toast.LENGTH_LONG
                    //).show()
                }
            }
            .addOnFailureListener { e ->
                // Use Main dispatcher for UI operations
                CoroutineScope(Dispatchers.Main).launch {
                    //Toast.makeText(
                    //    context,
                    //    "Failed to upload post: ${e.message}",
                    //    Toast.LENGTH_LONG
                    //).show()
                }
            }
    }

    fun fetchUserFromFirebase(
        email: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val user = User(
                        id = document.getString("id") ?: UUID.randomUUID().toString(),
                        email = document.getString("email") ?: "",
                        password = "", // Don't store the password locally
                        name = document.getString("name"),
                        profilePicUrl = document.getString("profilePicUrl"),
                        lastLoginTimestamp = document.getLong("lastLoginTimestamp") ?: System.currentTimeMillis()
                    )
                    onSuccess(user)
                } else {
                    onFailure(Exception("User not found in Firebase"))
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
