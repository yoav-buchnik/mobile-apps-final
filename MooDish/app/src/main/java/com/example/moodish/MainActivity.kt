package com.example.moodish

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moodish.databinding.ActivityMainBinding
import android.content.Intent
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user email from intent
        userEmail = intent.getStringExtra("USER_EMAIL")
        
        setupRecyclerView()
        setupChipListeners()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        binding.rvRestaurants.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            // TODO: Add adapter implementation
        }
    }

    private fun setupChipListeners() {
        binding.apply {
            chipRomantic.setOnClickListener {
                filterRestaurants("Romantic")
                showToast("Romantic clicked")
            }
            chipFamily.setOnClickListener {
                filterRestaurants("Family")
                showToast("Family clicked")
            }
            chipSolo.setOnClickListener {
                filterRestaurants("Solo")
                showToast("Solo clicked")
            }
            chipHappy.setOnClickListener {
                filterRestaurants("Happy")
                showToast("Happy clicked")
            }
            chipMore.setOnClickListener {
                showMoreCategories()
                showToast("More clicked")
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.inflateMenu(R.menu.bottom_navigation_menu)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    startActivity(intent)
                    true
                }
                R.id.nav_create_post -> {
                    val intent = Intent(this, CreatePostActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    startActivity(intent)
                    true
                }
                // Handle other menu items
                else -> false
            }
        }
    }

    private fun setupAddPostButton() {
        binding.fabAddPost.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun filterRestaurants(category: String) {
        // TODO: Implement filtering logic
    }

    private fun showMoreCategories() {
        // TODO: Implement more categories dialog/screen
    }
}