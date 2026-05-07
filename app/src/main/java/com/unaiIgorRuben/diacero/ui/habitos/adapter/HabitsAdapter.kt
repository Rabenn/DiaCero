package com.unaiIgorRuben.diacero.ui.habitos.adapter

import android.animation.ValueAnimator
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unaiIgorRuben.diacero.R
import com.unaiIgorRuben.diacero.data.local.entities.Habit
import com.unaiIgorRuben.diacero.databinding.ItemHabitBinding

class HabitsAdapter(
    private val onMarkDone: (Habit) -> Unit,
    private val onReset: (Habit) -> Unit
) : ListAdapter<Habit, HabitsAdapter.HabitViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding, onMarkDone, onReset)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HabitViewHolder(
        private val binding: ItemHabitBinding,
        private val onMarkDone: (Habit) -> Unit,
        private val onReset: (Habit) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            val context = binding.root.context
            binding.tvHabitName.text = habit.nombre
            binding.tvHabitIcon.text = habit.icono

            val currentStreak = habit.currentStreak
            binding.tvStreakValue.text = "$currentStreak/${habit.metaDias}"
            
            // Traducción de "Mejor racha"
            binding.tvBestStreak.text = context.getString(R.string.mejor_racha, habit.bestStreak)
            
            // Traducción de la frecuencia
            binding.tvFrequencyInfo.text = if (habit.frecuencia == 1) {
                context.getString(R.string.diario)
            } else {
                context.getString(R.string.cada_x_dias, habit.frecuencia)
            }
            
            binding.progressStreak.max = habit.metaDias
            binding.progressStreak.progress = currentStreak.coerceAtMost(habit.metaDias)

            applyMilestoneVisual(habit)
            setupButtons(habit)
            renderHeatmap(binding.gridHeatmap, habit.diasCompletados)
        }

        private fun setupButtons(habit: Habit) {
            val context = binding.root.context
            val completedToday = habit.isCompletedToday()
            
            if (completedToday) {
                binding.btnDoneToday.apply {
                    // Traducción de "COMPLETADO"
                    text = context.getString(R.string.completado_check)
                    isEnabled = false
                    alpha = 0.75f
                    setBackgroundColor(ContextCompat.getColor(context, R.color.secondary))
                }
            } else {
                binding.btnDoneToday.apply {
                    // Traducción de "¡Hecho!"
                    text = context.getString(R.string.hecho_btn)
                    isEnabled = true
                    alpha = 1f
                    setBackgroundColor(ContextCompat.getColor(context, R.color.primary))
                    setOnClickListener {
                        animateButtonTap(this)
                        onMarkDone(habit)
                    }
                }
            }

            binding.btnResetStreak.setOnClickListener {
                onReset(habit)
            }
        }

        private fun applyMilestoneVisual(habit: Habit) {
            val context = binding.root.context
            val streak = habit.currentStreak
            val isGoalReached = streak >= habit.metaDias

            if (isGoalReached) {
                binding.tvMilestone.visibility = View.VISIBLE
                binding.tvMilestone.text = context.getString(R.string.objetivo_cumplido)
                binding.cardHabit.strokeWidth = 4
                binding.cardHabit.strokeColor = Color.parseColor("#D4AF37") // Dorado
            } else {
                binding.tvMilestone.visibility = View.GONE
                binding.cardHabit.strokeWidth = 1
                binding.cardHabit.strokeColor = ContextCompat.getColor(context, R.color.divider)
            }
        }

        private fun renderHeatmap(grid: GridLayout, completedDays: List<Long>) {
            grid.removeAllViews()
            val context = grid.context
            val normalizedSet = completedDays.map { Habit.normalizeToDay(it) }.toSet()
            val today = Habit.normalizeToDay(System.currentTimeMillis())
            val dayMs = 24L * 60L * 60L * 1000L
            val totalCells = 28

            repeat(totalCells) { index ->
                val day = today - ((totalCells - 1 - index) * dayMs)
                val cell = TextView(context).apply {
                    val size = (12 * resources.displayMetrics.density).toInt()
                    val params = GridLayout.LayoutParams()
                    params.width = size
                    params.height = size
                    params.setMargins(4, 4, 4, 4)
                    layoutParams = params
                    
                    background = ContextCompat.getDrawable(
                        context,
                        if (normalizedSet.contains(day)) R.drawable.bg_heatmap_done else R.drawable.bg_heatmap_empty
                    )
                }
                grid.addView(cell)
            }
        }

        private fun animateButtonTap(view: View) {
            val animator = ValueAnimator.ofFloat(1f, 1.08f, 1f).apply {
                duration = 260
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    val value = it.animatedValue as Float
                    view.scaleX = value
                    view.scaleY = value
                }
            }
            animator.start()
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean = oldItem == newItem
    }
}
