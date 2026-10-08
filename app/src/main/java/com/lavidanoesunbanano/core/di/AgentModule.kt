package com.lavidanoesunbanano.core.di

import com.lavidanoesunbanano.data.remote.AgentClientProvider
import com.lavidanoesunbanano.domain.agent.AgentClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AgentModule {

    @Binds
    @Singleton
    abstract fun bindAgentClient(impl: AgentClientProvider): AgentClient
}
