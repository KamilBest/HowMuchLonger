package com.icyapps.howmuchlonger.di

import android.content.Context
import androidx.room.Room
import com.icyapps.howmuchlonger.data.local.EventDao
import com.icyapps.howmuchlonger.data.local.EventDatabase
import com.icyapps.howmuchlonger.data.repository.EventRepositoryImpl
import com.icyapps.howmuchlonger.domain.repository.EventRepository
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
    fun provideEventRepository(
        eventDao: EventDao,
        publicHolidayApi: com.icyapps.howmuchlonger.data.model.PublicHolidayApi
    ): EventRepository {
        return EventRepositoryImpl(eventDao, publicHolidayApi)
    }
} 