package com.lavidanoesunbanano.data.remote

import com.lavidanoesunbanano.BuildConfig
import com.lavidanoesunbanano.domain.agent.AgentClient
import com.lavidanoesunbanano.domain.agent.FakeAgentClient
import com.lavidanoesunbanano.domain.model.AgentRequest
import com.lavidanoesunbanano.domain.model.AgentResponse
import com.lavidanoesunbanano.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentClientProvider @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val fakeAgentClient: FakeAgentClient,
    private val remoteAgentClient: RemoteAgentClient
) : AgentClient {

    suspend fun getClient(): AgentClient {
        val settings = settingsRepository.getSettings().first()
        val isRemoteConfigured = BuildConfig.BASE_URL.isNotBlank()
        return if (settings.aiResponsesEnabled && isRemoteConfigured) {
            remoteAgentClient
        } else {
            fakeAgentClient
        }
    }

    override suspend fun respond(request: AgentRequest): AgentResponse {
        return getClient().respond(request)
    }
}
