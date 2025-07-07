package com.icyapps.howmuchlonger.di

import android.content.Context
import androidx.room.Room
import com.icyapps.howmuchlonger.data.local.EventDao
import com.icyapps.howmuchlonger.data.local.EventDatabase
import com.icyapps.howmuchlonger.data.repository.EventRepositoryImpl
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import com.icyapps.howmuchlonger.data.model.PublicHolidayApi
import com.icyapps.howmuchlonger.data.source.PublicHolidayDataSource
import com.icyapps.howmuchlonger.data.source.PublicHolidayApiDataSource
import com.icyapps.howmuchlonger.data.store.PublicHolidayDataStore
import com.icyapps.howmuchlonger.data.store.PublicHolidayRoomDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideEventDatabase(
        @ApplicationContext context: Context
    ): EventDatabase {
        return Room.databaseBuilder(
            context,
            EventDatabase::class.java,
            "event_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: EventDatabase) = database.eventDao()

    @Provides
    @Singleton
    fun provideEventDataStore(eventDao: EventDao): com.icyapps.howmuchlonger.data.local.EventDataStore {
        return com.icyapps.howmuchlonger.data.local.EventRoomDataStore(eventDao)
    }

    @Provides
    @Singleton
    fun provideEventRepository(
        eventDataStore: com.icyapps.howmuchlonger.data.local.EventDataStore,
        publicHolidayDataSource: PublicHolidayDataSource,
        publicHolidayDataStore: PublicHolidayDataStore
    ): EventRepository {
        return EventRepositoryImpl(eventDataStore, publicHolidayDataSource, publicHolidayDataStore)
    }

    @Provides
    @Singleton
    fun providePublicHolidayDataSource(publicHolidayApi: PublicHolidayApi): PublicHolidayDataSource {
        return PublicHolidayApiDataSource(publicHolidayApi)
    }

    @Provides
    @Singleton
    fun providePublicHolidayDataStore(eventDao: EventDao): PublicHolidayDataStore {
        return PublicHolidayRoomDataStore(eventDao)
    }
} 