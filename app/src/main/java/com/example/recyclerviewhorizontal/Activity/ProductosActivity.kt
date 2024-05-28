package com.example.recyclerviewhorizontal.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.griview.OnImageClickListener
import com.example.griview.OnItemClickListener
import com.example.griview.OnItemLongClickListener
import com.example.recyclerviewhorizontal.Clases.Fruta
import com.example.recyclerviewhorizontal.Adaptadores.MyAdapter
import com.example.recyclerviewhorizontal.Adaptadores.AnadirNuevoProductoActivity
import com.example.recyclerviewhorizontal.R
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.util.*


private val db = FirebaseFirestore.getInstance()
private var id=""
class ProductosActivity : AppCompatActivity() {
    private lateinit var mAdapter: MyAdapter
    private lateinit var id: String
    private lateinit var Marca: String
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.productos)
        val toolbar = findViewById<Toolbar>(R.id.custom_toolbar)
        val logoImageView: ImageView = findViewById(R.id.logoImageView)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        //dependiendo del nombre de la marca que me pase el marca activity cogere una lista de productos o otra
        val nombreItem = intent.getStringExtra("nombreMarca")
        Marca = nombreItem!!
        //necesito saber la id del usuario siempre
        id = intent.getStringExtra("USER_UID")!!
        logoImageView.setOnClickListener {
            // Cuando clicke sobre el logo me llevara a la pagina principal
            val intent = Intent(this, MyActivity::class.java)
            intent.putExtra("USER_UID", id)
            startActivity(intent)
        }
        val mRecyclerView: RecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        //boton que lleva a mi pedido
        val storeButton: ImageButton = findViewById(R.id.storeButton)
        storeButton.setOnClickListener {
            val intent = Intent(this, PedidoCliente::class.java)
            intent.putExtra("USER_UID", id)
            startActivity(intent)
        }
        //boton que me lleva a mi perfil
        val perfil: ImageButton = findViewById(R.id.profileButton)
        perfil.setOnClickListener {
            val intent = Intent(this, Perfil::class.java)
            intent.putExtra("USER_UID", id)
            startActivity(intent)
        }

        mAdapter = MyAdapter(
            mutableListOf(),
            object : OnItemClickListener {
                //si le doy a añadir nuevo producto me lleva a su activity para anadir un nuevo producto
                // si le doy click a un producto que no sea el de anadir se me anade el producto al pedido
                override fun OnItemClick(vista: View, position: Int) {
                    val selectedFruta = mAdapter.frutas[position]
                    if (selectedFruta.nombre == "AÑADIR NUEVO PRODUCTO") {
                        val intent = Intent(this@ProductosActivity, AnadirNuevoProductoActivity::class.java)
                        intent.putExtra("nombreMarca", Marca)
                        startActivity(intent)
                    } else {
                        agregarProductoAPedido(selectedFruta.nombre, selectedFruta.descripcion)                    }
                }
            },
            //si le doy a añadir nuevo producto me lleva a su activity para anadir un nuevo producto
            // si le doy click a un producto que no sea el de anadir se me anade el producto al pedido
            object : OnImageClickListener {
                override fun OnImageClick(vista: View, position: Int) {
                    val selectedFruta = mAdapter.frutas[position]
                    if (selectedFruta.nombre == "AÑADIR NUEVO PRODUCTO") {
                        val intent = Intent(this@ProductosActivity, AnadirNuevoProductoActivity::class.java)
                        intent.putExtra("nombreMarca", Marca)
                        startActivity(intent)
                    } else {
                        agregarProductoAPedido(selectedFruta.nombre, selectedFruta.descripcion)
                    }
                }
            },
            object : OnItemLongClickListener {
                //si mi id es la del admin puedo borrar el producto
                override fun onItemLongClick(view: View, position: Int): Boolean {
                    if (id == "LtSOHEAwnSdiNEWGdDLPbbWUjrs1") {
                        val selectedFruta = mAdapter.frutas[position]
                        val builder = AlertDialog.Builder(view.context)
                        builder.setTitle("Eliminar Producto")
                        builder.setMessage("¿Estás seguro que quieres eliminar ${selectedFruta.nombre}?")
                        builder.setPositiveButton("Sí") { _, _ ->
                            eliminarProducto(selectedFruta)
                        }
                        builder.setNeutralButton("Modificar") { _, _ ->
                            val intent = Intent(this@ProductosActivity, ModificarProductoActivity::class.java)
                            intent.putExtra("nombreProducto", selectedFruta.nombre)
                            intent.putExtra("descripcionProducto", selectedFruta.descripcion)
                            intent.putExtra("nombreMarca", Marca)
                            intent.putExtra("nombreImagen", selectedFruta.imagen)
                            intent.putExtra("precioProducto", selectedFruta.cantidad)
                            intent.putExtra("USER_UID",id)
                            startActivity(intent)
                        }
                        builder.setNegativeButton("Cancelar", null)
                        val dialog = builder.create()
                        dialog.show()
                    }
                    return true
                }
            }
        )

        mRecyclerView.adapter = mAdapter
        obtenerDatosProductos(nombreItem!!)
    }
    //obtengo los productos en orden de su fecha de creacion
    private fun obtenerDatosProductos(nombreItem: String) {
        val productosRef = db.collection("productos")
            .whereEqualTo("marca", nombreItem)
            .orderBy("timestamp", Query.Direction.DESCENDING) // Add this line

        productosRef.get()
            .addOnSuccessListener { documents ->
                val frutas = mutableListOf<Fruta>()
                for (document in documents) {
                    val descripcion = document.getString("descripcion")
                    val nombre = document.getString("nombre")
                    val precio = document.getDouble("precio")
                    val url = document.getString("url")
                    frutas.add(Fruta(nombre!!, descripcion!!, url!!, precio!!))
                }
                //queria que la lista se me listara alreves, de forma que los creados recientemente vayan al final
                frutas.reverse()
                if (id == "LtSOHEAwnSdiNEWGdDLPbbWUjrs1") {
                    frutas.add(
                        Fruta(
                            "AÑADIR NUEVO PRODUCTO",
                            "Descripción opcional",
                            "https://cdn.iconscout.com/icon/free/png-256/free-add-1467-470388.png",
                            0.0
                        )
                    )
                }
                mAdapter.actualizarFrutas(frutas)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error al obtener documentos", exception)
            }
    }
    //me agrega el producto al pedido de mi usuario
    private fun agregarProductoAPedido(nombre: String, descripcion: String) {
        val pedidoRef = db.collection("pedidos").document(id)
        pedidoRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val productos = document.get("productos") as? MutableList<String>
                val nuevoProducto = "$nombre@$descripcion"
                productos?.add(nuevoProducto)
                pedidoRef.update("productos", productos).addOnSuccessListener {
                    mostrarToast("Producto añadido al pedido")
                }.addOnFailureListener { exception ->
                    Log.w(TAG, "Error al actualizar productos en el pedido", exception)
                }
            } else {
                val newPedido = hashMapOf(
                    "usuario" to id,
                    "productos" to mutableListOf("$nombre@$descripcion")
                )
                pedidoRef.set(newPedido).addOnSuccessListener {
                    mostrarToast("Producto añadido al pedido")
                }.addOnFailureListener { exception ->
                    Log.w(TAG, "Error al crear un nuevo pedido", exception)
                }
            }
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error al acceder al pedido del usuario", exception)
        }
    }


    //funcion disponible para el admin, tambien elimina la imagen del storage
    private fun eliminarProducto(fruta: Fruta) {
        val productosRef = db.collection("productos")
        productosRef.whereEqualTo("nombre", fruta.nombre)
            .whereEqualTo("descripcion", fruta.descripcion)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageName = "${fruta.nombre}${fruta.descripcion}.jpg"
                    val imageRef = storage.reference.child("images/$imageName")
                    imageRef.delete()
                        .addOnSuccessListener {
                            document.reference.delete()
                                .addOnSuccessListener {
                                    mostrarToast("Producto ${fruta.nombre} eliminado correctamente")
                                    obtenerDatosProductos(Marca)
                                }
                                .addOnFailureListener { e ->
                                    mostrarToast("Error al eliminar el producto ${fruta.nombre}: ${e.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            mostrarToast("Error al eliminar la imagen del producto ${fruta.nombre}: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                mostrarToast("Error al buscar el producto ${fruta.nombre}: ${exception.message}")
            }
    }

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
    //Me lleva a la pagina anterior que siempre es la pagina principal
    override fun onBackPressed() {
        val intent = Intent(this@ProductosActivity, MyActivity::class.java)
        intent.putExtra("USER_UID", id)
        startActivity(intent)
    }
    companion object {
        private const val TAG = "ProductosActivity"
    }
}







