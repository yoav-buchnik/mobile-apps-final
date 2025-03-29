package com.example.moodish

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.moodish.databinding.ActivityCreatePostBinding
import com.example.moodish.data.AppDatabase
import com.example.moodish.utils.ImageUtils.uploadImageToStorage
import com.example.moodish.utils.ImageUtils.uriToBitmap
import com.example.moodish.utils.PostUtils.uploadPost


class CreatePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreatePostBinding
    private var imageUri: Uri? = null
    private var userEmail: String? = null
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = AppDatabase.getDatabase(this)

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

        setupLabelCheckBoxes()
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

    private fun setupLabelCheckBoxes() {
        val checkBoxes = listOf(binding.chkRomantic, binding.chkSolo, binding.chkHappy, binding.chkFamily)
        for (checkBox in checkBoxes) {
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkBoxes.filter { it != checkBox }.forEach { it.isChecked = false }
                }
            }
        }
    }

    private fun savePostInDb(postText: String, imageUrl: String, selectedLabel: String) {

    }

    private fun savePost() {
        val postText = binding.etPostText.text.toString().trim()
        if (postText.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Please enter text and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedLabel = when {
            binding.chkRomantic.isChecked -> "Romantic"
            binding.chkSolo.isChecked -> "Solo"
            binding.chkHappy.isChecked -> "Happy"
            binding.chkFamily.isChecked -> "Family"
            else -> null
        }

        if (selectedLabel == null) {
            Toast.makeText(this, "Please select one label", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val bitmap = uriToBitmap(this, imageUri!!)
            val imageName = "post_${System.currentTimeMillis()}"

            uploadImageToStorage(bitmap, imageName) { imageUrl ->
                if (imageUrl != null) {
                    uploadPost(
                        authorEmail = userEmail!!,
                        text = postText,
                        imageUrl = imageUrl.toString(),
                        label = selectedLabel,
                        database = database,
                        context = this
                    )
                } else {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER_EMAIL", userEmail.toString())
        startActivity(intent)
        finish() // Close the login activity
    }


}
