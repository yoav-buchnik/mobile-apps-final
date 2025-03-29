package com.example.moodish.utils

import android.content.Intent
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
                R.id.nav_search -> {
                    if (currentDestination != R.id.nav_search) {
                        val intent = Intent(activity, RestaurantSearchActivity::class.java)
                        intent.putExtra("USER_EMAIL", userEmail)
                        activity.startActivity(intent)
                    }
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
                R.id.nav_my_posts -> {
                    if (currentDestination != R.id.nav_my_posts) {
                        val intent = Intent(activity, MyPostsActivity::class.java)
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