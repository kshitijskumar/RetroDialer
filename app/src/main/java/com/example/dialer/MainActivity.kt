package com.example.dialer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.retrodialer.RetroDialer

class MainActivity : AppCompatActivity() {

    private val dialer: RetroDialer by lazy {
        findViewById(R.id.dialer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dialer.setOnCodeCompleteListener {
            Toast.makeText(this, "code: $it", Toast.LENGTH_SHORT).show()
        }
    }
}