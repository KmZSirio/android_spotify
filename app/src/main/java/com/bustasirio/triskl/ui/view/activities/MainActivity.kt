package com.bustasirio.triskl.ui.view.activities

import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.bustasirio.triskl.core.Constants.Companion.CLIENT_ID
import com.bustasirio.triskl.core.Constants.Companion.REDIRECT_URI
import com.bustasirio.triskl.core.Constants.Companion.SCOPE
import com.bustasirio.triskl.core.showToast
import com.bustasirio.triskl.databinding.ActivityMainBinding
import com.bustasirio.triskl.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Button
import com.bustasirio.triskl.R
import com.bustasirio.triskl.core.Resource

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainVM: MainViewModel by viewModels()

    private val baseUrl: String = "https://accounts.spotify.com"
    private val endpoint: String = "/authorize"
    private val stateSpotify: String = "34fFs29kd09"
    private val responseType: String = "code"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("tagMain", "onCreate")

        if (getPrefs()) {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.custom_dialog)
            dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_background))
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.setCancelable(false)

            dialog.show()

            val btnDialog: Button = dialog.findViewById(R.id.btnDialog)

            btnDialog.setOnClickListener {
                dialog.dismiss()
                val sharedPrefs = getSharedPreferences(
                    getString(R.string.preference_file_key),
                    Context.MODE_PRIVATE
                )
                with(sharedPrefs.edit()) {
                    putBoolean(getString(R.string.spotify_first_time), false)
                    apply()
                }
            }
        }

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
                Uri.parse("$baseUrl$endpoint?client_id=$CLIENT_ID&response_type=$responseType&redirect_uri=$REDIRECT_URI&scope=$SCOPE&state=$stateSpotify")
            )
        }
    }

    private fun getPrefs(): Boolean {
        val sharedPrefs =
            this.getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        return sharedPrefs.getBoolean(getString(R.string.spotify_first_time), true)
    }

    override fun onResume() {
        super.onResume()
        val uri: Uri? = intent.data

        if (uri != null && uri.toString().startsWith(REDIRECT_URI) && !uri.toString()
                .contains("access_denied")
        ) {
            mainVM.code.value = uri.getQueryParameter("code")!!

            mainVM.safeGetAuth()

            mainVM.authResponse.observe(this, { response ->
                when (response) {
                    is Resource.Success -> {
                        val sharedPrefs = getSharedPreferences(
                            getString(R.string.preference_file_key),
                            Context.MODE_PRIVATE
                        )
                        with(sharedPrefs.edit()) {
                            putString(getString(R.string.spotify_access_token), response.data?.accessToken)
                            putString(getString(R.string.spotify_token_type), response.data?.tokenType)
                            putString(getString(R.string.spotify_refresh_token), response.data?.refreshToken)
                            putBoolean(getString(R.string.spotify_logged), true)
                            apply()
                        }

                        val intent = Intent(this, LobbyActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    is Resource.Error -> {
                        if (response.message == "internet")
                            showToast(this, getString(R.string.internet_problem))
                        else
                            showToast(this, response.message ?: getString(R.string.something_wrong))
                    }
                    is Resource.Loading -> {}
                }
            })
        }
    }
}