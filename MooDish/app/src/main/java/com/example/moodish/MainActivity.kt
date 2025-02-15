package com.example.moodish

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moodish.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupChipListeners()
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