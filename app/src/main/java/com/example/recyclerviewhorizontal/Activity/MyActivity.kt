package com.example.recyclerviewhorizontal.Activity


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.griview.OnImageClickListener
import com.example.griview.OnItemClickListener
import com.example.griview.OnItemLongClickListener
import com.example.recyclerviewhorizontal.Adaptadores.AdaptadorMarca
import com.example.recyclerviewhorizontal.Clases.Marca
import com.example.recyclerviewhorizontal.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.widget.GridView
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.recyclerviewhorizontal.*
import com.example.recyclerviewhorizontal.Adaptadores.CarouselAdapter
import com.google.android.gms.common.util.CollectionUtils.listOf

//activity con la lista de marcas
class MyActivity : AppCompatActivity() {
    private lateinit var mAdapter: AdaptadorMarca
    private val db = FirebaseFirestore.getInstance()
    private lateinit var iduser: String
    private lateinit var categoryViewPager: RecyclerView // Cambio a RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.marcas_main)
        val toolbar = findViewById<Toolbar>(R.id.custom_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val uid = intent.getStringExtra("USER_UID")
        iduser = uid!!

        val gridView: GridView = findViewById(R.id.grid_view)
        categoryViewPager = findViewById(R.id.category_recycler_view) // Cambio a RecyclerView

        val storeButton: ImageButton = findViewById(R.id.storeButton)
        storeButton.setOnClickListener {
            val intent = Intent(this, PedidoCliente::class.java)
            intent.putExtra("USER_UID", iduser)
            startActivity(intent)
        }

        val perfil: ImageButton = findViewById(R.id.profileButton)
        perfil.setOnClickListener {
            val intent = Intent(this, Perfil::class.java)
            intent.putExtra("USER_UID", iduser)
            startActivity(intent)
        }

        mAdapter = AdaptadorMarca(
            mutableListOf(),
            object : OnItemClickListener {
                override fun OnItemClick(vista: View, position: Int) {
                    val item = mAdapter.getItem(position) as Marca
                    abrirMainActivity(item.nombre)
                }
            },
            object : OnImageClickListener {
                override fun OnImageClick(vista: View, position: Int) {
                    val item = mAdapter.getItem(position) as Marca
                    abrirMainActivity(item.nombre)
                }
            },
            object : OnItemLongClickListener {
                override fun onItemLongClick(view: View, position: Int): Boolean {
                    if (iduser == "LtSOHEAwnSdiNEWGdDLPbbWUjrs1") {
                        val selectedMarca = mAdapter.getItem(position) as Marca
                        val builder = AlertDialog.Builder(view.context)
                        builder.setTitle("Eliminar Marca")
                        builder.setMessage("¿Estás seguro que quieres eliminar ${selectedMarca.nombre}?")
                        builder.setPositiveButton("Sí") { _, _ ->
                            eliminarMarca(selectedMarca)
                        }
                        builder.setNegativeButton("Cancelar", null)
                        val dialog = builder.create()
                        dialog.show()
                    }
                    return true
                }
            }
        )

        gridView.adapter = mAdapter

        obtenerCategorias()
        obtenerDatosMarcas()
    }

    private fun obtenerCategorias() {
        val marcasRef = db.collection("marcas")
        marcasRef.get()
            .addOnSuccessListener { documents ->
                val categorias = mutableSetOf<String>()
                for (document in documents) {
                    val categoriasList = document.get("categorias") as List<String>?
                    categoriasList?.let {
                        categorias.addAll(it)
                    }
                }
                mostrarCategorias(categorias.toList())
            }

            .addOnFailureListener { exception ->
                Log.w(TAG, "Error al obtener documentos", exception)
            }
    }

    private fun mostrarCategorias(categorias: List<String>) {
        // Añadir "Todos" al principio de la lista
        val categoriasOrdenadas = mutableListOf("Todos")
        // Filtrar las categorías distintas de "Todos" y ordenarlas alfabéticamente
        val categoriasRestantes = categorias.filter { it != "Todos" }.sorted()
        categoriasOrdenadas.addAll(categoriasRestantes)

        val adapter = CarouselAdapter(this, categoriasOrdenadas) { categoria ->
            if (categoria == "Todos") {
                obtenerDatosMarcas()
            } else {
                obtenerDatosMarcasPorCategoria(categoria)
            }
        }
        categoryViewPager.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoryViewPager.adapter = adapter
    }

