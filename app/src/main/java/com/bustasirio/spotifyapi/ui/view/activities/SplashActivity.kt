package com.bustasirio.spotifyapi.ui.view.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bustasirio.spotifyapi.R
import android.preference.PreferenceManager

import android.content.SharedPreferences




class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val isLogged: Boolean = sharedPrefs.getBoolean(getString(R.string.spotify_logged), false)

        Log.d("tagSplashActivity", isLogged.toString())
        if (isLogged) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}