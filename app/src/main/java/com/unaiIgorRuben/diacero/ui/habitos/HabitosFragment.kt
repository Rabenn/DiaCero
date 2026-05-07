package com.unaiIgorRuben.diacero.ui.habitos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.unaiIgorRuben.diacero.MainActivity
import com.unaiIgorRuben.diacero.R
import com.unaiIgorRuben.diacero.data.local.database.AppDatabase
import com.unaiIgorRuben.diacero.data.repository.HabitRepository
import com.unaiIgorRuben.diacero.databinding.FragmentHabitosBinding
import com.unaiIgorRuben.diacero.ui.habitos.adapter.HabitsAdapter

class HabitosFragment : Fragment() {

    private var _binding: FragmentHabitosBinding? = null
    private val binding get() = _binding!!

    private lateinit var habitsAdapter: HabitsAdapter
    private val viewModel: HabitosViewModel by viewModels {
        val repository = HabitRepository(AppDatabase.getDatabase(requireContext()).habitDao())
        HabitosViewModelFactory(requireActivity().application, repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupList()
        setupAddButton()
        observeHabits()
    }

    private fun setupToolbar() {
        binding.toolbarHabitos.menu.clear()
        binding.toolbarHabitos.menu.add("Inicio").apply {
            setIcon(R.drawable.ic_home)
            setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        binding.toolbarHabitos.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.toolbarHabitos.setOnMenuItemClickListener {
            (activity as? MainActivity)?.volverAlMenuPrincipal()
            true
        }
        binding.fabIrCasaHabitos.setOnClickListener {
            (activity as? MainActivity)?.volverAlMenuPrincipal()
        }
    }

    private fun setupList() {
        habitsAdapter = HabitsAdapter(
            onMarkDone = { habit -> viewModel.markHabitDoneToday(habit) },
            onReset = { habit -> showResetConfirmation(habit) }
        )
        binding.rvHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitsAdapter
        }
    }

    private fun setupAddButton() {
        binding.fabAddHabito.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        val etName = dialogView.findViewById<EditText>(R.id.etHabitName)
        val etGoal = dialogView.findViewById<EditText>(R.id.etHabitGoal)
        val etFrequency = dialogView.findViewById<EditText>(R.id.etHabitFrequency)

        AlertDialog.Builder(requireContext())
            .setTitle("Crear nuevo hábito")
            .setView(dialogView)
            .setPositiveButton("Crear") { _, _ ->
                val name = etName.text.toString().trim()
                val goal = etGoal.text.toString().toIntOrNull() ?: 30
                val frequency = etFrequency.text.toString().toIntOrNull() ?: 1
                
                if (name.isNotEmpty()) {
                    viewModel.addHabit(name, goal, frequency)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showResetConfirmation(habit: com.unaiIgorRuben.diacero.data.local.entities.Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Reiniciar racha")
            .setMessage("¿Estás seguro de que quieres reiniciar la racha de '${habit.nombre}'? Se borrará todo el progreso actual.")
            .setPositiveButton("Reiniciar") { _, _ ->
                viewModel.resetStreak(habit)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun observeHabits() {
        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            habitsAdapter.submitList(habits)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
