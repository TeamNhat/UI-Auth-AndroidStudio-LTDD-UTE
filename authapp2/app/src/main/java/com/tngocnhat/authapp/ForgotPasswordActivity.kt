package com.tngocnhat.authapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var lastPasswordInput: EditText
    private lateinit var sendOtpButton: Button
    private val loginStatus = LoginStatus()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        emailInput = findViewById(R.id.emailInput)
        lastPasswordInput = findViewById(R.id.lastPasswordInput)
        sendOtpButton = findViewById(R.id.sendOtpButton)

        sendOtpButton.setOnClickListener {
            val email = emailInput.text.toString()
            val guessedPassword = lastPasswordInput.text.toString()
            loginStatus.forgotPass(email, guessedPassword, this)
        }
    }

    fun navigateToOtpVerification(email: String) {
        val intent = Intent(this, OtpVerificationActivity::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
    }
}