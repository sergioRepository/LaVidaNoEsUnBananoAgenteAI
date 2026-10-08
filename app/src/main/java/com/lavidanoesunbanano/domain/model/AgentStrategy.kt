package com.lavidanoesunbanano.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AgentStrategy(val value: String) {
    @SerialName("breathing")
    BREATHING("breathing"),

    @SerialName("reframe")
    REFRAME("reframe"),

    @SerialName("alternative")
    ALTERNATIVE("alternative"),

    @SerialName("relapse_support")
    RELAPSE_SUPPORT("relapse_support");

    companion object {
        fun fromValue(value: String): AgentStrategy {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: REFRAME
        }
    }
}
