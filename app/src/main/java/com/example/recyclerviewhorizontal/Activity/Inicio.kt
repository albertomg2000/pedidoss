package com.example.recyclerviewhorizontal.Activity

import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.recyclerviewhorizontal.R
//Primera pagina al iniciar la aplicacion
class Inicio : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio)

        auth = FirebaseAuth.getInstance()

        // Verifica si el usuario ya ha iniciado sesión
        if (auth.currentUser != null) {
            val user = auth.currentUser
            val uid = user?.uid
            // Si el usuario ya está autenticado, redirige a MyActivity
            val intent = Intent(this, MyActivity::class.java)
            intent.putExtra("USER_UID", uid)
            startActivity(intent)
            finish()
        } else {
            // Si no está autenticado, muestra las opciones de inicio de sesión y registro
            val btnLogin: Button = findViewById(R.id.btn_login)
            val btnRegister: Button = findViewById(R.id.btn_register)

            btnLogin.setOnClickListener {
                val intent = Intent(this, InicioSesion::class.java)
                startActivity(intent)
            }

            btnRegister.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
