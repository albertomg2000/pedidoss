package com.example.recyclerviewhorizontal.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.recyclerviewhorizontal.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import java.net.URL

private var id = ""
class ModificarProductoActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null
    private var isPorKilo: Boolean = false
    private lateinit var marcaSeleccionada: String
    private lateinit var productoId: String
    private lateinit var originalImageUrl: String
    private lateinit var originalNombre: String
    private lateinit var originalDescripcion: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modificar_producto)

        val nombreProducto = intent.getStringExtra("nombreProducto")!!
        val descripcionProducto = intent.getStringExtra("descripcionProducto")!!
        marcaSeleccionada = intent.getStringExtra("nombreMarca")!!
        originalImageUrl = intent.getStringExtra("nombreImagen")!!
        id = intent.getStringExtra("USER_UID")!!

        // Views
        val editTextNombre: EditText = findViewById(R.id.edit_text_nombre2)
        val editTextDescripcion: EditText = findViewById(R.id.edit_text_descripcion2)
        val editTextPrecio: EditText = findViewById(R.id.edit_text_precio2)
        val checkboxPorKilo: CheckBox = findViewById(R.id.checkbox_por_kilo)
        val buttonSeleccionarImagen: Button = findViewById(R.id.button_seleccionar_imagen2)
        val buttonGuardar: Button = findViewById(R.id.button_guardar2)

        // Botón para seleccionar imagen
        buttonSeleccionarImagen.setOnClickListener {
            seleccionarImagen.launch("image/*")
        }

        // Checkbox para seleccionar si el precio es por kilo
        checkboxPorKilo.setOnCheckedChangeListener { _, isChecked ->
            isPorKilo = isChecked
        }

        // Buscar producto por nombre y descripción
        buscarProducto(nombreProducto, descripcionProducto) { id, nombre, descripcion, precio, url, kilo ->
            productoId = id
            originalImageUrl = url
            originalNombre = nombre
            originalDescripcion = descripcion

            editTextNombre.setText(nombre)
            editTextDescripcion.setText(descripcion)
            editTextPrecio.setText(precio.toString())
            checkboxPorKilo.isChecked = kilo
            isPorKilo = kilo
        }

        // Botón para guardar el producto
        buttonGuardar.setOnClickListener {
            val nombre = editTextNombre.text.toString()
            val descripcion = editTextDescripcion.text.toString()
            val precio = editTextPrecio.text.toString().toDoubleOrNull()

            if (nombre.isNotEmpty() && descripcion.isNotEmpty() && precio != null) {
                if (selectedImageUri != null) {
                    // Si se seleccionó una nueva imagen
                    guardarProductoConNuevaImagen(nombre, descripcion, precio)
                } else {
                    // Si no se seleccionó una nueva imagen, reutilizar la imagen anterior
                    reutilizarImagenOriginal(nombre, descripcion, precio)
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

    private fun buscarProducto(nombre: String, descripcion: String, callback: (String, String, String, Double, String, Boolean) -> Unit) {
        db.collection("productos")
            .whereEqualTo("nombre", nombre)
            .whereEqualTo("descripcion", descripcion)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val id = document.id
                    val nombre = document.getString("nombre")!!
                    val descripcion = document.getString("descripcion")!!
                    val precio = document.getDouble("precio")!!
                    val url = document.getString("url")!!
                    val kilo = document.getBoolean("kilo")!!
                    callback(id, nombre, descripcion, precio, url, kilo)
                } else {
                    showToast("Producto no encontrado")
                }
            }
            .addOnFailureListener { e ->
                showToast("Error al buscar el producto: ${e.message}")
            }
    }

    private fun guardarProductoConNuevaImagen(nombre: String, descripcion: String, precio: Double) {
        storageReference = storage.reference
        val fileName = "$nombre$descripcion.jpg"
        val imageRef = storageReference.child("images/$fileName")

        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    // Si se sube la imagen correctamente, primero guardamos el producto con la nueva URL
                    guardarProducto(nombre, descripcion, precio, imageUrl.toString()) {
                        // Después de guardar el producto, eliminamos la imagen original
                        eliminarImagenOriginal()
                    }
                }
            }
            .addOnFailureListener { e ->
                showToast("Error al subir la imagen: ${e.message}")
            }
    }

    private fun reutilizarImagenOriginal(nombre: String, descripcion: String, precio: Double) {
        // Descargar la imagen original y volver a subirla con el nuevo nombre
        val originalImageRef = storage.getReferenceFromUrl(originalImageUrl)
        val fileName = "$nombre$descripcion.jpg"
        val newImageRef = storage.reference.child("images/$fileName")

        Thread {
            try {
                val url = URL(originalImageUrl)
                val connection = url.openConnection()
                connection.connect()

                val input = connection.getInputStream()
                val file = File.createTempFile("tempImage", "jpg")
                val output = FileOutputStream(file)

                input.copyTo(output)
                output.close()
                input.close()

                runOnUiThread {
                    newImageRef.putFile(Uri.fromFile(file)).addOnSuccessListener {
                        newImageRef.downloadUrl.addOnSuccessListener { newImageUrl ->
                            // Guardamos el producto con la nueva URL
                            guardarProducto(nombre, descripcion, precio, newImageUrl.toString()) {
                                // Después de guardar el producto, eliminamos la imagen original
                                eliminarImagenOriginal()
                            }
                        }
                    }.addOnFailureListener { e ->
                        showToast("Error al reutilizar la imagen: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showToast("Error al descargar la imagen: ${e.message}")
                }
            }
        }.start()
    }

    private fun eliminarImagenOriginal() {
        val imageRef = storage.getReferenceFromUrl(originalImageUrl)
        imageRef.delete().addOnSuccessListener {
            showToast("Imagen original eliminada")
        }.addOnFailureListener { e ->
            showToast("Error al eliminar la imagen original: ${e.message}")
        }
    }

    private fun guardarProducto(nombre: String, descripcion: String, precio: Double, imageUrl: String, onComplete: () -> Unit) {
        val nuevaUrl = imageUrl  // Usamos la nueva URL

        // Crear un mapa con los datos del producto actualizado
        val productoActualizado = hashMapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "precio" to precio,
            "url" to nuevaUrl,  // Usamos la nueva URL
            "marca" to marcaSeleccionada,
            "kilo" to isPorKilo,
            "timestamp" to FieldValue.serverTimestamp()
        )

        // Actualizar el documento del producto en Firestore
        db.collection("productos").document(productoId)
            .update(productoActualizado as Map<String, Any>)
            .addOnSuccessListener {
                showToast("Producto modificado correctamente")
                val resultIntent = Intent(this, ProductosActivity::class.java)
                resultIntent.putExtra("nombreMarca", marcaSeleccionada)
                resultIntent.putExtra("USER_UID", id)
                startActivity(resultIntent)
                finish()
                onComplete()
            }
            .addOnFailureListener { e ->
                showToast("Error al modificar el producto: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
