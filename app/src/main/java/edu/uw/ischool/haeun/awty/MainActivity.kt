package edu.uw.ischool.haeun.awty

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var editTextMessage: EditText
    private lateinit var editTextPhoneNumber: EditText
    private lateinit var editTextInterval: EditText
    private lateinit var buttonStartStop: Button
    private var isNagging = false
    private val handler = Handler()
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextMessage = findViewById(R.id.editTextMessage)
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber)
        editTextInterval = findViewById(R.id.editTextInterval)
        buttonStartStop = findViewById(R.id.buttonStartStop)

        // Check for SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1)
        }

        buttonStartStop.setOnClickListener {
            if (!isNagging) {
                startNagging()
            } else {
                stopNagging()
            }
        }
    }

    private fun startNagging() {
        val message = editTextMessage.text.toString()
        val phoneNumber = formatPhoneNumber(editTextPhoneNumber.text.toString().trim())
        val intervalString = editTextInterval.text.toString()
        val interval = intervalString.toIntOrNull()?.let { it * 60 * 1000 }

        if (message.isBlank() || phoneNumber.isBlank() || phoneNumber == "INVALID" || interval == null || interval <= 0) {
            val alertMessage = when {
                phoneNumber == "INVALID" -> "Phone number must be 10 digits."
                interval == null || interval <= 0 -> "Interval must be a positive integer, and not be zero."
                else -> "All fields are required."
            }

            AlertDialog.Builder(this)
                .setMessage(alertMessage)
                .setPositiveButton("OK", null)
                .show()
            return
        }

        runnable = Runnable {
            sendSMS(phoneNumber, "$message\nAudio: [Audio Link]\nVideo: [Video Link]")
            handler.postDelayed(runnable, interval.toLong())
        }
        handler.post(runnable)
        isNagging = true
        buttonStartStop.text = getString(R.string.stop)
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        val sentPI: PendingIntent = PendingIntent.getBroadcast(this, 0, Intent("SMS_SENT"), PendingIntent.FLAG_UPDATE_CURRENT)

        try {
            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, null)
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Handle exceptions like invalid number format
        }
    }

    private fun stopNagging() {
        handler.removeCallbacks(runnable)
        isNagging = false
        buttonStartStop.text = getString(R.string.start)
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        val phoneDigits = phoneNumber.filter { it.isDigit() }
        return if (phoneDigits.length == 10) {
            "${phoneDigits.substring(0, 3)}-${phoneDigits.substring(3, 6)}-${phoneDigits.substring(6)}"
        } else {
            "INVALID"
        }
    }
}
