package com.bustasirio.spotifyapi.ui.viewmodel

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsService
import com.bustasirio.spotifyapi.ui.view.activities.HomeActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor( private val service: SpotifyAccountsService): ViewModel() {

    private val response = MutableLiveData<AuthorizationModel>()
    val responseAuth : LiveData<AuthorizationModel> get() = response
    val code = MutableLiveData<String>()

//    val code : String = uri.getQueryParameter("code")!!
//    private val authorizationString = "$clientId:${resources.getString(R.string.spotify_secret)}"
//    private val base64Str = "XHgzMVx4MzVceDMxXHg2NVx4NjVceDYxXHgzNlx4NjRceDMyXHg2Mlx4MzdceDYxXHgzNFx4MzZceDMzXHgzMFx4NjFceDYzXHg2NVx4MzRceDMwXHgzMFx4MzBceDM1XHg2Nlx4MzBceDMxXHgzNFx4MzBceDM1XHg2Mlx4MzZceDNhXHg2MVx4NjJceDM0XHgzOFx4MzVceDY2XHg2NVx4MzlceDY1XHg2NFx4MzdceDYzXHgzNFx4MzRceDM5XHgzNVx4NjJceDY0XHgzN1x4NjNceDYzXHgzOVx4NjJceDY2XHg2Mlx4NjJceDM3XHgzOFx4NjNceDMwXHg2NFx4NjE="
//    private val authorizationBasic = "Basic $base64Str"
    private val authorizationBasic = "Basic MTUxZWVhNmQyYjdhNDYzMGFjZTQwMDA1ZjAxNDA1YjY6YWI0ODVmZTllZDdjNDQ5NWJkN2NjOWJmYmI3OGMwZGE="
    private val redirectUri : String = "bustasirio://callback"



    fun getAuth() = viewModelScope.launch {
        service.getAuth(
            authorizationBasic,
            "authorization_code",
            code.value!!,
            redirectUri
        ).let {

            if ( it.isSuccessful ) {
                response.postValue(it.body())
            } else {
                Log.d("tagMainViewModel", "getAuth Error  ${it.code()}")
                Log.d("tagMainViewModel", "getAuth Code   ${code.value}")
            }
        }
    }
}