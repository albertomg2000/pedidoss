package com.example.recyclerviewhorizontal.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.recyclerviewhorizontal.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ModificarProductoActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null
    private var isPorKilo: Boolean = false
    private lateinit var marcaSeleccionada: String
    private lateinit var productoId: String
    private lateinit var originalImageUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modificar_producto)

        productoId = intent.getStringExtra("productoId")!!
        marcaSeleccionada = intent.getStringExtra("nombreMarca")!!
        originalImageUrl = intent.getStringExtra("producto_url")!!

        val editTextNombre: EditText = findViewById(R.id.edit_text_nombre2)
        val editTextDescripcion: EditText = findViewById(R.id.edit_text_descripcion2)
        val editTextPrecio: EditText = findViewById(R.id.edit_text_precio2)
        val checkboxPorKilo: CheckBox = findViewById(R.id.checkbox_por_kilo)
        val buttonSeleccionarImagen: Button = findViewById(R.id.button_seleccionar_imagen2)
        val buttonGuardar: Button = findViewById(R.id.button_guardar2)

        // Obtener datos del producto y mostrarlos en los campos
        obtenerDatosProducto(productoId) { nombre, descripcion, precio, kilo ->
            editTextNombre.setText(nombre)
            editTextDescripcion.setText(descripcion)
            editTextPrecio.setText(precio.toString())
            checkboxPorKilo.isChecked = kilo
            isPorKilo = kilo
        }

        // Botón para seleccionar imagen
        buttonSeleccionarImagen.setOnClickListener {
            seleccionarImagen.launch("image/*")
        }

        // Checkbox para seleccionar si el precio es por kilo
        checkboxPorKilo.setOnCheckedChangeListener { _, isChecked ->
            isPorKilo = isChecked
        }

        // Botón para guardar el producto
        buttonGuardar.setOnClickListener {
            val nombre = editTextNombre.text.toString()
            val descripcion = editTextDescripcion.text.toString()
            val precio = editTextPrecio.text.toString().toDoubleOrNull()

            if (nombre.isNotEmpty() && descripcion.isNotEmpty() && precio != null) {
                if (selectedImageUri != null) {
                    guardarProductoConImagen(nombre, descripcion, precio)
                } else {
                    guardarProducto(nombre, descripcion, precio, originalImageUrl)
                }
            } else {
                showToast("Ingrese todos los campos")
            }
        }
    }

    // Registro para la selección de imágenes
    private val seleccionarImagen = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = uri
        }
    }

    private fun obtenerDatosProducto(productoId: String, callback: (String, String, Double, Boolean) -> Unit) {
        db.collection("productos").document(productoId).get()
            .addOnSuccessListener { document ->
                val nombre = document.getString("nombre")!!
                val descripcion = document.getString("descripcion")!!
                val precio = document.getDouble("precio")!!
                val kilo = document.getBoolean("kilo")!!
                callback(nombre, descripcion, precio, kilo)
            }
            .addOnFailureListener { e ->
                showToast("Error al obtener los datos del producto: ${e.message}")
            }
    }

    private fun guardarProductoConImagen(nombre: String, descripcion: String, precio: Double) {
        storageReference = storage.reference
        val fileName = "$nombre$descripcion.jpg"
        val imageRef = storageReference.child("images/$fileName")

        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    guardarProducto(nombre, descripcion, precio, imageUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                showToast("Error al subir la imagen: ${e.message}")
            }
    }

    private fun guardarProducto(nombre: String, descripcion: String, precio: Double, imageUrl: String) {
        val productoActualizado = hashMapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "precio" to precio,
            "url" to imageUrl,
            "marca" to marcaSeleccionada,
            "kilo" to isPorKilo,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("productos").document(productoId)
            .update(productoActualizado as Map<String, Any>)
            .addOnSuccessListener {
                showToast("Producto modificado correctamente")
                finish()
            }
            .addOnFailureListener { e ->
                showToast("Error al modificar el producto: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
