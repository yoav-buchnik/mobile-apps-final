package com.example.moodish.utils

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moodish.*
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationUtils {
    fun setupBottomNavigation(
        activity: AppCompatActivity,
        bottomNav: BottomNavigationView,
        userEmail: String?,
        currentDestination: Int
    ) {
        bottomNav.inflateMenu(R.menu.bottom_navigation_menu)
        bottomNav.selectedItemId = currentDestination
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (currentDestination != R.id.nav_home) {
                        val intent = Intent(activity, MainActivity::class.java)
                        intent.putExtra("USER_EMAIL", userEmail)
                        activity.startActivity(intent)
                    }
                    true
                }
                R.id.nav_favorites -> {
                    Toast.makeText(activity, "Favorites functionality will be implemented soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_create_post -> {
                    if (currentDestination != R.id.nav_create_post) {
                        val intent = Intent(activity, CreatePostActivity::class.java)
                        intent.putExtra("USER_EMAIL", userEmail)
                        activity.startActivity(intent)
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (currentDestination != R.id.nav_profile) {
                        val intent = Intent(activity, ProfileActivity::class.java)
                        intent.putExtra("USER_EMAIL", userEmail)
                        activity.startActivity(intent)
                    }
                    true
                }
                else -> false
            }
        }
    }
} 