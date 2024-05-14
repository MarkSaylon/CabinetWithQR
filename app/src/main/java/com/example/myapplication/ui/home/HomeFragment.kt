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
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val cabinetLogDao by lazy { CabinetDao(requireContext()) }

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.takeButton.setOnClickListener {
            var openclose = 0
            val time = getCurrentTime()
            var state = "In Use"
            var cabinetName = "Cabinet1"
            var notif = "Cabinet 1 is now in use"
            if(openclose == 1){
                state = "Available"
                cabinetName = "Cabinet1"
                notif = "Cabinet 1 is now empty"
                binding.takeButton.text = "Use"
                binding.textView1.text = "In Use"
                openclose = 0
            }else{
                binding.takeButton.text = "Claim"
                binding.textView1.text = "Available"
                openclose = 1
            }

            cabinetLogDao.insert(time, state, cabinetName, notif)

            // Show the notification
            showNotification("Cabinet 1", openclose)
        }

        binding.takeButton2.setOnClickListener {
            var openclose = 0
            val time = getCurrentTime()
            var state = "In Use"
            var cabinetName = "Cabinet2"
            var notif = "Cabinet 2 is now in use"
            if(openclose == 1){
                state = "Available"
                cabinetName = "Cabinet2"
                notif = "Cabinet 2 is now empty"
                binding.takeButton2.text = "Use"
                binding.textView2.text = "In Use"
                openclose = 0
            }else{
                binding.takeButton2.text = "Claim"
                binding.textView2.text = "Available"
                openclose = 1
            }

            cabinetLogDao.insert(time, state, cabinetName, notif)

            // Show the notification
            showNotification("Cabinet 2", openclose)
        }

        binding.takeButton3.setOnClickListener {
            var openclose = 0
            val time = getCurrentTime()
            var state = "In Use"
            var cabinetName = "Cabinet3"
            var notif = "Cabinet 3 is now in use"
            if(openclose == 1){
                state = "Available"
                cabinetName = "Cabinet3"
                notif = "Cabinet 3 is now empty"
                binding.takeButton3.text = "Use"
                binding.textView3.text = "In Use"
                openclose = 0
            }else{
                binding.takeButton3.text = "Claim"
                binding.textView3.text = "Available"
                openclose = 1
            }

            cabinetLogDao.insert(time, state, cabinetName, notif)

            // Show the notification
            showNotification("Cabinet 3", openclose)
        }
        return root
    }
    private fun showNotification(cabinet: String, openclose:Int) {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var contentText: String? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationId = 1
        val channelId = "cabinet_channel_id"
        val contentTitle = "Cabinet Status"
        if (openclose == 0){
            contentText = "$cabinet is now in use"
        } else {
            contentText = "$cabinet is now available"
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