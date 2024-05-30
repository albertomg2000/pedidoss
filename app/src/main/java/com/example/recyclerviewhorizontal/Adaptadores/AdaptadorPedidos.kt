

package com.example.recyclerviewhorizontal.Adaptadores

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
            showQuantityDialog(productoItem, position)
        }

        private fun showQuantityDialog(productoItem: ProductoItem, position: Int) {
            val dialogView = LayoutInflater.from(itemView.context).inflate(R.layout.dialogo_pedido, null)
            val productName: TextView = dialogView.findViewById(R.id.dialog_product_name)
            val quantityText: TextView = dialogView.findViewById(R.id.quantity_text)
            val buttonIncrement: Button = dialogView.findViewById(R.id.button_increment)
            val buttonDecrement: Button = dialogView.findViewById(R.id.button_decrement)
            val buttonAccept: Button = dialogView.findViewById(R.id.button_accept)

            productName.text = productoItem.nombre
            quantityText.text = productoItem.cantidad.toString()

            buttonIncrement.setOnClickListener {
                productoItem.cantidad++
                quantityText.text = productoItem.cantidad.toString()
            }

            buttonDecrement.setOnClickListener {
                if (productoItem.cantidad > 0) {
                    productoItem.cantidad--
                    quantityText.text = productoItem.cantidad.toString()
                }
            }

            val alertDialog = AlertDialog.Builder(itemView.context)
                .setView(dialogView)
                .create()

            buttonAccept.setOnClickListener {
                if (productoItem.cantidad == 0) {
                    productos.removeAt(position)
                    notifyItemRemoved(position)
                } else {
                    notifyItemChanged(position)
                }
                updateFirebase()
                alertDialog.dismiss()
            }

            alertDialog.show()
        }

        private fun updateFirebase() {
            val updatedProductos = productos.flatMap { item -> List(item.cantidad) { "${item.nombre}@${item.descripcion}" } }
            FirebaseFirestore.getInstance().collection("pedidos")
                .document(pedidoId)
                .update("productos", updatedProductos)
        }
    }
}


