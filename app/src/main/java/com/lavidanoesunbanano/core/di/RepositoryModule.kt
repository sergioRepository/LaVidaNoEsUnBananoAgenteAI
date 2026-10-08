package com.lavidanoesunbanano.core.di

import com.lavidanoesunbanano.data.local.datastore.SettingsRepositoryImpl
import com.lavidanoesunbanano.data.local.room.InterventionRepositoryImpl
import com.lavidanoesunbanano.data.local.room.SessionRepositoryImpl
import com.lavidanoesunbanano.data.local.room.TriggerAppRepositoryImpl
import com.lavidanoesunbanano.domain.repository.InterventionRepository
import com.lavidanoesunbanano.domain.repository.SessionRepository
import com.lavidanoesunbanano.domain.repository.SettingsRepository
import com.lavidanoesunbanano.domain.repository.TriggerAppRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindTriggerAppRepository(impl: TriggerAppRepositoryImpl): TriggerAppRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindInterventionRepository(impl: InterventionRepositoryImpl): InterventionRepository
}
