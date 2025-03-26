package com.example.moodish

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.moodish.data.AppDatabase
import com.example.moodish.data.model.User
import com.example.moodish.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: AppDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        database = AppDatabase.getDatabase(this)
        
        setupClickListeners()
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
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        
        binding.tvForgotPassword.setOnClickListener {
            showToast("Forgot Password clicked")
        }
    }
    
    private fun attemptLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Update last login timestamp in local database
                    lifecycleScope.launch {
                        val user = database.userDao().getUserByEmail(email)
                        if (user != null) {
                            database.userDao().insertUser(user.copy(lastLoginTimestamp = System.currentTimeMillis()))
                        }
                    }
                    showToast("Login successful")
                    navigateToMainActivity()
                } else {
                    showToast("Authentication failed: ${task.exception?.message}")
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
        intent.putExtra("USER_EMAIL", binding.etEmail.editText?.text.toString())
        startActivity(intent)
        finish() // Close the login activity
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 