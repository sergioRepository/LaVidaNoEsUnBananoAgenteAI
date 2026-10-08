package com.lavidanoesunbanano.domain.agent

import com.lavidanoesunbanano.domain.model.AgentRequest
import com.lavidanoesunbanano.domain.model.AgentResponse

interface AgentClient {
    suspend fun respond(request: AgentRequest): AgentResponse
}
