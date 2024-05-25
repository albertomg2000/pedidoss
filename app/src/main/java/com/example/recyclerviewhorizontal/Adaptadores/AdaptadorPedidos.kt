

package com.example.recyclerviewhorizontal.Adaptadores

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import com.example.recyclerviewhorizontal.Clases.ProductoItem
import com.example.recyclerviewhorizontal.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso


class AdaptadorPedidos(
    private val productos: MutableList<ProductoItem>,
    private val pedidoId: String
) : RecyclerView.Adapter<AdaptadorPedidos.PedidoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val productoItem = productos[position]
        holder.bind(productoItem)
    }

    override fun getItemCount(): Int {
        return productos.size
    }

    inner class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val imageView: ImageView = itemView.findViewById(R.id.ivImagen)
        private val textViewTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        private val textViewDescripcion: TextView = itemView.findViewById(R.id.tvDesc)
        private val textViewCantidad: TextView = itemView.findViewById(R.id.tvCantidad)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(productoItem: ProductoItem) {
            textViewTitulo.text = productoItem.nombre
            textViewDescripcion.text = productoItem.descripcion
            textViewCantidad.text = "Cantidad: ${productoItem.cantidad}"


            val nombreImagen = (productoItem.nombre + productoItem.descripcion).replace("@", "")
            val storageReference = FirebaseStorage.getInstance().reference.child("images/$nombreImagen.jpg")
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri).into(imageView)
            }.addOnFailureListener {
                // Handle any errors
            }
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            val productoItem = productos[position]
            AlertDialog.Builder(itemView.context)
                .setTitle(productoItem.nombre)
                .setMessage("¿Qué quieres hacer?")
                .setPositiveButton("Decrementar") { dialog, which ->
                    eliminarProducto(position)
                }
                .setNegativeButton("Incrementar") { dialog, which ->
                    incrementarCantidad(position)
                }
                .setNeutralButton("Cancelar", null)
                .show()
        }

        private fun eliminarProducto(position: Int) {
            val productoItem = productos[position]

            // Actualizar la lista localmente
            if (productoItem.cantidad > 1) {
                productos[position] = productoItem.copy(cantidad = productoItem.cantidad - 1)
            } else {
                productos.removeAt(position)
            }
            notifyDataSetChanged()

            // Actualizar en Firebase Firestore
            val updatedProductos = productos.flatMap { item -> List(item.cantidad) { "${item.nombre}@${item.descripcion}" } }
            FirebaseFirestore.getInstance().collection("pedidos")
                .document(pedidoId)
                .update("productos", updatedProductos)
        }

        private fun incrementarCantidad(position: Int) {
            val productoItem = productos[position]

            // Actualizar la lista localmente
            productos[position] = productoItem.copy(cantidad = productoItem.cantidad + 1)
            notifyDataSetChanged()

            // Actualizar en Firebase Firestore
            val updatedProductos = productos.flatMap { item -> List(item.cantidad) { "${item.nombre}@${item.descripcion}" } }
            FirebaseFirestore.getInstance().collection("pedidos")
                .document(pedidoId)
                .update("productos", updatedProductos)
        }
    }
}

