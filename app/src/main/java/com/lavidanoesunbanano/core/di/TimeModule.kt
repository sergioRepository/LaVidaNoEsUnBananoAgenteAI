package com.lavidanoesunbanano.core.di

import com.lavidanoesunbanano.core.time.SystemClock
import com.lavidanoesunbanano.domain.time.Clock
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
