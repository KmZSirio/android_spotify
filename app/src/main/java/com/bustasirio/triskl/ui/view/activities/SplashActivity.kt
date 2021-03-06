package com.bustasirio.triskl.ui.view.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.bustasirio.triskl.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        try {
            val sharedPrefs = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

            val isLogged: Boolean = sharedPrefs.getBoolean(getString(R.string.spotify_logged), false)

            if (isLogged) {
                val intent = Intent(this, LobbyActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                this.finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                this.finish()
            }
        } catch (e: Throwable) {
            Toast.makeText( this, "Error. Try again later. $e", Toast.LENGTH_SHORT)
                .show()
        }
    }
}