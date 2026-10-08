package com.lavidanoesunbanano.domain.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Utilidades para el cálculo del día lógico.
 * El día lógico inicia a las 04:00 AM para que una noche 22:00-02:00 pertenezca al mismo día.
 */
object LogicalDateUtils {
    val LOGICAL_DAY_START_TIME: LocalTime = LocalTime.of(4, 0, 0)

    /**
     * Retorna la fecha local correspondiente al día lógico de un instante dado.
     */
    fun getLogicalDate(instant: Instant, zoneId: ZoneId): LocalDate {
        val localDateTime = LocalDateTime.ofInstant(instant, zoneId)
        return if (localDateTime.toLocalTime().isBefore(LOGICAL_DAY_START_TIME)) {
            localDateTime.toLocalDate().minusDays(1)
        } else {
            localDateTime.toLocalDate()
        }
    }

    /**
     * Retorna el instante exacto de inicio del día lógico (04:00:00 local).
     */
    fun getLogicalDayStartInstant(date: LocalDate, zoneId: ZoneId): Instant {
        return LocalDateTime.of(date, LOGICAL_DAY_START_TIME)
            .atZone(zoneId)
            .toInstant()
    }

    /**
     * Retorna el instante exacto de fin del día lógico (03:59:59.999 local del día siguiente).
     */
    fun getLogicalDayEndInstant(date: LocalDate, zoneId: ZoneId): Instant {
        return LocalDateTime.of(date.plusDays(1), LOGICAL_DAY_START_TIME)
            .atZone(zoneId)
            .toInstant()
    }
}
