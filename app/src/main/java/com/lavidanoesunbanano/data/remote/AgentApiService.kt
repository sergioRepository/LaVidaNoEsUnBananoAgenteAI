package com.lavidanoesunbanano.data.remote

import com.lavidanoesunbanano.domain.model.AgentRequest
import com.lavidanoesunbanano.domain.model.AgentResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AgentApiService {
    @POST("v1/intervention")
    suspend fun postIntervention(@Body request: AgentRequest): AgentResponse
}
