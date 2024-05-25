package com.example.recyclerviewhorizontal.Adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.griview.OnImageClickListener
import com.example.griview.OnItemClickListener
import com.example.griview.OnItemLongClickListener
import com.example.recyclerviewhorizontal.Clases.Marca
import com.example.recyclerviewhorizontal.R
import com.squareup.picasso.Picasso

class AdaptadorMarca(
    var marcas: MutableList<Marca>, // Lista de marcas
    var listener: OnItemClickListener,
    var imageListener: OnImageClickListener,
    var longListener: OnItemLongClickListener
) : BaseAdapter() {

    fun actualizarMarcas(nuevasMarcas: List<Marca>) {
        marcas.clear()
        marcas.addAll(nuevasMarcas)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return marcas.size
    }

    override fun getItem(position: Int): Any {
        return marcas[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val holder: ViewHolder

        if (convertView == null) {
            convertView = LayoutInflater.from(parent?.context).inflate(R.layout.item, parent, false)
            holder = ViewHolder(convertView)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        val item = getItem(position) as Marca
        holder.tvTitulo.text = item.nombre

        // Ajustar altura de la imagen si el título tiene más de 15 caracteres
        val scale = parent?.context?.resources?.displayMetrics?.density ?: 1f
        val newHeight = if (item.nombre.length > 15) {
            (124 * scale + 0.5f).toInt() // Convertir 98dp a pixels
        } else {
            (138 * scale + 0.5f).toInt() // Convertir 138dp a pixels o cualquier otro valor predeterminado que desees
        }
        val layoutParams = holder.ivImagen.layoutParams
        layoutParams.height = newHeight
        holder.ivImagen.layoutParams = layoutParams

        // Verificar si la marca tiene categorías
        if (item.categorias.isNotEmpty()) {
            // Obtener el primer elemento del array de categorías y establecerlo como texto
            val primerElemento = item.categorias[0].capitalize()
            val textoCategoria = if (item.categorias.size > 1) {
                "$primerElemento..."
            } else {
                primerElemento
            }
            holder.tvDescripcion.text = textoCategoria
        } else {
            // Si no hay categorías, establecer un texto predeterminado
            holder.tvDescripcion.text = "Sin categoría"
        }

        Picasso.get().load(item.imagen).fit().into(holder.ivImagen)
        holder.ivImagen.setOnClickListener { imageListener.OnImageClick(it, position) }
        holder.ivImagen.setOnLongClickListener {
            longListener.onItemLongClick(it, position)
            true // Devuelve true para indicar que el evento fue manejado
        }
        return convertView!!
    }


    class ViewHolder(
        v: View
    ) : View.OnClickListener, View.OnLongClickListener {
        var tvTitulo: TextView = v.findViewById(R.id.text_anime)
        var tvDescripcion: TextView = v.findViewById(R.id.claseMarca) // Referencia al nuevo TextView
        var ivImagen: ImageView = v.findViewById(R.id.image_thumbnail)

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
        }

        override fun onLongClick(v: View?): Boolean {
            return true
        }
    }
}
