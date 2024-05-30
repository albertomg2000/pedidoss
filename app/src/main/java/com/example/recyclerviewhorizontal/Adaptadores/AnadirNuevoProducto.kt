package com.example.recyclerviewhorizontal.Adaptadores

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.recyclerviewhorizontal.Activity.MyActivity
import com.example.recyclerviewhorizontal.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class AnadirNuevoProductoActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null
    private var isPorKilo: Boolean = false
    private lateinit var marcaSeleccionada: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anadir_nuevo_producto)
        marcaSeleccionada = intent.getStringExtra("nombreMarca")!!

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

        // Obtener la lista de marcas y mostrarlas en el Spinner

        // Botón para guardar el producto
        buttonGuardar.setOnClickListener {
            val nombre = editTextNombre.text.toString()
            val descripcion = editTextDescripcion.text.toString()
            val precio = editTextPrecio.text.toString().toDoubleOrNull()

            if (nombre.isNotEmpty() && descripcion.isNotEmpty() && precio != null && selectedImageUri != null) {
                // Si se ingresaron todos los datos y se seleccionó una imagen, guardar el producto
                guardarProducto(nombre, descripcion, precio)
            } else {
                showToast("Ingrese todos los campos y seleccione una imagen")
            }
        }
    }

    // Registro para la selección de imágenes
    private val seleccionarImagen = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Cuando se selecciona una imagen, actualiza la URI seleccionada
            selectedImageUri = uri
        }
    }

    private fun guardarProducto(nombre: String, descripcion: String, precio: Double) {
        storageReference = storage.reference
        val fileName = "$nombre$descripcion.jpg" // Nombre de archivo con extensión .jpg
        val imageRef = storageReference.child("images/$fileName")

        // Subir la imagen seleccionada a Firebase Storage
        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                // Si la carga es exitosa, obtener la URL de descarga de la imagen
                imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    // Agregar el producto a Firestore con la URL de la imagen
                    agregarProducto(nombre, descripcion, precio, imageUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                // Si hay un error, mostrar un mensaje de error
                showToast("Error al subir la imagen: ${e.message}")
            }
    }

    private fun agregarProducto(nombre: String, descripcion: String, precio: Double, imageUrl: String) {
        val nuevoProducto = hashMapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "precio" to precio,
            "url" to imageUrl,
            "marca" to marcaSeleccionada,
            "kilo" to isPorKilo,
            "timestamp" to FieldValue.serverTimestamp()
        )

        // Agregar el producto a Firestore
        db.collection("productos")
            .add(nuevoProducto)
            .addOnSuccessListener {
                showToast("Producto agregado correctamente")
                finish()
                abrirActividadProductos()
            }
            .addOnFailureListener { e ->
                showToast("Error al agregar el producto: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun abrirActividadProductos() {
        val intent = Intent(this, MyActivity::class.java)
        intent.putExtra("USER_UID", "LtSOHEAwnSdiNEWGdDLPbbWUjrs1")
        startActivity(intent)
    }
}
