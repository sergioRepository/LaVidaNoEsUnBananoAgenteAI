package org.platica.demo.domain.time

import java.time.Instant
import java.time.ZoneId

/**
 * Abstracción de reloj inyectable para dominio y pruebas.
 * Kotlin puro sin dependencias de Android.
 */
interface Clock {
    fun now(): Instant
    fun zoneId(): ZoneId
}
