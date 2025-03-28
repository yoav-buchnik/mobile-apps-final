package com.example.moodish.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    val email: String,
    val text: String,
    val imageUrl: String? = null,
    val label: String? = null,
    val lastUpdated: Long? = System.currentTimeMillis()
)