package com.example.myapplication.ui.home

import CabinetDao
import android.app.Dialog
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
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val cabinetLogDao by lazy { CabinetDao(requireContext()) }

    private var currentQRCodeIndex = 0
    private var cabinetQRCodes = listOf<Int>()
    private var isImageReceived = false
    private var qrCodeDialog: Dialog? = null

    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


            val cabinet1State = cabinetLogDao.getLatestStateForCabinet("Cabinet1")
            val cabinet2State = cabinetLogDao.getLatestStateForCabinet("Cabinet2")
            val cabinet3State = cabinetLogDao.getLatestStateForCabinet("Cabinet3")

            updateCabinetUI(binding.takeButton, binding.textView1, cabinet1State)
            updateCabinetUI(binding.takeButton2, binding.textView2, cabinet2State)
            updateCabinetUI(binding.takeButton3, binding.textView3, cabinet3State)
            Log.e("Loglang","Ito yung paulit ulit LCSL")


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
            button.text = "Claim"
            textView.text = "In Use"
        } else {
            button.text = "Use"
            textView.text = "Available"
            Log.e("Mali","Bat ako andito"+state)
        }
    }

    private fun showQRCodePopup(cabinetName: String) {
        Log.d("HomeFragment", "showQRCodePopup: $cabinetName")

        val qrCodes = when (cabinetName) {
            "Cabinet1" -> listOf(
                R.drawable.drawer_a_1,
                R.drawable.drawer_a_2,
                R.drawable.drawer_a_3,
                R.drawable.drawer_a_4
            ).shuffled()
            "Cabinet2" -> listOf(
                R.drawable.drawer_b_1,
                R.drawable.drawer_b_2,
                R.drawable.drawer_b_3,
                R.drawable.drawer_b_4
            ).shuffled()
            "Cabinet3" -> listOf(
                R.drawable.drawer_c_1,
                R.drawable.drawer_c_2,
                R.drawable.drawer_c_3,
                R.drawable.drawer_c_4
            ).shuffled()
            else -> emptyList()
        }

        cabinetQRCodes = qrCodes
        isImageReceived = false

        if (qrCodeDialog == null) {
            qrCodeDialog = Dialog(requireContext())
            qrCodeDialog?.setContentView(R.layout.dialog_qr_code)

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
        lifecycleScope.launch {
            waitForImageReceived(cabinetName)
            if (isImageReceived) {
                toggleCabinetState(cabinetName)
                qrCodeDialog?.dismiss()
            }
        }
    }

    private suspend fun waitForImageReceived(cabinetName: String) {
        while (!isImageReceived) {
            checkIfQRCodeScanned(cabinetName)
            delay(1000)
        }
    }

    private fun checkIfQRCodeScanned(cabinetName: String) {
        val request = Request.Builder()
            .url("http://192.168.1.6:5000")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HomeFragment", "Error checking QR code status: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                isImageReceived = jsonResponse.getBoolean("scanned")
                response.close()

                if (isImageReceived) {
                    lifecycleScope.launch {

                        qrCodeDialog?.dismiss()
                    }
                }
            }
        })
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
        val imageUrl = "http://192.168.1.6:5000/image"
        val imageFile = saveImageToFile(imageUrl)
        return imageFile?.absolutePath
    }

    private suspend fun toggleCabinetState(cabinetName: String) {
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
            "$cabinetName is now available"
        }

        cabinetLogDao.insert(time, state, cabinetName, notif, imageLink)
        showNotification(cabinetName, state)

        // Switch case to para dun sa UI
        when (cabinetName) {
            "Cabinet1" -> updateCabinetUI(binding.takeButton, binding.textView1, state)
            "Cabinet2" -> updateCabinetUI(binding.takeButton2, binding.textView2, state)
            "Cabinet3" -> updateCabinetUI(binding.takeButton3, binding.textView3, state)
        }
    }

    private fun updateUIAfterDatabaseUpdate() {
        lifecycleScope.launch {
            val cabinet1State = cabinetLogDao.getLatestStateForCabinet("Cabinet1")
            val cabinet2State = cabinetLogDao.getLatestStateForCabinet("Cabinet2")
            val cabinet3State = cabinetLogDao.getLatestStateForCabinet("Cabinet3")

            updateCabinetUI(binding.takeButton, binding.textView1, cabinet1State)
            updateCabinetUI(binding.takeButton2, binding.textView2, cabinet2State)
            updateCabinetUI(binding.takeButton3, binding.textView3, cabinet3State)
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

        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelId = "cabinet_channel_id"
        val channelName = "Cabinet Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance)
        notificationManager.createNotificationChannel(channel)
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
