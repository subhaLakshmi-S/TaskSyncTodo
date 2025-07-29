package com.todo.todolist.firebaseAuth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.todo.todolist.DarkTheme
import com.todo.todolist.MainActivity
import com.todo.todolist.R
import com.todo.todolist.databinding.LoginActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    @Inject
    lateinit var authUtil: AuthUtil
    private lateinit var binding: LoginActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        DarkTheme.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val forgotPassword= binding.forgotPassword
        val gifImageView= binding.appLogo
        Glide.with(this).asGif().load(R.drawable.app_logo).into(gifImageView)

        forgotPassword.setOnClickListener{
            showForgotPasswordDialog()
        }
        // If already logged in, go to MainActivity
        if (authUtil.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authUtil.signInWithEmail(email, password) { success, error ->
                if (success) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login failed: $error", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showForgotPasswordDialog() {
        val input= EditText(this)
        input.hint = " Enter your email"
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        input.gravity = 1
        AlertDialog.Builder(this)
            .setTitle("Reset password")
            .setMessage("Enter your Registered email to receive a password reset link.")
            .setView(input)
            .setPositiveButton("Send"){dialog, _ ->
                val email = input.text.toString().trim()
                if(email.isNotEmpty()){
                    FirebaseAuth.getInstance()
                        .sendPasswordResetEmail(email)
                        .addOnCompleteListener{ task ->
                            if(task.isSuccessful){
                                Toast.makeText(this,"Reset link sent to email", Toast.LENGTH_LONG).show()
                            }
                            else{
                                val error =task.exception?.message
                                if (error != null) {
                                    if (error.contains("There is no user")){
                                        Toast.makeText(this,"This email is not registered", Toast.LENGTH_LONG).show()

                                    }
                                    else{
                                        Toast.makeText(this,"Error: $error", Toast.LENGTH_LONG).show()

                                    }
                                }
                            }
                        }
                }
                else{
                    Toast.makeText(this,"Please enter a valid email", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("Cancel",null).show()
    }
}
