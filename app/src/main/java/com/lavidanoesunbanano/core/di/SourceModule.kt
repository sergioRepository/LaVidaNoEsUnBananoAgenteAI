package com.lavidanoesunbanano.core.di

import com.lavidanoesunbanano.data.source.RealUsageEventSource
import com.lavidanoesunbanano.data.source.UsageEventSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SourceModule {

    @Binds
    @Singleton
    abstract fun bindUsageEventSource(impl: RealUsageEventSource): UsageEventSource
}
