package org.platica.demo.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AgentResponse(
    @SerialName("message")
    val message: String,

    @SerialName("strategy")
    val strategy: AgentStrategy, // "breathing" | "reframe" | "alternative" | "relapse_support"

    @SerialName("is_crisis")
    val isCrisis: Boolean = false
)
