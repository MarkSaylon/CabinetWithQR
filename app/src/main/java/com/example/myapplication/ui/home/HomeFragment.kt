package com.example.myapplication.ui.home

import CabinetDao
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val cabinetLogDao by lazy { CabinetDao(requireContext()) }

    private var currentQRCodeIndex = 0
    private var cabinetQRCodes = listOf<Int>()
    private var isImageReceived = false
    private var qrCodeDialog: Dialog? = null

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
            showQRCodePopup("Cabinet1")
        }

        binding.takeButton2.setOnClickListener {
            showQRCodePopup("Cabinet2")
        }

        binding.takeButton3.setOnClickListener {
            showQRCodePopup("Cabinet3")
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

    private fun showQRCodePopup(cabinetName: String) {
        val qrCodes = when (cabinetName) {
            "Cabinet1" -> listOf(
                R.drawable.drawer_a_1,
                R.drawable.drawer_a_2,
                R.drawable.drawer_a_3,
                R.drawable.drawer_a_4
            )
            "Cabinet2" -> listOf(
                R.drawable.drawer_b_1,
                R.drawable.drawer_b_2,
                R.drawable.drawer_b_3,
                R.drawable.drawer_b_4
            )
            "Cabinet3" -> listOf(
                R.drawable.drawer_c_1,
                R.drawable.drawer_c_2,
                R.drawable.drawer_c_3,
                R.drawable.drawer_c_4
            )
            else -> emptyList()
        }

        cabinetQRCodes = qrCodes
        isImageReceived = false

        if (qrCodeDialog == null) {
            qrCodeDialog = Dialog(requireContext())
            qrCodeDialog?.setContentView(R.layout.dialog_qr_code)

            val qrCodeImageView = qrCodeDialog?.findViewById<ImageView>(R.id.qrCodeImageView)

            qrCodeDialog?.setOnDismissListener {
                currentQRCodeIndex = -1
                cabinetQRCodes = emptyList()
                qrCodeDialog = null
            }
        }

        if (currentQRCodeIndex == -1 || currentQRCodeIndex >= cabinetQRCodes.size) {
            currentQRCodeIndex = 0
        }

        val currentQRCodeDrawable = cabinetQRCodes[currentQRCodeIndex]
        qrCodeDialog?.findViewById<ImageView>(R.id.qrCodeImageView)?.setImageResource(currentQRCodeDrawable)

        qrCodeDialog?.show()

        currentQRCodeIndex++

        // Wait for the image to be received
        waitForImageReceived(qrCodeDialog!!, cabinetName)
    }

    private fun waitForImageReceived(qrCodeDialog: Dialog, cabinetName: String) {
        val waitHandler = Handler(Looper.getMainLooper())
        val waitRunnable = object : Runnable {
            override fun run() {
                if (isImageReceived) {
                    // pag nareceive yung image saka sya gagalaw
                    toggleCabinetState(cabinetName)
                    qrCodeDialog.dismiss()
                } else {
                    waitHandler.postDelayed(this, 1000) // wait lang to para di sya mawala agad.
                }
            }
        }

        waitHandler.post(waitRunnable)
    }

    private suspend fun downloadImage(imageUrl: String): ByteArray {
        return withContext(Dispatchers.IO) {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val inputStream = connection.inputStream
            val buffer = ByteArray(4096)
            val byteArrayOutputStream = ByteArrayOutputStream()

            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead)
            }

            byteArrayOutputStream.toByteArray()
        }
    }

    private suspend fun saveImageToFile(imageUrl: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val imageBytes = downloadImage(imageUrl)
                val fileName = "captured_image_${System.currentTimeMillis()}.jpg"
                val outputFile = File(requireContext().filesDir, fileName)
                outputFile.outputStream().use { fileOutputStream ->
                    fileOutputStream.write(imageBytes)
                }
                outputFile
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error saving image: ${e.message}")
                null
            }
        }
    }

    private suspend fun downloadAndSaveImage(): String? {
        val imageUrl = "link nung potragis na rpi"
        val imageFile = saveImageToFile(imageUrl)
        return imageFile?.absolutePath
    }

    private fun toggleCabinetState(cabinetName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            showQRCodePopup(cabinetName)
            val imageLink = downloadAndSaveImage()
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

            cabinetLogDao.insert(time, state, cabinetName, notif, imageLink)
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