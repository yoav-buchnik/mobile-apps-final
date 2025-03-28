package com.example.moodish

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

        binding.btnSelectImage.setOnClickListener {
            selectImage()
        }

        binding.btnSavePost.setOnClickListener {
            savePost()
        }
    }

    // 🔹 Using Gallery App Launcher to select an image
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            binding.ivPostImage.setImageURI(uri)
        }
    }

    private fun selectImage() {
        galleryLauncher.launch("image/*")
    }

    private fun savePost() {
        val postText = binding.etPostText.text.toString().trim()
        if (postText.isEmpty() || imageUri == null) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val labels = mutableListOf<String>()
        if (binding.chkRomantic.isChecked) labels.add("Romantic")
        if (binding.chkSolo.isChecked) labels.add("Solo")
        if (binding.chkHappy.isChecked) labels.add("Happy")

        if (labels.isEmpty()) {
            Toast.makeText(this, "Select at least one label", Toast.LENGTH_SHORT).show()
            return
        }

        //uploadImageToStorage(postText, labels)
    }

    private fun uploadImageToStorage(postText: String, labels: List<String>) {
        val imageRef = storage.reference.child("posts/${UUID.randomUUID()}.jpg")
        imageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        savePostToFirestore(postText, labels, downloadUri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun savePostToFirestore(postText: String, labels: List<String>, imageUrl: String) {
        val post = hashMapOf(
            "authorEmail" to userEmail,
            "text" to postText,
            "imageUrl" to imageUrl,
            "labels" to labels,
            "timestamp" to System.currentTimeMillis()
        )

        //firestore.collection("posts").add(post)
        //    .addOnSuccessListener {
        //        Toast.makeText(this, "Post saved successfully", Toast.LENGTH_SHORT).show()
         //       finish()
         //   }
          //  .addOnFailureListener {
           //     Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show()
           // }
    }
}
