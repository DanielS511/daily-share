package com.example.dailyshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.dailyshare.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
private const val TAG = "SignUpActivity"
class SignUpActivity : AppCompatActivity() {
    private lateinit var etEmailRegister : EditText
    private lateinit var etPasswordRegister : EditText
    private lateinit var etAgeRegister : EditText
    private lateinit var etNameRegister : EditText
    private lateinit var buttSignUp : Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreDB: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        etAgeRegister = findViewById(R.id.etAgeRegister)
        etNameRegister = findViewById(R.id.etNameRegister)
        etEmailRegister = findViewById(R.id.etEmailRegister)
        etPasswordRegister = findViewById(R.id.etPasswordRegister)
        auth = Firebase.auth
        firestoreDB =  Firebase.firestore

    }

    fun signUp(view: View){
        val email = etEmailRegister.text.toString()
        val password = etPasswordRegister.text.toString()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val userUID = auth.currentUser?.uid.toString()
                    val user = User(
                        etNameRegister.text.toString(),
                        etAgeRegister.text.toString().toInt()
                    )
                    firestoreDB.collection("users").document(userUID)
                        .set(user)
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                    val intent =  Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}