package com.icyapps.howmuchlonger.data.di

import com.icyapps.howmuchlonger.data.model.PublicHolidayApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://date.nager.at/api/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun providePublicHolidayApi(retrofit: Retrofit): PublicHolidayApi =
        retrofit.create(PublicHolidayApi::class.java)
} 