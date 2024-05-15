package com.example.myapplication.ui.home

import CabinetDao
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val cabinetLogDao by lazy { CabinetDao(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewLifecycleOwner.lifecycleScope.launch {
            val cabinet1State = cabinetLogDao.getLatestStateForCabinet("Cabinet1")
            val cabinet2State = cabinetLogDao.getLatestStateForCabinet("Cabinet2")
            val cabinet3State = cabinetLogDao.getLatestStateForCabinet("Cabinet3")

            updateCabinetUI(binding.takeButton, binding.textView1, cabinet1State)
            updateCabinetUI(binding.takeButton2, binding.textView2, cabinet2State)
            updateCabinetUI(binding.takeButton3, binding.textView3, cabinet3State)
        }

        binding.takeButton.setOnClickListener {
            toggleCabinetState("Cabinet1")
            val cabinet1State = cabinetLogDao.getLatestStateForCabinet("Cabinet1")
            updateCabinetUI(binding.takeButton, binding.textView1,cabinet1State)
        }

        binding.takeButton2.setOnClickListener {
            toggleCabinetState("Cabinet2")
            val cabinet2State = cabinetLogDao.getLatestStateForCabinet("Cabinet2")
            updateCabinetUI(binding.takeButton2, binding.textView2,cabinet2State)
        }

        binding.takeButton3.setOnClickListener {
            toggleCabinetState("Cabinet3")
            val cabinet2State = cabinetLogDao.getLatestStateForCabinet("Cabinet3")
            updateCabinetUI(binding.takeButton3, binding.textView3,cabinet2State)
        }

        return root
    }

    private fun updateCabinetUI(button: Button, textView: TextView, state: String?) {
        if (state == "In Use") {
            button.text = "Use"
            textView.text = "Available"
        } else {
            button.text = "Claim"
            textView.text = "In Use"
        }
    }

    private fun toggleCabinetState(cabinetName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val time = getCurrentTime()
            val state = if (cabinetLogDao.getLatestStateForCabinet(cabinetName) == "In Use") {
                "Available"
            } else {
                "In Use"
            }
            val notif = if (state == "In Use") {
                "$cabinetName is now in use"
            } else {
                "$cabinetName is now empty"
            }

            cabinetLogDao.insert(time, state, cabinetName, notif)
            showNotification(cabinetName, state)
        }
    }

    private fun showNotification(cabinet: String, state: String) {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var contentText: String

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationId = 1
        val channelId = "cabinet_channel_id"
        val contentTitle = "Cabinet Status"
        contentText = if (state == "In Use") {
            "$cabinet is now in use"
        } else {
            "$cabinet is now available"
        }

        val builder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(notificationId, builder.build())
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "cabinet_channel_id"
            val channelName = "Cabinet Notifications"
            val channelDescription = "Notifications for cabinet status"
            val channelImportance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, channelImportance).apply {
                description = channelDescription
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getCurrentTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return currentDateTime.format(formatter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}