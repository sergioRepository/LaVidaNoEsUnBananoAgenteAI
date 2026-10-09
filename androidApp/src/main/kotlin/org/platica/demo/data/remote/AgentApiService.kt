package org.platica.demo.data.remote

import org.platica.demo.domain.model.AgentRequest
import org.platica.demo.domain.model.AgentResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AgentApiService {
    @POST("v1/intervention")
    suspend fun postIntervention(@Body request: AgentRequest): AgentResponse
}
