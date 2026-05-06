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
    private val onMarkDone: (Habit) -> Unit
) : ListAdapter<Habit, HabitsAdapter.HabitViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding, onMarkDone)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HabitViewHolder(
        private val binding: ItemHabitBinding,
        private val onMarkDone: (Habit) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            binding.tvHabitName.text = habit.nombre
            binding.tvHabitIcon.text = habit.icono

            val currentStreak = habit.currentStreak
            binding.tvStreakValue.text = currentStreak.toString()
            binding.tvBestStreak.text = "Mejor racha: ${habit.bestStreak} dias"
            binding.progressStreak.max = getMilestoneTarget(currentStreak)
            binding.progressStreak.progress = currentStreak.coerceAtMost(binding.progressStreak.max)

            applyMilestoneVisual(habit)
            setupDoneButton(habit)
            renderHeatmap(binding.gridHeatmap, habit.diasCompletados)
        }

        private fun setupDoneButton(habit: Habit) {
            val completedToday = habit.isCompletedToday()
            if (completedToday) {
                binding.btnDoneToday.apply {
                    text = "✔ COMPLETADO HOY"
                    isEnabled = false
                    alpha = 0.75f
                    setBackgroundColor(ContextCompat.getColor(context, R.color.secondary))
                }
            } else {
                binding.btnDoneToday.apply {
                    text = "¡Hecho por hoy!"
                    isEnabled = true
                    alpha = 1f
                    setBackgroundColor(ContextCompat.getColor(context, R.color.primary))
                    setOnClickListener {
                        animateButtonTap(this)
                        onMarkDone(habit)
                    }
                }
            }
        }

        private fun applyMilestoneVisual(habit: Habit) {
            val streak = habit.currentStreak
            val milestone = when {
                streak >= 30 -> "Logro 30 dias"
                streak >= 15 -> "Logro 15 dias"
                streak >= 7 -> "Logro 7 dias"
                else -> null
            }

            if (milestone != null) {
                binding.tvMilestone.visibility = View.VISIBLE
                binding.tvMilestone.text = milestone
                binding.cardHabit.strokeWidth = 3
                binding.cardHabit.strokeColor = Color.parseColor("#D4AF37")
            } else {
                binding.tvMilestone.visibility = View.GONE
                binding.cardHabit.strokeWidth = 1
                binding.cardHabit.strokeColor = ContextCompat.getColor(binding.root.context, R.color.divider)
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
                    layoutParams = ViewGroup.MarginLayoutParams(size, size).apply {
                        setMargins(4, 4, 4, 4)
                    }
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

        private fun getMilestoneTarget(streak: Int): Int = when {
            streak < 7 -> 7
            streak < 30 -> 30
            else -> 60
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean = oldItem == newItem
    }
}
