package com.example.moodish

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.moodish.data.AppDatabase
import com.example.moodish.databinding.ActivityProfileBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.example.moodish.data.model.User

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var database: AppDatabase
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = AppDatabase.getDatabase(this)

        // Get the user email from the intent
        userEmail = intent.getStringExtra("USER_EMAIL")
        if (userEmail == null) {
            showToast("User information not available")
            finish()
            return
        }

        setupClickListeners()
        loadUserProfile()
        setupBottomNavigation()
    }

    private fun setupClickListeners() {
        binding.btnChangePhoto.setOnClickListener {
            showToast("Change photo functionality will be implemented soon")
        }

        binding.btnEditProfile.setOnClickListener {
            showToast("Edit profile functionality will be implemented soon")
        }

        binding.btnLogout.setOnClickListener {
            // Simple Firebase sign out
            FirebaseAuth.getInstance().signOut()
            
            // Return to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            val user = database.userDao().getUserByEmail(userEmail!!)
            if (user != null) {
                // Set user name
                binding.tvName.text = user.name ?: "No name provided"
                
                // Set user email
                binding.tvEmail.text = user.email
                
                // Format and set last login date
                val sdf = SimpleDateFormat("MMMM d, yyyy 'at' HH:mm", Locale.getDefault())
                val lastLoginDate = Date(user.lastLoginTimestamp)
                binding.tvLastLogin.text = "Last login: ${sdf.format(lastLoginDate)}"
                
                // Load profile image if available, otherwise use default
                if (!user.profilePicUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(user.profilePicUrl)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(binding.ivProfileImage)
                } else {
                    binding.ivProfileImage.setImageResource(R.drawable.default_profile)
                }
            } else {
                // If user not found in local DB but we have Firebase auth, create local entry
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val newUser = User(
                        email = firebaseUser.email!!,
                        password = "", // Don't store the password
                        name = firebaseUser.displayName,
                        profilePicUrl = firebaseUser.photoUrl?.toString(),
                        lastLoginTimestamp = System.currentTimeMillis()
                    )
                    database.userDao().insertUser(newUser)
                    loadUserProfile() // Reload the profile
                } else {
                    showToast("User not found")
                    finish()
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.inflateMenu(R.menu.bottom_navigation_menu)
        binding.bottomNavigation.selectedItemId = R.id.nav_profile
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    startActivity(intent)
                    true
                }
                R.id.nav_favorites -> {
                    showToast("Favorites functionality will be implemented soon")
                    true
                }
                R.id.nav_create_post -> {
                    val intent = Intent(this, CreatePostActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    true
                }
                else -> false
            }
        }
    }
} 