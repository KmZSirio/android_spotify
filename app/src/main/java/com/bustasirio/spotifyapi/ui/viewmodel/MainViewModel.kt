package com.bustasirio.spotifyapi.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.core.Constants.Companion.REDIRECT_URI
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val service: SpotifyAccountsService
) : ViewModel() {

    val response = MutableLiveData<AuthorizationModel>()
    val errorResponse = MutableLiveData<Int>()
    val code = MutableLiveData<String>()

    // ! TODO encode basic myself!
    val authorizationBasic = MutableLiveData<String>()

    fun getAuth() = viewModelScope.launch {
        service.getAuth(
            authorizationBasic.value!!,
            "authorization_code",
            code.value!!,
            REDIRECT_URI
        ).let {
            if (it.isSuccessful) {
                response.postValue(it.body())
            } else {
                errorResponse.postValue(it.code())
            }
        }
    }
}