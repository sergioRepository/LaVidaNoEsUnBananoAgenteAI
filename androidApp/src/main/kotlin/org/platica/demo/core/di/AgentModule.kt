package org.platica.demo.core.di

import org.platica.demo.data.remote.AgentClientProvider
import org.platica.demo.domain.agent.AgentClient
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
