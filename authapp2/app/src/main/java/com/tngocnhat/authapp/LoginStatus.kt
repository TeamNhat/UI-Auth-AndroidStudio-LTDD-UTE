package com.tngocnhat.authapp

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class LoginStatus {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val mFunctions: FirebaseFunctions = FirebaseFunctions.getInstance()

    fun checkLoginStatus(): Boolean {
        return mAuth.currentUser != null
    }

    fun login(email: String, password: String, context: Context) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    (context as LoginActivity).navigateToHome()
                } else {
                    Toast.makeText(context, "Đăng nhập thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun signup(email: String, password: String, context: Context) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    user?.let {
                        val hashedPassword = hashPassword(password)
                        val userData = hashMapOf(
                            "email" to email,
                            "hashedPassword" to hashedPassword
                        )
                        db.collection("users").document(it.uid).set(userData)
                            .addOnSuccessListener {
                                user.sendEmailVerification()
                                (context as SignupActivity).navigateToHome()
                            }
                    }
                } else {
                    Toast.makeText(context, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun forgotPass(email: String, guessedPassword: String, context: Context) {
        db.collection("users").whereEqualTo("email", email).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isEmpty) {
                    for (document: QueryDocumentSnapshot in task.result) {
                        val storedHash = document.getString("hashedPassword")
                        if (storedHash != null && storedHash == hashPassword(guessedPassword)) {
                            otpCaller(email, context)
                            return@addOnCompleteListener
                        }
                    }
                    Toast.makeText(context, "Email hoặc mật khẩu gần nhất không đúng", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Email không tồn tại", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun otpCaller(email: String, context: Context) {
        val otp = generateOTP()
        val expiryTime = System.currentTimeMillis() + 5 * 60 * 1000
        val otpId = UUID.randomUUID().toString()

        val otpData = hashMapOf(
            "otp" to otp,
            "expiry" to expiryTime,
            "email" to email
        )

        db.collection("otp").document(otpId).set(otpData)
            .addOnSuccessListener {
                val data = hashMapOf(
                    "email" to email,
                    "otp" to otp
                )
                mFunctions.getHttpsCallable("sendOTP").call(data)
                    .addOnCompleteListener { task: Task<HttpsCallableResult> ->
                        if (task.isSuccessful) {
                            (context as ForgotPasswordActivity).navigateToOtpVerification(email)
                        } else {
                            Toast.makeText(context, "Lỗi gửi OTP", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
    }

    fun recreatePass(email: String, enteredOtp: String, newPassword: String, context: Context) {
        db.collection("otp").whereEqualTo("email", email).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isEmpty) {
                    for (document: QueryDocumentSnapshot in task.result) {
                        val storedOtp = document.getString("otp")
                        val expiry = document.getLong("expiry")
                        if (storedOtp != null && storedOtp == enteredOtp && System.currentTimeMillis() < expiry!!) {
                            val user = mAuth.currentUser
                            if (user != null) {  // Kiểm tra user không null
                                user.updatePassword(newPassword)
                                val hashedPassword = hashPassword(newPassword)
                                if (hashedPassword != null) {  // Kiểm tra hashedPassword không null
                                    db.collection("users").document(user.uid).update("hashedPassword", hashedPassword)
                                }
                                document.reference.delete()
                                mAuth.signOut()
                                (context as OtpVerificationActivity).navigateToLogin()
                                return@addOnCompleteListener
                            }
                        }
                    }
                    Toast.makeText(context, "OTP không hợp lệ hoặc hết hạn", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun hashPassword(password: String): String? {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(password.toByteArray())
            val sb = StringBuilder()
            for (b in hash) {
                sb.append(String.format("%02x", b))
            }
            sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            Log.e("LoginStatus", "Error hashing password", e)
            null
        }
    }

    private fun generateOTP(): String {
        return (100000 + Random().nextInt(900000)).toString()
    }
}