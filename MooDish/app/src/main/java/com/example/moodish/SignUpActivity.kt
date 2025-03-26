package com.example.moodish

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.moodish.data.AppDatabase
import com.example.moodish.data.model.User
import com.example.moodish.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySignupBinding
    private lateinit var database: AppDatabase
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = AppDatabase.getDatabase(this)
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.editText?.text.toString()
            val email = binding.etEmail.editText?.text.toString()
            val password = binding.etPassword.editText?.text.toString()
            val confirmPassword = binding.etConfirmPassword.editText?.text.toString()
            
            if (validateInputs(name, email, password, confirmPassword)) {
                registerUser(name, email, password)
            }
        }
        
        binding.btnLogin.setOnClickListener {
            navigateToLogin()
        }
    }
    
    private fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Update profile to include name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    
                    auth.currentUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                // Save user details locally
                                lifecycleScope.launch {
                                    val user = User(
                                        email = email,
                                        password = password, // Consider if you really need to store this
                                        name = name,
                                        profilePicUrl = null,
                                        lastLoginTimestamp = System.currentTimeMillis()
                                    )
                                    database.userDao().insertUser(user)
                                    showToast("Registration successful")
                                    navigateToLogin()
                                }
                            } else {
                                showToast("Failed to update profile: ${profileTask.exception?.message}")
                            }
                        }
                } else {
                    showToast("Registration failed: ${task.exception?.message}")
                }
            }
    }
    
    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            binding.etName.error = "Name cannot be empty"
            return false
        } else {
            binding.etName.error = null
        }
        
        if (email.isEmpty()) {
            binding.etEmail.error = "Email cannot be empty"
            return false
        } else {
            binding.etEmail.error = null
        }
        
        if (password.isEmpty()) {
            binding.etPassword.error = "Password cannot be empty"
            return false
        } else {
            binding.etPassword.error = null
        }
        
        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.error = "Confirm password cannot be empty"
            return false
        } else {
            binding.etConfirmPassword.error = null
        }
        
        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            return false
        } else {
            binding.etConfirmPassword.error = null
        }
        
        // Basic email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Please enter a valid email"
            return false
        } else {
            binding.etEmail.error = null
        }
        
        return true
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Close the signup activity
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 