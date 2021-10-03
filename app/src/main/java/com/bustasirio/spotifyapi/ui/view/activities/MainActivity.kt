package com.bustasirio.spotifyapi.ui.view.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.core.Constants.Companion.CLIENT_ID
import com.bustasirio.spotifyapi.core.Constants.Companion.REDIRECT_URI
import com.bustasirio.spotifyapi.databinding.ActivityMainBinding
import com.bustasirio.spotifyapi.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    // ! FIXME Crash when app doesn't have internet

    private val mainVM: MainViewModel by viewModels()

    private val baseUrl: String = "https://accounts.spotify.com"
    private val endpoint: String = "/authorize"
    private val stateSpotify: String = "34fFs29kd09"
    private val responseType: String = "code"
    private val scope: String =
        "playlist-read-private playlist-read-collaborative user-top-read " +
                "user-read-email user-read-recently-played playlist-modify-public " +
                "playlist-modify-private user-library-read user-modify-playback-state"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding.logInButton.setOnClickListener {
            val colorInt: Int = resources.getColor(R.color.spotifyBlack, resources.newTheme())

            val defaultColors = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(colorInt)
                .build()

            val builder = CustomTabsIntent.Builder()
            builder.setDefaultColorSchemeParams(defaultColors)
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(
                this,
                Uri.parse("$baseUrl$endpoint?client_id=$CLIENT_ID&response_type=$responseType&redirect_uri=$REDIRECT_URI&scope=$scope&state=$stateSpotify")
            )
        }

        binding.phoneButton.setOnClickListener {
            Toast.makeText(this, getString(R.string.decoration_main), Toast.LENGTH_SHORT).show()
        }

        binding.googleButton.setOnClickListener {
            Toast.makeText(this, getString(R.string.decoration_main), Toast.LENGTH_SHORT).show()
        }

        binding.facebookButton.setOnClickListener {
            Toast.makeText(this, getString(R.string.decoration_main), Toast.LENGTH_SHORT).show()
        }

        binding.signUpButton.setOnClickListener {
            Toast.makeText(this, getString(R.string.decoration_main), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val uri: Uri? = intent.data

        if (uri != null && uri.toString().startsWith(REDIRECT_URI) && !uri.toString()
                .contains("access_denied")
        ) {
            mainVM.code.value = uri.getQueryParameter("code")!!
            mainVM.auth.value = resources.getString(R.string.esl)

            mainVM.getAuth()

            mainVM.response.observe(this, {
//                Log.d("tagMainActivityResponse", it.accessToken)

                val sharedPrefs = getSharedPreferences(
                    getString(R.string.preference_file_key),
                    Context.MODE_PRIVATE
                )
                with(sharedPrefs.edit()) {
                    putString(getString(R.string.spotify_access_token), it.accessToken)
                    putString(getString(R.string.spotify_token_type), it.tokenType)
                    putString(getString(R.string.spotify_refresh_token), it.refreshToken)
                    putBoolean(getString(R.string.spotify_logged), true)
                    apply()
                }

                val intent = Intent(this, LobbyActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            })

            mainVM.errorResponse.observe(this, {
                if (it != null) {
                    Toast.makeText(this, "Error: $it, try again later.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error. Try again later.", Toast.LENGTH_SHORT).show()
                }
            })


        }
    }
}