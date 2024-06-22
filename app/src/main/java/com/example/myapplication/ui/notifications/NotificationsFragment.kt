package com.example.myapplication.ui.notifications

import DoorDAO
import NotificationAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val doorLogDao by lazy { DoorDAO(requireContext()) }

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val logs = doorLogDao.getAllLogs()

        binding.logs.layoutManager = LinearLayoutManager(requireContext())
        binding.logs.adapter = NotificationAdapter(logs)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}