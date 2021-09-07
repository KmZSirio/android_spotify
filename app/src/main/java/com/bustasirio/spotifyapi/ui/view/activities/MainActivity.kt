package com.bustasirio.spotifyapi.ui.view.activities

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
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsClient
import com.bustasirio.spotifyapi.databinding.ActivityMainBinding
import com.bustasirio.spotifyapi.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLEncoder

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    private val baseUrl : String = "https://accounts.spotify.com"
    private val endpoint : String = "/authorize"
    private val clientId : String = "151eea6d2b7a4630ace40005f01405b6"
    private val stateSpotify : String = "34fFs29kd09"
    private val responseType : String = "code"
    private val scope : String = "playlist-read-private playlist-read-collaborative user-top-read"
    private val redirectUri : String = "bustasirio://callback"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding.logInButton.setOnClickListener{

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
        val uri : Uri? = intent.data

        if ( uri != null && uri.toString().startsWith(redirectUri) && !uri.toString().contains("access_denied") ) {
            Log.i("uri", "$uri")

            mainViewModel.code.value = uri.getQueryParameter("code")!!

            mainViewModel.getAuth()

            val response = mainViewModel.responseAuth.value

            if( response != null ) {
                Log.d("tagMainActivity", response.accessToken)
            }

//            val code : String = uri.getQueryParameter("code")!!
//
//            val authorizationString = "$clientId:${resources.getString(R.string.spotify_secret)}"
//            val bytes = URLEncoder.encode(authorizationString, "UTF-8")
//            Log.i("base64", bytes)
////            val base64Str = authorizationString.encode()
//            val base64Str = "XHgzMVx4MzVceDMxXHg2NVx4NjVceDYxXHgzNlx4NjRceDMyXHg2Mlx4MzdceDYxXHgzNFx4MzZceDMzXHgzMFx4NjFceDYzXHg2NVx4MzRceDMwXHgzMFx4MzBceDM1XHg2Nlx4MzBceDMxXHgzNFx4MzBceDM1XHg2Mlx4MzZceDNhXHg2MVx4NjJceDM0XHgzOFx4MzVceDY2XHg2NVx4MzlceDY1XHg2NFx4MzdceDYzXHgzNFx4MzRceDM5XHgzNVx4NjJceDY0XHgzN1x4NjNceDYzXHgzOVx4NjJceDY2XHg2Mlx4NjJceDM3XHgzOFx4NjNceDMwXHg2NFx4NjE="
//
//            Log.i("base64", base64Str)
//            val authorizationBasic = "Basic $base64Str"
//
//            val builder : Retrofit.Builder = Retrofit.Builder()
//                .baseUrl(baseUrl)
//                .addConverterFactory(GsonConverterFactory.create())
//
//            val retrofit : Retrofit = builder.build()
//
//            val client : SpotifyAccountsClient = retrofit.create(SpotifyAccountsClient::class.java)
//
//            val accessTokenCall : Call<AuthorizationModel> = client.getAccessToken(
//                authorizationBasic,
//                "authorization_code",
//                code,
//                redirectUri
//            )
//
//            accessTokenCall.enqueue(object : Callback<AuthorizationModel> {
//                override fun onResponse(
//                    call: Call<AuthorizationModel>,
//                    response: Response<AuthorizationModel>
//                ) {
//                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    startActivity(intent)
//                    finish()
//                }
//
//                override fun onFailure(call: Call<AuthorizationModel>, t: Throwable?) {
//                    Log.i("onFailure call ", "$t")
//                    Toast.makeText(
//                        this@MainActivity,
//                        "Something wrong occurred",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            })


        }
    }
}