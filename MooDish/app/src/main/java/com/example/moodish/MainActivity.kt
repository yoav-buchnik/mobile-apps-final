package com.example.moodish

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moodish.databinding.ActivityMainBinding
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.example.moodish.data.AppDatabase
import com.example.moodish.adapter.PostAdapter
import com.example.moodish.data.model.Post
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import com.example.moodish.utils.NavigationUtils


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var userEmail: String? = null
    private lateinit var database: AppDatabase
    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user email from intent
        userEmail = intent.getStringExtra("USER_EMAIL")
        
        database = AppDatabase.getDatabase(this)
        setupRecyclerView()
        setupChipListeners()
        setupBottomNavigation()
        syncPostsWithFirebase()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()
        binding.rvRestaurants.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = postAdapter
        }
    }

    private fun setupChipListeners() {
        binding.apply {
            chipRomantic.setOnClickListener {
                filterRestaurants("Romantic")
            }
            chipFamily.setOnClickListener {
                filterRestaurants("Family")
            }
            chipSolo.setOnClickListener {
                filterRestaurants("Solo")
            }
            chipHappy.setOnClickListener {
                filterRestaurants("Happy")
            }
            chipAll.setOnClickListener {
                fetchPosts()
            }
        }
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigation(
            activity = this,
            bottomNav = binding.bottomNavigation,
            userEmail = userEmail,
            currentDestination = R.id.nav_home
        )
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun syncPostsWithFirebase() {
        val db = FirebaseFirestore.getInstance()
        val postsRef = db.collection("posts")

        postsRef.get()
            .addOnSuccessListener { documents ->
                lifecycleScope.launch {
                    try {
                        // First, delete all existing posts from local DB
                        database.postDao().deleteAllPosts()

                        // Then insert all posts from Firebase
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
                        Toast.makeText(
                            this@MainActivity,
                            "Posts synchronized successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@MainActivity,
                            "Error syncing posts: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        fetchPosts()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to fetch posts from Firebase: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                // If sync fails, still show local posts
                fetchPosts()
            }
    }

    private fun fetchPosts() {
        lifecycleScope.launch {
            val posts = database.postDao().getAllPosts()
            postAdapter.updatePosts(posts)
        }
    }

    private fun filterRestaurants(category: String) {
        lifecycleScope.launch {
            val filteredPosts = database.postDao().getAllPosts().filter { post ->
                post.label == category
            }
            postAdapter.updatePosts(filteredPosts)
        }
    }

    private fun showMoreCategories() {
        // TODO: Implement more categories dialog/screen
    }
}