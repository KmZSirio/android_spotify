package com.bustasirio.triskl.di

import com.bustasirio.triskl.data.network.SpotifyAccountsClient
import com.bustasirio.triskl.data.network.SpotifyApiClient
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

    // * To prevent typo :b
    private const val accountsRetrofit : String = "AccountsRetrofit"
    private const val apiRetrofit : String = "ApiRetrofit"

    @Singleton
    @Provides
    @Named(accountsRetrofit)
    fun providesAccountsRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideSpotifyAccountsClient(@Named(accountsRetrofit) retrofit: Retrofit): SpotifyAccountsClient {
        return retrofit.create(SpotifyAccountsClient::class.java)
    }

    @Singleton
    @Provides
    @Named(apiRetrofit)
    fun providesApiRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.spotify.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideSpotifyApiClient(@Named(apiRetrofit) retrofit: Retrofit): SpotifyApiClient {
        return retrofit.create(SpotifyApiClient::class.java)
    }


}