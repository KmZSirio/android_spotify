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
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor( private val service: SpotifyAccountsService): ViewModel() {

    val response = MutableLiveData<AuthorizationModel>()
    val responseError = MutableLiveData<Int>()
    val code = MutableLiveData<String>()
    //TODO encode basic myself!
    val authorizationBasic = MutableLiveData<String>()

    private val redirectUri : String = "bustasirio://callback"

    fun getAuth() = viewModelScope.launch {
        service.getAuth(
            authorizationBasic.value!!,
            "authorization_code",
            code.value!!,
            redirectUri
        ).let {
            if ( it.isSuccessful ) {
                response.postValue(it.body())
            } else {
                responseError.postValue(it.code())
            }
        }
    }
}