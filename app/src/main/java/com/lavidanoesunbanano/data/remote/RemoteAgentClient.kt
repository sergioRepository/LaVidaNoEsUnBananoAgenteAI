package com.lavidanoesunbanano.data.remote

import android.util.Log
import com.lavidanoesunbanano.domain.agent.AgentClient
import com.lavidanoesunbanano.domain.agent.FakeAgentClient
import com.lavidanoesunbanano.domain.model.AgentRequest
import com.lavidanoesunbanano.domain.model.AgentResponse
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class RemoteAgentClient @Inject constructor(
    private val apiServiceProvider: Provider<AgentApiService>,
    private val fakeAgentClient: FakeAgentClient
) : AgentClient {

    companion object {
        private const val TAG = "RemoteAgentClient"
        const val TIMEOUT_MILLIS = 8000L // 8 segundos
    }

    override suspend fun respond(request: AgentRequest): AgentResponse {
        return try {
            withTimeout(TIMEOUT_MILLIS) {
                apiServiceProvider.get().postIntervention(request)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error o timeout al consultar RemoteAgentClient. Cayendo a FakeAgentClient.", e)
            fakeAgentClient.respond(request)
        }
    }
}
