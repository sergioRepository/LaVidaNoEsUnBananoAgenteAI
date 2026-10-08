package com.lavidanoesunbanano.domain.agent

import com.lavidanoesunbanano.domain.model.AgentRequest
import com.lavidanoesunbanano.domain.model.AgentStrategy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class FakeAgentClientTest {

    private lateinit var fakeClient: FakeAgentClient

    @Before
    fun setup() {
        fakeClient = FakeAgentClient()
    }

    @Test
    fun respond_whenRelapsedToday_returnsRelapseSupportStrategy() = runBlocking {
        val request = AgentRequest(
            userId = "user-123",
            appCategory = "red social",
            sessionMinutes = 15,
            localHour = 23,
            reason = "TRIGGER_APP_IN_RISK_WINDOW",
            emotion = "ansiedad",
            userText = null,
            relapsedToday = true
        )

        val response = fakeClient.respond(request)

        assertEquals(AgentStrategy.RELAPSE_SUPPORT, response.strategy)
        assertFalse(response.isCrisis)
    }

    @Test
    fun respond_whenAnxietyEmotion_returnsBreathingStrategy() = runBlocking {
        val request = AgentRequest(
            userId = "user-123",
            appCategory = "red social",
            sessionMinutes = 10,
            localHour = 15,
            reason = "SESSION_THRESHOLD_EXCEEDED",
            emotion = "ansiedad",
            userText = null,
            relapsedToday = false
        )

        val response = fakeClient.respond(request)

        assertEquals(AgentStrategy.BREATHING, response.strategy)
    }

    @Test
    fun respond_whenBoredomEmotion_returnsAlternativeStrategy() = runBlocking {
        val request = AgentRequest(
            userId = "user-123",
            appCategory = "video",
            sessionMinutes = 20,
            localHour = 16,
            reason = "SESSION_THRESHOLD_EXCEEDED",
            emotion = "aburrimiento",
            userText = null,
            relapsedToday = false
        )

        val response = fakeClient.respond(request)

        assertEquals(AgentStrategy.ALTERNATIVE, response.strategy)
    }

    @Test
    fun respond_whenHabitEmotion_returnsReframeStrategy() = runBlocking {
        val request = AgentRequest(
            userId = "user-123",
            appCategory = "juegos",
            sessionMinutes = 25,
            localHour = 18,
            reason = "SESSION_THRESHOLD_EXCEEDED",
            emotion = "costumbre",
            userText = null,
            relapsedToday = false
        )

        val response = fakeClient.respond(request)

        assertEquals(AgentStrategy.REFRAME, response.strategy)
    }
}
