package com.example.dailyshare

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
//        Toast.makeText(baseContext,"log in", Toast.LENGTH_SHORT).show()
        val auth = Firebase.auth
        if (auth.currentUser != null){
            goPostActivity()
        }

        FirebaseApp.initializeApp(this)
        val loginButt = findViewById<Button>(R.id.loginButt)
        loginButt.setOnClickListener {
            loginButt.isEnabled = false
            val email = findViewById<EditText>(R.id.etEmail).text.toString()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()

            if (email.isBlank() || password.isBlank()){
                Toast.makeText(this, "Email or Password is BLANK", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { taskId ->
                loginButt.isEnabled = true
                if (taskId.isSuccessful){
                    Toast.makeText(this, "success!", Toast.LENGTH_SHORT).show()
                    goPostActivity()
                }else{
                    Log.e(TAG,"sign in with email and password fail", taskId.exception)
                    Toast.makeText(this, "Authentication fail", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun goPostActivity() {
        val intent =  Intent(this, PostActivity::class.java)
        startActivity(intent)
        finish()
    }
}