package com.unaiIgorRuben.diacero.ui.calendario.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.databinding.ItemEventoBinding
import java.text.SimpleDateFormat
import java.util.*

class CalendarioAdapter(
    private var eventos: List<Evento>,
    private var mostrarPasadosEnNegro: Boolean = false,
    private val onItemClick: (Evento) -> Unit,
    private val onItemLongClick: (Evento) -> Unit
) : RecyclerView.Adapter<CalendarioAdapter.EventoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val binding = ItemEventoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        holder.bind(eventos[position])
    }

    override fun getItemCount() = eventos.size

    fun actualizarEventos(nuevosEventos: List<Evento>) {
        this.eventos = nuevosEventos
        notifyDataSetChanged()
    }

    fun setMostrarPasadosEnNegro(enabled: Boolean) {
        this.mostrarPasadosEnNegro = enabled
        notifyDataSetChanged()
    }

    inner class EventoViewHolder(private val binding: ItemEventoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(evento: Evento) {
            binding.tvTitulo.text = evento.titulo

            val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())
            val horaInicioStr = sdfHora.format(evento.horaInicio)
            val horaFinStr = sdfHora.format(evento.horaFin)
            binding.tvHora.text = "$horaInicioStr - $horaFinStr"

            if (evento.ubicacion?.isNotEmpty() == true) {
                binding.tvUbicacion.text = evento.ubicacion
                binding.tvUbicacion.visibility = View.VISIBLE
            } else {
                binding.tvUbicacion.visibility = View.GONE
            }

            // Lógica para color negro oscuro en eventos pasados
            val hoy = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val colorFinal = if (mostrarPasadosEnNegro && evento.fecha.before(hoy)) {
                "#000000"
            } else {
                evento.color
            }

            if (colorFinal.isNotEmpty()) {
                binding.colorIndicator.setBackgroundColor(android.graphics.Color.parseColor(colorFinal))
            } else {
                binding.colorIndicator.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }

            binding.iconPriority.visibility = if (evento.esPrioritario) View.VISIBLE else View.GONE

            // Preview de la imagen a la derecha
            if (evento.fotoRuta != null) {
                binding.ivPreview.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(Uri.parse(evento.fotoRuta))
                    .into(binding.ivPreview)
            } else {
                binding.ivPreview.visibility = View.GONE
            }

            val tieneMultimedia = evento.fotoRuta != null || evento.audioRuta != null
            if (tieneMultimedia) {
                binding.layoutMultimedia.visibility = View.VISIBLE
                binding.iconFoto.visibility = if (evento.fotoRuta != null) View.VISIBLE else View.GONE
                binding.iconAudio.visibility = if (evento.audioRuta != null) View.VISIBLE else View.GONE
            } else {
                binding.layoutMultimedia.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onItemClick(evento)
            }

            binding.root.setOnLongClickListener {
                onItemLongClick(evento)
                true
            }
        }
    }
}
