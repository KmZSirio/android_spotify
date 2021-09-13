package com.bustasirio.spotifyapi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.model.TopArtistsModel
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsService
import com.bustasirio.spotifyapi.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyActivityViewModel @Inject constructor(
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : ViewModel() {


}