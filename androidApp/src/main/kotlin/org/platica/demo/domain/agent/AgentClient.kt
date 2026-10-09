package org.platica.demo.domain.agent

import org.platica.demo.domain.model.AgentRequest
import org.platica.demo.domain.model.AgentResponse

interface AgentClient {
    suspend fun respond(request: AgentRequest): AgentResponse
}
