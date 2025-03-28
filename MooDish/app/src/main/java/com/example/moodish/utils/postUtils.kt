package com.example.moodish.utils


import com.example.moodish.data.AppDatabase
import com.example.moodish.data.model.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


object PostUtils {

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
}
