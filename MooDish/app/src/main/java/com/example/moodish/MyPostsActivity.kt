package com.example.moodish

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moodish.adapter.PostAdapter
import com.example.moodish.data.AppDatabase
import com.example.moodish.databinding.ActivityMyPostsBinding
import com.example.moodish.utils.NavigationUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.example.moodish.data.model.Post

class MyPostsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyPostsBinding
    private lateinit var database: AppDatabase
    private lateinit var postAdapter: PostAdapter
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userEmail = intent.getStringExtra("USER_EMAIL")
        if (userEmail == null) {
            showToast("User information not available")
            finish()
            return
        }

        database = AppDatabase.getDatabase(this)
        setupRecyclerView()
        setupBottomNavigation()
        fetchUserPosts()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()
        binding.rvMyPosts.apply {
            layoutManager = LinearLayoutManager(this@MyPostsActivity)
            adapter = postAdapter
        }
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigation(
            activity = this,
            bottomNav = binding.bottomNavigation,
            userEmail = userEmail,
            currentDestination = R.id.nav_my_posts
        )
    }

    private fun fetchUserPosts() {
        // First, try to get posts from local database
        lifecycleScope.launch {
            try {
                val localPosts = database.postDao().getAllUserPosts(userEmail!!)
                postAdapter.updatePosts(localPosts)
                
                // Then, fetch from Firebase to ensure we have the latest data
                fetchPostsFromFirebase()
            } catch (e: Exception) {
                showToast("Error loading posts: ${e.message}")
            }
        }
    }

    private fun fetchPostsFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        val postsRef = db.collection("posts")
            .whereEqualTo("email", userEmail)

        postsRef.get()
            .addOnSuccessListener { documents ->
                lifecycleScope.launch {
                    try {
                        val posts = documents.map { doc ->
                            Post(
                                id = doc.getString("id") ?: "",
                                email = doc.getString("email") ?: "",
                                text = doc.getString("text") ?: "",
                                imageUrl = doc.getString("imageUrl"),
                                label = doc.getString("label"),
                                lastUpdated = doc.getLong("lastUpdated")
                            )
                        }
                        
                        // Update local database
                        database.postDao().deleteAllPosts() // Optional: you might want to only delete user's posts
                        posts.forEach { post ->
                            database.postDao().insertPost(post)
                        }
                        
                        // Update UI
                        postAdapter.updatePosts(posts)
                    } catch (e: Exception) {
                        showToast("Error syncing with Firebase: ${e.message}")
                    }
                }
            }
            .addOnFailureListener { e ->
                showToast("Error fetching posts: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}