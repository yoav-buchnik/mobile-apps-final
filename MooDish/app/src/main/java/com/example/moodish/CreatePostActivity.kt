package com.example.moodish

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.moodish.databinding.ActivityCreatePostBinding
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class CreatePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreatePostBinding
    private var imageUri: Uri? = null
    private val storage = FirebaseStorage.getInstance()
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userEmail = intent.getStringExtra("USER_EMAIL")
        setupBottomNavigation()
        setupClickListeners()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.inflateMenu(R.menu.bottom_navigation_menu)
        binding.bottomNavigation.selectedItemId = R.id.nav_create_post
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    startActivity(intent)
                    true
                }
                R.id.nav_create_post -> true
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectImage.setOnClickListener {
            selectImage()
        }

        binding.btnSavePost.setOnClickListener {
            savePost()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            binding.ivPostImage.setImageURI(it)
        }
    }

    private fun selectImage() {
        galleryLauncher.launch("image/*")
    }

    private fun savePost() {
        val postText = binding.etPostText.text.toString().trim()
        if (postText.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Please enter text and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val labels = mutableListOf<String>()
        if (binding.chkRomantic.isChecked) labels.add("Romantic")
        if (binding.chkSolo.isChecked) labels.add("Solo")
        if (binding.chkHappy.isChecked) labels.add("Happy")

        // Here you would typically upload the image to Firebase Storage
        // and save the post data to your database
        // For now, we'll just show a success message
        Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
