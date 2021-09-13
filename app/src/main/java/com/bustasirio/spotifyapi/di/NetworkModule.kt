package com.bustasirio.spotifyapi.di

import com.bustasirio.spotifyapi.data.network.SpotifyAccountsClient
import com.bustasirio.spotifyapi.data.network.SpotifyApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    @Named("AccountsRetrofit")
    fun providesAccountsRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideSpotifyAccountsClient(@Named("AccountsRetrofit") retrofit: Retrofit): SpotifyAccountsClient {
        return retrofit.create(SpotifyAccountsClient::class.java)
    }

    @Singleton
    @Provides
    @Named("ApiRetrofit")
    fun providesApiRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.spotify.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideSpotifyApiClient(@Named("ApiRetrofit") retrofit: Retrofit): SpotifyApiClient {
        return retrofit.create(SpotifyApiClient::class.java)
    }


}