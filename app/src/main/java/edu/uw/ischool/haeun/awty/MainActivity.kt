package edu.uw.ischool.haeun.awty

import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

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
            showCustomToast(phoneNumber, message)
            handler.postDelayed(runnable, interval.toLong())
        }
        handler.post(runnable)
        isNagging = true
        buttonStartStop.text = getString(R.string.stop)
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

    private fun showCustomToast(phoneNumber: String, message: String) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container))

        val textCaption: TextView = layout.findViewById(R.id.text_caption)
        val textMessageBody: TextView = layout.findViewById(R.id.text_message_body)

        textCaption.text = "Texting $phoneNumber"
        textMessageBody.text = message

        with (Toast(applicationContext)) {
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }
}
