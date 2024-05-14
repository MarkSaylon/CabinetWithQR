package com.example.myapplication.ui.dashboard

import CabinetDao
import LogAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val cabinetLogDao by lazy { CabinetDao(requireContext()) }

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // ACCESS NG DB, JUST IN CASE MAY PAPALITAN KAYO
        val logs = cabinetLogDao.getAllLogs()

        // DITO LANG UNG SETUP NG VIEWS FOR THE PAKING LOGS
        binding.logs.layoutManager = LinearLayoutManager(requireContext())
        binding.logs.adapter = LogAdapter(logs)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}