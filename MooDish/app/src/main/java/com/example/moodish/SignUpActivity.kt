package com.example.moodish

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.moodish.data.AppDatabase
import com.example.moodish.data.entity.User
import com.example.moodish.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySignupBinding
    private lateinit var database: AppDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = AppDatabase.getDatabase(this)
        
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
        lifecycleScope.launch {
            val existingUser = database.userDao().getUserByEmail(email)
            if (existingUser != null) {
                showToast("Email already registered")
            } else {
                val newUser = User(
                    email = email,
                    password = password,
                    name = name
                )
                database.userDao().insertUser(newUser)
                showToast("Registration successful")
                navigateToLogin()
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