package com.example.moodish.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val password: String,
    val name: String? = null,
    val profilePicUrl: String? = null,
    val lastLoginTimestamp: Long = System.currentTimeMillis()
)