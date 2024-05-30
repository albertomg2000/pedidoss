package com.example.recyclerviewhorizontal.Activity


import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recyclerviewhorizontal.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val editTextName: EditText = findViewById(R.id.edit_text_name)
        val editTextEmail: EditText = findViewById(R.id.edit_text_email)
        val editTextPassword: EditText = findViewById(R.id.edit_text_password)
        val editTextPhone: EditText = findViewById(R.id.edit_text_phone)
        val buttonRegister: Button = findViewById(R.id.button_register)

        buttonRegister.setOnClickListener {
            val name = editTextName.text.toString()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val phone = editTextPhone.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && phone.isNotEmpty()) {
                registerUser(name, email, password, phone)
            } else {
                showToast("Por favor, complete todos los campos")
            }
        }
    }

    private fun registerUser(name: String, email: String, password: String, phone: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "phone" to phone
                    )

                    db.collection("users").document(auth.currentUser?.uid ?: "")
                        .set(user)
                        .addOnSuccessListener {
                            showToast("Registro exitoso")
                            finish()
                        }
                        .addOnFailureListener { e ->
                            showToast("Error al registrar los datos: ${e.message}")
                        }
                } else {
                    showToast("Error al registrar: ${task.exception?.message}")
                }
            }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
