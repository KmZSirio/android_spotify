package com.bustasirio.spotifyapi.ui.view.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.databinding.ActivityMainBinding
import com.bustasirio.spotifyapi.ui.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainActivityViewModel by viewModels()

    private val baseUrl: String = "https://accounts.spotify.com"
    private val endpoint: String = "/authorize"
    private val clientId: String = "151eea6d2b7a4630ace40005f01405b6"
    private val stateSpotify: String = "34fFs29kd09"
    private val responseType: String = "code"
    private val scope: String = "playlist-read-private playlist-read-collaborative user-top-read"
    private val redirectUri: String = "bustasirio://callback"


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
                Uri.parse("$baseUrl$endpoint?client_id=$clientId&response_type=$responseType&redirect_uri=$redirectUri&scope=$scope&state=$stateSpotify")
            )

        }
    }

    override fun onResume() {
        super.onResume()
        val uri: Uri? = intent.data

        if (uri != null && uri.toString().startsWith(redirectUri) && !uri.toString()
                .contains("access_denied")
        ) {
            Log.i("uri", "$uri")

            mainViewModel.code.value = uri.getQueryParameter("code")!!
            mainViewModel.authorizationBasic.value = resources.getString(R.string.spotify_basic)

            mainViewModel.getAuth()

            mainViewModel.response.observe(this, {
                Log.d("tagMainActivityResponse", it.accessToken)

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

                startActivity(Intent(this, LobbyActivity::class.java))
                finish()
            })

            mainViewModel.errorResponse.observe(this, {
                if (it != null) {
                    Toast.makeText(this, "Error: $it, try again later.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error. Try again later.", Toast.LENGTH_SHORT).show()
                }
            })


        }
    }
}