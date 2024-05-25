package com.example.recyclerviewhorizontal.Adaptadores
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.griview.OnImageClickListener
import com.example.griview.OnItemClickListener
import com.example.griview.OnItemLongClickListener
import com.example.recyclerviewhorizontal.Clases.Fruta
import com.example.recyclerviewhorizontal.R
import com.squareup.picasso.Picasso

class MyAdapter(
    var frutas: MutableList<Fruta>,
    var listener: OnItemClickListener,
    var imageListener: OnImageClickListener,
    var longListener: OnItemLongClickListener
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    fun actualizarFrutas(nuevasFrutas: List<Fruta>) {
        frutas.clear()
        frutas.addAll(nuevasFrutas)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(v, listener, longListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = frutas[position]
        holder.tvTitulo.text = item.nombre
        holder.descripcion.text = item.descripcion
        holder.descripcion.text = item.descripcion
        Picasso.get().load(item.imagen).fit().into(holder.ivImagen)
        holder.ivImagen.setOnClickListener { imageListener.OnImageClick(it, position) }
        holder.itemView.setOnLongClickListener { v ->
            longListener.onItemLongClick(v, position)
            true
        }
    }

    override fun getItemCount(): Int {
        return frutas.size
    }

    class ViewHolder(
        v: View,
        var listener: OnItemClickListener,
        var longListener: OnItemLongClickListener
    ) : RecyclerView.ViewHolder(v), View.OnClickListener, View.OnLongClickListener {
        var tvTitulo: TextView = v.findViewById(R.id.tvTitulo)
        var ivImagen: ImageView = v.findViewById(R.id.ivImagen)
        var descripcion: TextView = v.findViewById(R.id.tvDesc)
        init {
            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) {
            listener.OnItemClick(p0!!, adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            longListener.onItemLongClick(v!!, adapterPosition)
            return true
        }
    }
}
