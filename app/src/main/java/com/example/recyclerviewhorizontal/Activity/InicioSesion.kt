package com.example.recyclerviewhorizontal.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recyclerviewhorizontal.R
import com.google.firebase.auth.FirebaseAuth

class InicioSesion : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginsesion)

        auth = FirebaseAuth.getInstance()

        val editTextEmail: EditText = findViewById(R.id.edit_text_email)
        val editTextPassword: EditText = findViewById(R.id.edit_text_password)
        val buttonLogin: Button = findViewById(R.id.button_login)
        val buttonRegister: Button = findViewById(R.id.button_register)
        val buttonForgotPassword: Button = findViewById(R.id.button_forgot_password)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            //comprueba si la cuenta esta registrada
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val uid = user?.uid
                            val intent = Intent(this, MyActivity::class.java)
                            intent.putExtra("USER_UID", uid)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Correo electrónico o contraseña incorrecta", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, ingrese correo y contraseña", Toast.LENGTH_SHORT).show()
            }
        }
        //redirige a registrarte
        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Manejar el clic del botón de olvido de contraseña
        buttonForgotPassword.setOnClickListener {
            val email = editTextEmail.text.toString()
            if (email.isNotEmpty()) {
                // Enviar correo de restablecimiento de contraseña
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Correo de restablecimiento de contraseña enviado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error al enviar el correo de restablecimiento", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, ingrese su correo electrónico", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
