package com.example.moodish

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.moodish.data.AppDatabase
import com.example.moodish.data.entity.User
import com.example.moodish.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: AppDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = AppDatabase.getDatabase(this)
        
        setupClickListeners()

        addAdminUserIfNeeded()
    }
    
    private fun addAdminUserIfNeeded() {
        lifecycleScope.launch {
            val adminUser = User(
                email = "admin",
                password = "admin"
            )
            database.userDao().insertUser(adminUser)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.editText?.text.toString()
            val password = binding.etPassword.editText?.text.toString()
            
            if (validateInputs(email, password)) {
                attemptLogin(email, password)
            }
        }
        
        binding.btnSignUp.setOnClickListener {
            showToast("Signup clicked")
        }
        
        binding.btnGoogleSignIn.setOnClickListener {
            showToast("Google Sign In clicked")
        }
        
        binding.tvForgotPassword.setOnClickListener {
            showToast("Forgot Password clicked")
        }
    }
    
    private fun attemptLogin(email: String, password: String) {
        lifecycleScope.launch {
            val user = database.userDao().login(email, password)
            if (user != null) {
                showToast("Login successful")
                navigateToMainActivity()
            } else {
                showToast("Invalid credentials")
            }
        }
    }
    
    private fun validateInputs(email: String, password: String): Boolean {
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
        
        return true
    }
    
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the login activity
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 