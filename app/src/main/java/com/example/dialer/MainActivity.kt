package com.example.dialer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.retrodialer.RetroDialer

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CORRECT_PIN = "2212"
    }

    private val dialer: RetroDialer by lazy {
        findViewById(R.id.dialer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dialer.setOnCodeCompleteListener {
            val toastMsg = if (it == CORRECT_PIN) "Access granted" else "Incorrect pin : $it"
            Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show()
        }
    }
}