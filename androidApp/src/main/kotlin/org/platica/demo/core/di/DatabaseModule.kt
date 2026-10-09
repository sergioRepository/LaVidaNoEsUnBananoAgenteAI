package org.platica.demo.core.di

import android.content.Context
import androidx.room.Room
import org.platica.demo.BuildConfig
import org.platica.demo.data.local.room.AppDatabase
import org.platica.demo.data.local.room.dao.ActiveSessionDao
import org.platica.demo.data.local.room.dao.DailyStatsDao
import org.platica.demo.data.local.room.dao.InterventionEventDao
import org.platica.demo.data.local.room.dao.TriggerAppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        val builder = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lavidanoesunbanano.db"
        )
        if (BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration()
        }
        return builder.build()
    }

    @Provides
    fun provideTriggerAppDao(db: AppDatabase): TriggerAppDao = db.triggerAppDao()

    @Provides
    fun provideInterventionEventDao(db: AppDatabase): InterventionEventDao = db.interventionEventDao()

    @Provides
    fun provideActiveSessionDao(db: AppDatabase): ActiveSessionDao = db.activeSessionDao()

    @Provides
    fun provideDailyStatsDao(db: AppDatabase): DailyStatsDao = db.dailyStatsDao()
}
