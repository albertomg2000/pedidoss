
package com.example.recyclerviewhorizontal.Activity

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recyclerviewhorizontal.Adaptadores.AdaptadorPedidos
import com.example.recyclerviewhorizontal.Clases.Pedido
import com.example.recyclerviewhorizontal.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Button
import com.example.recyclerviewhorizontal.Clases.ProductoItem
import com.example.recyclerviewhorizontal.EmailUtil
import com.google.android.gms.tasks.Task
//lista con el pedido del cliente
class PedidoCliente : AppCompatActivity() {
    var usuarioId = ""
    private lateinit var recyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var productos: MutableList<ProductoItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedido_cliente)
        val uid = intent.getStringExtra("USER_UID")
        usuarioId = uid!!
        recyclerView = findViewById(R.id.recyclerViewPedidos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnEnviarPedido: Button = findViewById(R.id.btnEnviarPedido)

        val currentUser = auth.currentUser
        currentUser?.let {
            val usuarioId = uid
            db.collection("pedidos")
                .whereEqualTo("usuario", usuarioId)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val pedido = document.toObject(Pedido::class.java)
                        val productoMap = pedido.productos.groupingBy { it }.eachCount()
                        productos = productoMap.map {
                            val parts = it.key.split("@")
                            val nombre = parts[0]
                            val descripcion = if (parts.size > 1) parts[1] else ""
                            ProductoItem(nombre, descripcion, it.value)
                        }.toMutableList()
                        val pedidoAdapter = AdaptadorPedidos(productos, document.id)
                        recyclerView.adapter = pedidoAdapter
                    }

                    if (productos.isEmpty()) {
                        Toast.makeText(this, "Pedido vacío", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al obtener pedidos: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnEnviarPedido.setOnClickListener {
            if (productos.isEmpty()) {
                Toast.makeText(this, "Pedido vacío", Toast.LENGTH_SHORT).show()
            } else {
                mostrarDialogoObservaciones()
            }
        }
    }

    private fun mostrarDialogoObservaciones() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Observaciones (opcional)")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Enviar") { dialog, _ ->
            val observaciones = input.text.toString()
            enviarCorreo(observaciones)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
    //envia el correo al email del admin
    private fun enviarCorreo(observaciones: String) {
        val email = "cuentaparasubirproyectosdedam@gmail.com" //poner email que quiere recibir el pedido
        val subject = "Pedido de Cliente"

        obtenerInformacionUsuario(usuarioId).addOnSuccessListener { usuarioInfo ->
            val nombre = usuarioInfo["name"] ?: ""
            val emailUsuario = usuarioInfo["email"] ?: ""
            val telefono = usuarioInfo["phone"] ?: ""

            val body = buildEmailBody(nombre, emailUsuario, telefono, observaciones)

            try {
                EmailUtil.sendEmail(email, subject, body)
                runOnUiThread {
                    Toast.makeText(this, "Pedido enviado por correo electrónico!", Toast.LENGTH_SHORT).show()
                }

                // Eliminar pedido de la base de datos y actualizar la lista
                //una vez se envia el correo con el pedido el pedido queda vacio
                eliminarPedidoYActualizarLista()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error al enviar el correo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun buildEmailBody(nombre: String, email: String, telefono: String, observaciones: String): String {
        val pedidoDetalles = productos.joinToString(separator = "\n") { "${it.nombre.replace(" ", "_")}: ${it.cantidad}" }
        val cuerpo = """
            Nombre: $nombre
            Correo Electrónico: $email
            Teléfono: $telefono
            
            Detalles del Pedido:
            $pedidoDetalles
            
            Observaciones:
            $observaciones
        """.trimIndent()

        return cuerpo
    }

    private fun obtenerInformacionUsuario(usuarioId: String): Task<Map<String, String>> {
        val usuarioInfo = mutableMapOf<String, String>()
        val documentRef = db.collection("users").document(usuarioId)
        return documentRef.get()
            .continueWith { task ->
                val document = task.result
                if (document != null && document.exists()) {
                    usuarioInfo["name"] = document.getString("name") ?: ""
                    usuarioInfo["email"] = document.getString("email") ?: ""
                    usuarioInfo["phone"] = document.getString("phone") ?: ""
                }
                usuarioInfo
            }
    }

    private fun eliminarPedidoYActualizarLista() {
        db.collection("pedidos")
            .whereEqualTo("usuario", usuarioId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    db.collection("pedidos").document(document.id)
                        .delete()
                        .addOnSuccessListener {
                            productos.clear()
                            recyclerView.adapter?.notifyDataSetChanged()
                        }
                        .addOnFailureListener { exception ->
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al obtener pedidos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
