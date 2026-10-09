package org.platica.demo.data.remote

import org.platica.demo.BuildConfig
import org.platica.demo.domain.agent.AgentClient
import org.platica.demo.domain.agent.FakeAgentClient
import org.platica.demo.domain.model.AgentRequest
import org.platica.demo.domain.model.AgentResponse
import org.platica.demo.domain.repository.SettingsRepository
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
