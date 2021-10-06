package com.bustasirio.triskl.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bustasirio.triskl.MyApplication
import com.bustasirio.triskl.core.Constants.Companion.REDIRECT_URI
import com.bustasirio.triskl.core.Resource
import com.bustasirio.triskl.core.hasInternetConnection
import com.bustasirio.triskl.data.model.AuthorizationModel
import com.bustasirio.triskl.data.network.SpotifyAccountsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    app: Application,
    private val service: SpotifyAccountsService
) : AndroidViewModel(app) {

    val authResponse: MutableLiveData<Resource<AuthorizationModel>> = MutableLiveData()

    val code = MutableLiveData<String>()
    val auth = MutableLiveData<String>()

    fun safeGetAuth() {
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) getAuth()
            else authResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when(t) {
                is IOException -> authResponse.postValue(Resource.Error("network"))
                else -> authResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    private fun getAuth() = viewModelScope.launch {
        service.getAuth(
            auth.value!!,
            "authorization_code",
            code.value!!,
            REDIRECT_URI
        ).let {
            if (it.isSuccessful) authResponse.postValue(Resource.Success(it.body()!!))
            else authResponse.postValue(Resource.Error(it.code().toString()))
        }
    }
}