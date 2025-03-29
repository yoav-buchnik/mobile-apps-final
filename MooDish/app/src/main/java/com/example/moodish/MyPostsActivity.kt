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
        setupSwipeRefresh()
        fetchUserPosts()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            isMyPostsPage = true,
            context = this,
            database = database
        )
        binding.rvMyPosts.apply {
            layoutManager = LinearLayoutManager(this@MyPostsActivity)
            adapter = postAdapter
        }
    }

    private fun setupSwipeRefresh() {
        // Set the refresh listener to trigger syncPostsWithFirebase
        binding.swipeRefresh.setOnRefreshListener {
            fetchUserPosts()
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

    fun fetchUserPosts() {
        // First, try to get posts from local database
        lifecycleScope.launch {
            try {
                fetchPostsFromFirebase()
            } catch (e: Exception) {
                showToast("Error loading posts: ${e.message}")
            }
        }
    }

    private fun fetchPostsFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        val postsRef = db.collection("posts")

        lifecycleScope.launch {
            try {
                // Get the latest update timestamp from local database
                val latestTimestamp = database.postDao().getLatestUpdateTimestamp() ?: 0L

                // Query Firebase for posts newer than our latest timestamp
                postsRef.whereGreaterThan("lastUpdated", latestTimestamp)
                    .get()
                    .addOnSuccessListener { documents ->
                        lifecycleScope.launch {
                            try {
                                // Insert only the new posts from Firebase
                                for (document in documents) {
                                    val post = Post(
                                        id = document.getString("id") ?: "",
                                        email = document.getString("email") ?: "",
                                        text = document.getString("text") ?: "",
                                        imageUrl = document.getString("imageUrl"),
                                        label = document.getString("label"),
                                        lastUpdated = document.getLong("lastUpdated")
                                    )
                                    database.postDao().insertPost(post)
                                }

                                // After syncing, fetch all posts from local DB
                                fetchPosts()

                                binding.swipeRefresh.isRefreshing = false

                                if (documents.isEmpty) {
                                    Toast.makeText(
                                        this@MyPostsActivity,
                                        "Already up to date",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MyPostsActivity,
                                        "Synced ${documents.size()} new posts",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                binding.swipeRefresh.isRefreshing = false
                                Toast.makeText(
                                    this@MyPostsActivity,
                                    "Error syncing posts: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                fetchPosts()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(
                            this@MyPostsActivity,
                            "Failed to fetch posts from Firebase: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        // If sync fails, still show local posts
                        lifecycleScope.launch {
                            fetchPosts()
                        }
                    }
            } catch (e: Exception) {
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(
                    this@MyPostsActivity,
                    "Error accessing local database: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                fetchPosts()
            }
        }
    }

    private fun fetchPosts() {
        lifecycleScope.launch {
            val localPosts = database.postDao().getAllUserPosts(userEmail!!)
            postAdapter.updatePosts(localPosts)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}