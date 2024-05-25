package com.example.recyclerviewhorizontal.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.recyclerviewhorizontal.R
import com.google.firebase.firestore.FirebaseFirestore

//perfil de mi usuario, solo es posible cambiar el nombre y el telefono no el email
class Perfil : AppCompatActivity() {
    private lateinit var tvEmail: TextView
    private lateinit var etNombre: EditText
    private lateinit var etTelefono: EditText
    private lateinit var btnGuardar: Button

    private val db = FirebaseFirestore.getInstance()
    private lateinit var usuarioId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_usuario)
        val toolbar = findViewById<Toolbar>(R.id.custom_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        usuarioId = intent.getStringExtra("USER_UID") ?: ""

        tvEmail = findViewById(R.id.tvEmail)
        etNombre = findViewById(R.id.etNombre)
        etTelefono = findViewById(R.id.etTelefono)
        btnGuardar = findViewById(R.id.btnGuardar)

        obtenerInformacionUsuario(usuarioId)

        btnGuardar.setOnClickListener {
            guardarCambiosUsuario()
        }
    }

    private fun obtenerInformacionUsuario(usuarioId: String) {
        db.collection("users").document(usuarioId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val email = document.getString("email") ?: ""
                    val nombre = document.getString("name") ?: ""
                    val telefono = document.getString("phone") ?: ""

                    tvEmail.text = email
                    etNombre.setText(nombre)
                    etTelefono.setText(telefono)
                } else {
                    Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al obtener datos del usuario: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarCambiosUsuario() {
        val nombre = etNombre.text.toString()
        val telefono = etTelefono.text.toString()

        if (nombre.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Nombre y teléfono no pueden estar vacíos", Toast.LENGTH_SHORT).show()
            return
        }

        val usuarioUpdates = hashMapOf(
            "name" to nombre,
            "phone" to telefono
        )

        db.collection("users").document(usuarioId)
            .update(usuarioUpdates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MyActivity::class.java)
                intent.putExtra("USER_UID", usuarioId)
                startActivity(intent)
                finish() // Finaliza la actividad actual para que no esté en el stack

    }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al actualizar datos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
