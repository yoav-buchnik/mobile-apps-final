package com.example.moodish.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moodish.data.model.Post

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Query("SELECT * FROM posts WHERE email = :email")
    suspend fun getAllUserPosts(email: String): List<Post>

    @Query("SELECT * FROM posts")
    suspend fun getAllPosts(): List<Post>

    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String)

    @Query("SELECT MAX(lastUpdated) FROM posts")
    suspend fun getLatestUpdateTimestamp(): Long?
}