package com.example.myapplication.ui.home

import CabinetDao
import DoorDAO
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var userProvidedIPAddress: String = "192.168.1.7:8080"
    private val binding get() = _binding!!

    private val cabinetLogDao by lazy { CabinetDao(requireContext()) }
    private val doorLogDao by lazy { DoorDAO(requireContext()) }

    private var currentQRCodeIndex = 0
    private var cabinetQRCodes = listOf<Int>()
    private var isImageReceived = false
    private var qrCodeDialog: Dialog? = null
    private var videoFeedDialog: Dialog? = null
    private var notificationJob: Job? = null
    private var player: ExoPlayer? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)  // Increase read timeout
        .writeTimeout(60, TimeUnit.SECONDS) // Increase write timeout
        .connectTimeout(60, TimeUnit.SECONDS) // Increase connection timeout
        .build()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root



            val cabinet1State = cabinetLogDao.getLatestStateForCabinet("DrawerA")
            val cabinet2State = cabinetLogDao.getLatestStateForCabinet("DrawerB")
            val cabinet3State = cabinetLogDao.getLatestStateForCabinet("DrawerC")



            updateCabinetUI(binding.takeButton, binding.textView1, cabinet1State)
            updateCabinetUI(binding.takeButton2, binding.textView2, cabinet2State)
            updateCabinetUI(binding.takeButton3, binding.textView3, cabinet3State)
            Log.e("Loglang","Ito yung paulit ulit LCSL")


        binding.takeButton.setOnClickListener {
            showQRCodePopup("DrawerA")
        }

        binding.takeButton2.setOnClickListener {
            showQRCodePopup("DrawerB")
        }

        binding.takeButton3.setOnClickListener {
            showQRCodePopup("DrawerC")
        }

        binding.showVideoFeedButton.setOnClickListener {
            //sendVideoRequest()
            //showVideoFeedDialog(userProvidedIPAddress)
            val packageManager = requireContext().packageManager
            val installedPackages = packageManager.getInstalledPackages(0)
            val packageInfo = installedPackages.find { it.packageName == "com.pibits.raspberrypiremotecam" }

            if (packageInfo != null) {
                val intent = packageManager.getLaunchIntentForPackage(packageInfo.packageName)
                if (intent != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Cannot launch the app", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "No Raspicam Remote Installed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.showVideoFeedButton2.setOnClickListener {
            //sendVideoRequest()
            //showVideoFeedDialog(userProvidedIPAddress)
            val packageManager = requireContext().packageManager
            val installedPackages = packageManager.getInstalledPackages(0)
            val packageInfo = installedPackages.find { it.packageName == "com.pibits.raspberrypiremotecam" }

            if (packageInfo != null) {
                val intent = packageManager.getLaunchIntentForPackage(packageInfo.packageName)
                if (intent != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Cannot launch the app", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "No Raspicam Remote Installed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.showVideoFeedButton3.setOnClickListener {
            //sendVideoRequest()
            //showVideoFeedDialog(userProvidedIPAddress)
            val packageManager = requireContext().packageManager
            val installedPackages = packageManager.getInstalledPackages(0)
            val packageInfo = installedPackages.find { it.packageName == "com.pibits.raspberrypiremotecam" }

            if (packageInfo != null) {
                val intent = packageManager.getLaunchIntentForPackage(packageInfo.packageName)
                if (intent != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Cannot launch the app", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "No Raspicam Remote Installed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.pushIPChange.setOnClickListener {
            val newIPAddress = binding.changeAddress.text.toString()
            if (newIPAddress.isNotEmpty()) {
                updateIPAddress(newIPAddress)
                binding.changeAddress.setText(" ")
            } else {
                updateIPAddress("192.168.1.7")
                binding.changeAddress.setText(" ")
            }
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
            "DrawerA" -> listOf(
                R.drawable.drawer_a_1,
                R.drawable.drawer_a_2,
                R.drawable.drawer_a_3,
                R.drawable.drawer_a_4
            ).shuffled()
            "DrawerB" -> listOf(
                R.drawable.drawer_b_1,
                R.drawable.drawer_b_2,
                R.drawable.drawer_b_3,
                R.drawable.drawer_b_4
            ).shuffled()
            "DrawerC" -> listOf(
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

                qrCodeDialog?.dismiss()
            }
        }
    }

    private suspend fun waitForImageReceived(cabinetName: String) {
        while (!isImageReceived) {
            checkIfQRCodeScanned(cabinetName)
            delay(1000)
        }

        if (isImageReceived) {
            showNotification(cabinetName, "In Use")
            toggleCabinetState(cabinetName)
            qrCodeDialog?.setCancelable(false) // Make the dialog non-cancelable
            checkCabinetClosure(cabinetName)
        }
    }

    private fun checkCabinetClosure(cabinetName: String) {
        lifecycleScope.launch {
            while (true) {
                val isClosed = fetchCabinetClosureStatus()
                if (!isClosed) {
                    qrCodeDialog?.dismiss()
                    notificationJob?.cancel()
                    Log.e("HABSABSB","DITO NA KO")
                    break
                } else {
                    showClosureNotification(cabinetName)
                    Log.e("wowowowow","nyek")
                }
            }
        }
    }

    private suspend fun fetchCabinetClosureStatus(): Boolean {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("http://$userProvidedIPAddress/cabinet_status")
                .build()

            try {
                val response = client.newCall(request).execute()
                val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                response.close()
                jsonResponse.getBoolean("closed")
            } catch (e: IOException) {
                Log.e("HomeFragment", "Error checking cabinet closure: ${e.message}")
                false
            }
        }
    }

    private fun showClosureNotification(cabinetName: String) {
        notificationJob = lifecycleScope.launch {
            while (isActive) {
                showNotification(cabinetName, "Not Closed")
                delay(10000) // 10 seconds
            }
        }
    }

    private fun checkIfQRCodeScanned(cabinetName: String) {
        val request = Request.Builder()
            .url("http://$userProvidedIPAddress")
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
        val imageUrl = "http://$userProvidedIPAddress/image"
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
            "DrawerA" -> updateCabinetUI(binding.takeButton, binding.textView1, state)
            "DrawerB" -> updateCabinetUI(binding.takeButton2, binding.textView2, state)
            "DrawerC" -> updateCabinetUI(binding.takeButton3, binding.textView3, state)
        }

        var doorOpen = true

        while (doorOpen) {
            val currentTime = getCurrentTime() // Function to get the current time
            val doorstate = if (doorLogDao.getLatestDoorState() == "$cabinetName is opened") {
                "$cabinetName is closed"
            } else {
                "$cabinetName is opened"
            }

            doorLogDao.insert(currentTime, doorstate)

            // Check if the door state is "closed"
            if (doorstate == "$cabinetName is closed") {
                doorOpen = false // Set doorOpen to false to exit the loop
            }

            delay(5000)
        }

    }
    //wala na to pero keep niyo nlng cute niya eh
    private fun updateUIAfterDatabaseUpdate() {
        lifecycleScope.launch {
            val cabinet1State = cabinetLogDao.getLatestStateForCabinet("DrawerA")
            val cabinet2State = cabinetLogDao.getLatestStateForCabinet("DrawerB")
            val cabinet3State = cabinetLogDao.getLatestStateForCabinet("DrawerC")

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

        if (state == "Not Closed") {
            contentText = "$cabinet is still OPEN!"

            val notification = NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            notificationManager.notify(notificationId, notification)
        }
        // Do nothing if the cabinet is closed
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
    //LIVE FEED SHIT HIGHLY EXPERIMENTAL WALA AKO ALAM DITO LEGIT
    private fun sendVideoRequest() {
        val requestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), "Video")
        val request = Request.Builder()
            .url("http://$userProvidedIPAddress/send_message")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("VideoRequest", "Error sending video request: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("VideoRequest", "Video request sent successfully")
                response.close()
            }
        })
    }

    private fun showVideoFeedDialog(userProvidedIPAddress: String) {
        if (videoFeedDialog == null) {
            videoFeedDialog = Dialog(requireContext())
            videoFeedDialog?.setContentView(R.layout.live_feed_popup)

            videoFeedDialog?.setOnDismissListener {
                videoFeedDialog = null
                player?.release()
            }

            // Set up the video feed
            val playerView = videoFeedDialog?.findViewById<PlayerView>(R.id.playerView)
            val streamUrl = "http://$userProvidedIPAddress:5000/video_feed"
            player = ExoPlayer.Builder(requireContext()).build().also {
                val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
                it.setMediaItem(mediaItem)
                it.prepare()
                it.play()
                playerView?.player = it
            }

            // Set up the dismiss button
            val dismissButton = videoFeedDialog?.findViewById<Button>(R.id.dismissButton)
            dismissButton?.setOnClickListener {
                videoFeedDialog?.dismiss()
            }
        }

        videoFeedDialog?.show()
    }


    private fun updateIPAddress(newIPAddress: String) {
        userProvidedIPAddress = newIPAddress


    }
}
