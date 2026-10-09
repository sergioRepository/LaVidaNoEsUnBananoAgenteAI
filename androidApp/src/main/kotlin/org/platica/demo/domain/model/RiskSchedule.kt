package org.platica.demo.domain.model

import java.time.LocalTime

data class RiskSchedule(
    val startHour: Int = 22,
    val startMinute: Int = 0,
    val endHour: Int = 2,
    val endMinute: Int = 0
) {
    val startTime: LocalTime get() = LocalTime.of(startHour, startMinute)
    val endTime: LocalTime get() = LocalTime.of(endHour, endMinute)

    fun crossesMidnight(): Boolean {
        return startTime.isAfter(endTime)
    }

    /**
     * Evalúa si una hora local se encuentra dentro de la ventana de riesgo.
     * Inicio inclusivo, fin exclusivo.
     */
    fun isWithin(time: LocalTime): Boolean {
        return if (crossesMidnight()) {
            !time.isBefore(startTime) || time.isBefore(endTime)
        } else {
            !time.isBefore(startTime) && time.isBefore(endTime)
        }
    }
}
