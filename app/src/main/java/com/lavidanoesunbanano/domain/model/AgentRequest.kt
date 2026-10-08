package com.lavidanoesunbanano.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AgentRequest(
    @SerialName("user_id")
    val userId: String,

    @SerialName("app_category")
    val appCategory: String, // "red social" | "video" | "juegos" | "mensajería" | "otra"

    @SerialName("session_minutes")
    val sessionMinutes: Int,

    @SerialName("local_hour")
    val localHour: Int,

    @SerialName("reason")
    val reason: String, // "TRIGGER_APP_IN_RISK_WINDOW" | "SESSION_THRESHOLD_EXCEEDED"

    @SerialName("emotion")
    val emotion: String? = null, // "aburrimiento" | "ansiedad" | "cansancio" | "estrés" | "costumbre" | null

    @SerialName("user_text")
    val userText: String? = null,

    @SerialName("relapsed_today")
    val relapsedToday: Boolean
)
