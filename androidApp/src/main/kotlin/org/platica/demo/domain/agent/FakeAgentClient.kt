package org.platica.demo.domain.agent

import org.platica.demo.domain.model.AgentRequest
import org.platica.demo.domain.model.AgentResponse
import org.platica.demo.domain.model.AgentStrategy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAgentClient @Inject constructor() : AgentClient {

    override suspend fun respond(request: AgentRequest): AgentResponse {
        val (strategy, message) = when {
            request.relapsedToday -> {
                AgentStrategy.RELAPSE_SUPPORT to
                        "Reincidir es parte del proceso de aprendizaje, no un fracaso. Date un respiro amable y volvamos a empezar."
            }

            request.emotion.equals("ansiedad", ignoreCase = true) -> {
                AgentStrategy.BREATHING to
                        "La ansiedad te empuja a buscar alivio rápido en la pantalla. Respira hondo y regálate calma antes de decidir."
            }

            request.emotion.equals("estrés", ignoreCase = true) -> {
                AgentStrategy.BREATHING to
                        "El estrés sobrecarga tu mente. Esta pausa te devuelve el control y la serenidad que necesitas."
            }

            request.emotion.equals("aburrimiento", ignoreCase = true) -> {
                AgentStrategy.ALTERNATIVE to
                        "El aburrimiento busca estímulo inmediato. ¿Qué tal tomar un vaso de agua o estirarte dos minutos?"
            }

            request.emotion.equals("cansancio", ignoreCase = true) -> {
                AgentStrategy.ALTERNATIVE to
                        "Tu cuerpo te pide descanso genuino, no sobreestimulación digital. Prueba cerrar los ojos y descansar un momento."
            }

            request.emotion.equals("costumbre", ignoreCase = true) -> {
                AgentStrategy.REFRAME to
                        "Abriste esta app por pura inercia. Pregúntate con honestidad: ¿realmente necesitas estar aquí ahora mismo?"
            }

            else -> {
                AgentStrategy.REFRAME to
                        "Hacer una pausa consciente ahora te devuelve el tiempo que mereces para lo que de verdad importa."
            }
        }

        return AgentResponse(
            message = message,
            strategy = strategy,
            isCrisis = false
        )
    }
}
