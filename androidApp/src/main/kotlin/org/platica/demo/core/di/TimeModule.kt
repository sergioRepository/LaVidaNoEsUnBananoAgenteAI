package org.platica.demo.core.di

import org.platica.demo.core.time.SystemClock
import org.platica.demo.domain.time.Clock
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeModule {

    @Binds
    @Singleton
    abstract fun bindClock(systemClock: SystemClock): Clock
}
