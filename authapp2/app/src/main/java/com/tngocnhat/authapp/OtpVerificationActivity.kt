package com.tngocnhat.authapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class OtpVerificationActivity : AppCompatActivity() {
    private lateinit var otpInput: EditText
    private lateinit var newPasswordInput: EditText
    private lateinit var confirmButton: Button
    private val loginStatus = LoginStatus()
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        email = intent.getStringExtra("email") ?: ""
        otpInput = findViewById(R.id.otpInput)
        newPasswordInput = findViewById(R.id.newPasswordInput)
        confirmButton = findViewById(R.id.confirmButton)

        confirmButton.setOnClickListener {
            val otp = otpInput.text.toString()
            val newPassword = newPasswordInput.text.toString()
            loginStatus.recreatePass(email, otp, newPassword, this)
        }
    }

    fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}