package com.example.recyclerviewhorizontal.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.recyclerviewhorizontal.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // Delay for 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            // Start the main activity
            val intent = Intent(this, Inicio::class.java)
            startActivity(intent)
            // Finish the splash activity so the user can't go back to it
            finish()
        }, 2000) // 2000 milliseconds = 2 seconds
    }
}
