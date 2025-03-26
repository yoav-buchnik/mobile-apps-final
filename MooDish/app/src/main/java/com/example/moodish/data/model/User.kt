package com.example.moodish.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val email: String,
    val password: String,
    val name: String? = null,
    val profilePicUrl: String? = null,
    val lastLoginTimestamp: Long = System.currentTimeMillis()
)