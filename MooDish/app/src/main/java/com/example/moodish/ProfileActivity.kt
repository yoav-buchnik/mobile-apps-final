package com.example.moodish

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.example.moodish.utils.navigationUtils
import com.example.moodish.utils.UserUtils
import com.example.moodish.databinding.DialogEditProfileBinding
import com.example.moodish.utils.ImageUtils

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
        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
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
            // First try Firebase
            UserUtils.fetchUserFromFirebase(
                email = userEmail!!,
                onSuccess = { firebaseUser ->
                    // Update local database with remote data
                    lifecycleScope.launch {
                        database.userDao().insertUser(firebaseUser)
                        updateUIWithUserData(firebaseUser)
                    }
                },
                onFailure = { _ ->
                    // Fallback to local database
                    lifecycleScope.launch {
                        val localUser = database.userDao().getUserByEmail(userEmail!!)
                        if (localUser != null) {
                            updateUIWithUserData(localUser)
                        } else {
                            handleMissingUser()
                        }
                    }
                }
            )
        }
    }

    private fun updateUIWithUserData(user: User) {
        // Set user name
        binding.tvName.text = user.name ?: "No name"

        // Set user email
        binding.tvEmail.text = user.email

        // Format and set last login date
        val sdf = SimpleDateFormat("MMMM d, yyyy 'at' HH:mm", Locale.getDefault())
        val lastLoginDate = Date(user.lastLoginTimestamp)
        binding.tvLastLogin.text = "Last login: ${sdf.format(lastLoginDate)}"

        // Load profile image if available, otherwise use default
        if (!user.profilePicUrl.isNullOrEmpty()) {
            // Load profile image if available
            user.profilePicUrl?.let { url ->
                Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(binding.ivProfileImage)
            } ?: run {
                binding.ivProfileImage.setImageResource(R.drawable.default_profile)
            }
        }
    }

    private fun handleMissingUser() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val newUser = User(
                email = firebaseUser.email!!,
                password = "", // Don't store the password
                name = firebaseUser.displayName,
                profilePicUrl = firebaseUser.photoUrl?.toString(),
                lastLoginTimestamp = System.currentTimeMillis()
            )
            lifecycleScope.launch {
                database.userDao().insertUser(newUser)
                updateUIWithUserData(newUser)
            }
        } else {
            showToast("User not found")
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNavigation() {
        navigationUtils.setupBottomNavigation(
            activity = this,
            bottomNav = binding.bottomNavigation,
            userEmail = userEmail,
            currentDestination = R.id.nav_profile
        )
    }

    private fun showEditProfileDialog() {
        val dialogBinding = DialogEditProfileBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogBinding.root)
            .create()

        // Pre-fill current values
        dialogBinding.etName.setText(binding.tvName.text)

        // Setup image picker
        dialogBinding.btnSelectImage.setOnClickListener {
            selectImageFromGallery()
        }

        dialogBinding.btnSave.setOnClickListener {
            val newName = dialogBinding.etName.text.toString()
            if (newName.isNotBlank()) {
                updateUserProfile(newName)
                dialog.dismiss()
            } else {
                showToast("Name cannot be empty")
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                uploadImageToFirebase(uri)
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        try {
            val bitmap = ImageUtils.uriToBitmap(this, imageUri)
            val imageName = "profile_${userEmail}_${System.currentTimeMillis()}"

            ImageUtils.uploadImageToStorage(bitmap, imageName) { imageUrl ->
                if (imageUrl != null) {
                    updateUserProfilePicture(imageUrl)
                } else {
                    showToast("Failed to upload image")
                }
            }
        } catch (e: Exception) {
            showToast("Error processing image: ${e.message}")
        }
    }

    private fun updateUserProfile(newName: String) {
        lifecycleScope.launch {
            val currentUser = database.userDao().getUserByEmail(userEmail!!)
            if (currentUser != null) {
                val updatedUser = currentUser.copy(name = newName)
                database.userDao().insertUser(updatedUser)
                UserUtils.updateUserInFirebase(updatedUser)
                updateUIWithUserData(updatedUser)
                showToast("Profile updated successfully")
            }
        }
    }

    fun updateUserProfilePicture(imageUrl: String) {
        lifecycleScope.launch {
            val currentUser = database.userDao().getUserByEmail(userEmail!!)
            if (currentUser != null) {
                val updatedUser = currentUser.copy(profilePicUrl = imageUrl)
                database.userDao().insertUser(updatedUser)
                UserUtils.updateUserInFirebase(updatedUser)
                updateUIWithUserData(updatedUser)
                showToast("Profile picture updated successfully")
            }
        }
    }
}