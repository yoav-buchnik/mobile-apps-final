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
import com.example.moodish.utils.NavigationUtils
import android.app.ProgressDialog


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
        NavigationUtils.setupBottomNavigation(
            activity = this,
            bottomNav = binding.bottomNavigation,
            userEmail = userEmail,
            currentDestination = R.id.nav_create_post
        )
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
        val postText = binding.etPostText.text.toString()
        if (postText.isEmpty()) {
            Toast.makeText(this, "Please enter some text", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedLabel = getSelectedLabel()
        if (selectedLabel.isEmpty()) {
            Toast.makeText(this, "Please select a label", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress spinner
        val progressDialog = android.app.ProgressDialog(this)
        progressDialog.setMessage("Uploading post...")
        progressDialog.setCancelable(false)
        progressDialog.show()

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
                    ) { success ->
                        progressDialog.dismiss()
                        if (success) {
                            Toast.makeText(this, "Post created successfully", Toast.LENGTH_SHORT).show()
                            // Navigate back to main activity
                            val intent = Intent(this, MainActivity::class.java)
                            intent.putExtra("USER_EMAIL", userEmail.toString())
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to create post", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSelectedLabel(): String {
        return when {
            binding.chkRomantic.isChecked -> "Romantic"
            binding.chkSolo.isChecked -> "Solo"
            binding.chkHappy.isChecked -> "Happy"
            binding.chkFamily.isChecked -> "Family"
            else -> ""
        }
    }
}
