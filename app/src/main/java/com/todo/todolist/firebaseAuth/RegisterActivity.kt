package com.todo.todolist.firebaseAuth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.todo.todolist.DarkTheme
import com.todo.todolist.MainActivity
import com.todo.todolist.databinding.RegisterActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    @Inject
    lateinit var authUtil: AuthUtil

    private lateinit var binding: RegisterActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        DarkTheme.applyTheme(this)

        super.onCreate(savedInstanceState)

        binding = RegisterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val confirmPassword = binding.confirmPasswordInput.text.toString().trim()
            val username= binding.username.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authUtil.registerUser(email, password,username) { success, error ->
                if (success) {
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                else {
                    when(error) {
                        is FirebaseAuthUserCollisionException -> {
                            Toast.makeText(this, "Email already registered", Toast.LENGTH_LONG)
                                .show()
                        }

                        is FirebaseAuthWeakPasswordException -> {
                            Toast.makeText(
                                this,
                                "Weak password: ${error.reason}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(this, "Invalid email format", Toast.LENGTH_LONG).show()
                        }

                        else -> {
                            Toast.makeText(this, "Error: ${error?.localizedMessage}", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
            }
        }

        binding.loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
