package com.example.recyclerviewhorizontal.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.recyclerviewhorizontal.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AñadirMarcaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anadir_marca)

        val editTextNombre: EditText = findViewById(R.id.edit_text_nombre)
        val editTextCategorias: EditText = findViewById(R.id.edit_text_categorias)
        val buttonSeleccionarImagen: Button = findViewById(R.id.button_seleccionar_imagen)
        val buttonGuardar: Button = findViewById(R.id.button_guardar)

        // Botón para seleccionar imagen
        buttonSeleccionarImagen.setOnClickListener {
            seleccionarImagen.launch("image/*")
        }

        // Botón para guardar la marca
        buttonGuardar.setOnClickListener {
            val nombre = editTextNombre.text.toString()
            val categorias = editTextCategorias.text.toString()

            if (nombre.isNotEmpty() && categorias.isNotEmpty() && selectedImageUri != null) {
                // Si se seleccionó una imagen y se ingresó un nombre y una categoría, guardar la marca
                guardarMarca(nombre, categorias)
            } else {
                showToast("Ingrese el nombre, la(s) categoría(s) y seleccione una imagen")
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

    private fun guardarMarca(nombre: String, categorias: String) {
        storageReference = storage.reference
        val fileName = "$nombre.jpg" // Nombre de archivo con extensión .jpg
        val imageRef = storageReference.child("images/$fileName")

        // Subir la imagen seleccionada a Firebase Storage
        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                // Si la carga es exitosa, obtener la URL de descarga de la imagen
                imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    // Dividir las categorías ingresadas por el usuario en una lista de categorías
                    val categoriasList = categorias.split(",").map { it.trim().lowercase() }
                    // Agregar la marca a Firestore con la URL de la imagen y las categorías
                    agregarMarca(nombre, categoriasList, imageUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                // Si hay un error, mostrar un mensaje de error
                showToast("Error al subir la imagen: ${e.message}")
            }
    }

    private fun agregarMarca(nombre: String, categorias: List<String>, imageUrl: String) {
        val nuevaMarca = hashMapOf(
            "nombre" to nombre,
            "url" to imageUrl, // Guardar la URL de la imagen en Firestore
            "categorias" to categorias // Guardar la lista de categorías en Firestore
        )

        // Agregar la marca a Firestore
        db.collection("marcas")
            .add(nuevaMarca)
            .addOnSuccessListener {
                showToast("Marca agregada correctamente")
                finish()
                MarcaAnadida()
            }
            .addOnFailureListener { e ->
                showToast("Error al agregar la marca: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun MarcaAnadida(){
        val intent = Intent(this, MyActivity::class.java)
        intent.putExtra("USER_UID", "LtSOHEAwnSdiNEWGdDLPbbWUjrs1")
        startActivity(intent)
    }
}
