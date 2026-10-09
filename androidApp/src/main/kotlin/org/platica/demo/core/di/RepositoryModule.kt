package org.platica.demo.core.di

import org.platica.demo.data.local.datastore.SettingsRepositoryImpl
import org.platica.demo.data.local.room.InterventionRepositoryImpl
import org.platica.demo.data.local.room.SessionRepositoryImpl
import org.platica.demo.data.local.room.TriggerAppRepositoryImpl
import org.platica.demo.domain.repository.InterventionRepository
import org.platica.demo.domain.repository.SessionRepository
import org.platica.demo.domain.repository.SettingsRepository
import org.platica.demo.domain.repository.TriggerAppRepository
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
