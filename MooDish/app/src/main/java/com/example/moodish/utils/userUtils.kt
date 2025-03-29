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
            android.util.Log.d("UserUtils", "Attempting to upload user...")
        }

        usersref.add(convertUserToMap(user))
            .addOnSuccessListener { documentReference ->
                // Use Main dispatcher for UI operations
                CoroutineScope(Dispatchers.Main).launch {
                    android.util.Log.d("UserUtils", "User uploaded successfully! ID: ${documentReference.id}")
                }
            }
            .addOnFailureListener { e ->
                // Use Main dispatcher for UI operations
                CoroutineScope(Dispatchers.Main).launch {
                    android.util.Log.e("UserUtils", "Failed to upload user: ${e.message}")
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

    fun updateUserInFirebase(
        updatedUser: User,
    ){
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("users")

        usersRef.whereEqualTo("email", updatedUser.email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val documentId = documents.documents[0].id

                    usersRef.document(documentId)
                        .update(convertUserToMap(updatedUser))
                        .addOnSuccessListener {
                            android.util.Log.d("UserUtils", "User updated successfully!")
                            //onSuccess(updatedUser)
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("UserUtils", "Failed to update user: ${e.message}")
                            //onFailure(e)
                        }
                } else {
                    //onFailure(Exception("User not found in Firebase"))
                }
            }
            .addOnFailureListener { e ->
                //onFailure(e)
            }
    }

    suspend fun updateUserTimestamp(user: User, database: AppDatabase){
        val lastLoginUpdate = System.currentTimeMillis()
        val updatedUser = user
        updatedUser.lastLoginTimestamp = lastLoginUpdate
        updateUserInFirebase(updatedUser)
        database.userDao().insertUser(user.copy(lastLoginTimestamp = lastLoginUpdate))
    }

    private fun convertUserToMap(user: User): Map<String, Any?> {
        return mapOf(
            "id" to user.id,
            "email" to user.email,
            "password" to user.password,
            "name" to user.name,
            "profilePicUrl" to user.profilePicUrl,
            "lastLoginTimestamp" to user.lastLoginTimestamp
        )
    }
}