    private fun obtenerDatosMarcas() {
        val productosRef = db.collection("marcas")
        productosRef.orderBy("nombre", Query.Direction.ASCENDING).get()
            .addOnSuccessListener { documents ->
                val marcas = mutableListOf<Marca>()
                for (document in documents) {
                    val url = document.getString("url")
                    val nombre = document.getString("nombre")
                    val categoriasList = document.get("categorias") as? List<String>
                    // Verifica si las categorías son nulas o vacías
                    val categorias = if (categoriasList.isNullOrEmpty()) {
                        listOf("Sin categoría")
                    } else {
                        categoriasList
                    }
                    marcas.add(Marca(nombre!!, url!!, categorias))
                }
                // Ordenar las marcas por el primer elemento de su categoría
                marcas.sortWith(compareBy<Marca> { marca ->
                    when {
                        marca.nombre.equals("AHOAN", ignoreCase = true) -> "\u0000" // Coloca "AHOAN" al principio
                        marca.nombre.equals("OTROS PRODUCTOS", ignoreCase = true) -> "\uffff" // Coloca "otros productos" al final
                        else -> marca.categorias.firstOrNull()?.toLowerCase() ?: ""
                    }
                }.thenBy { it.nombre.toLowerCase() })

                if (iduser == "LtSOHEAwnSdiNEWGdDLPbbWUjrs1") {
                    marcas.add(
                        Marca(
                            "Añadir Marca",
                            "https://cdn.iconscout.com/icon/free/png-256/free-add-1467-470388.png",
                            listOf()
                        )
                    )
                }
                mAdapter.actualizarMarcas(marcas)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error al obtener documentos", exception)
            }
    }

    //se filtran por categoria cuando pulso alguna de las categorias del carousel
    private fun obtenerDatosMarcasPorCategoria(categoria: String) {
        val marcasRef = db.collection("marcas")
        marcasRef.whereArrayContains("categorias", categoria).get()
            .addOnSuccessListener { documents ->
                val marcas = mutableListOf<Marca>()
                for (document in documents) {
                    val url = document.getString("url")
                    val nombre = document.getString("nombre")
                    val categorias = document.get("categorias") as? List<String>
                    // Verifica si las categorías son nulas o vacías
                    val categoriasList = categorias ?: listOf()
                    marcas.add(Marca(nombre!!, url!!, categoriasList))
                }
                // Ordenar las marcas por el primer elemento de su categoría
                marcas.sortWith(compareBy { it.categorias.firstOrNull()?.toLowerCase() ?: "" })
                if (iduser == "LtSOHEAwnSdiNEWGdDLPbbWUjrs1") {
                    marcas.add(Marca("Añadir Marca", "https://cdn.iconscout.com/icon/free/png-256/free-add-1467-470388.png", listOf()))
                }
                // Actualizamos el adaptador con las marcas filtradas
                mAdapter.actualizarMarcas(marcas)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error al obtener documentos", exception)
            }
    }

    //solo disponible para el admin
    private fun eliminarMarca(marca: Marca) {
        val productosRef = db.collection("marcas")
        productosRef.whereEqualTo("nombre", marca.nombre)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            mostrarToast("Marca ${marca.nombre} eliminado correctamente")
                            obtenerDatosMarcass()
                        }
                        .addOnFailureListener { e ->
                            mostrarToast("Error al eliminar el producto ${marca.nombre}: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                mostrarToast("Error al buscar el producto ${marca.nombre}: ${exception.message}")
            }
    }

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    fun abrirMainActivity(nombreItem: String) {
        if (nombreItem == "Añadir Marca") {
            val intent = Intent(this, AñadirMarcaActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, ProductosActivity::class.java)
            intent.putExtra("nombreMarca", nombreItem)
            intent.putExtra("USER_UID", iduser)
            startActivity(intent)
        }
    }

    companion object {
        private const val TAG = "marcas_main"
    }
}
