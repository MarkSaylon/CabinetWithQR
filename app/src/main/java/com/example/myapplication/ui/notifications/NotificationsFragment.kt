package com.example.myapplication.ui.notifications

import CabinetDao
import NotificationAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val cabinetLogDao by lazy { CabinetDao(requireContext()) }

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val logs = cabinetLogDao.getAllLogs()

        binding.logs.layoutManager = LinearLayoutManager(requireContext())
        binding.logs.adapter = NotificationAdapter(logs)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}