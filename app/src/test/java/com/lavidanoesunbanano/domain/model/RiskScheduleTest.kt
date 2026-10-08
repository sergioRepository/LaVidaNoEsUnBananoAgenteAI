package com.lavidanoesunbanano.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class RiskScheduleTest {

    @Test
    fun crossesMidnight_whenStartGreaterThanEnd_returnsTrue() {
        val schedule = RiskSchedule(startHour = 22, startMinute = 0, endHour = 2, endMinute = 0)
        assertTrue(schedule.crossesMidnight())
    }

    @Test
    fun crossesMidnight_whenStartLessThanEnd_returnsFalse() {
        val schedule = RiskSchedule(startHour = 14, startMinute = 0, endHour = 18, endMinute = 0)
        assertFalse(schedule.crossesMidnight())
    }

    @Test
    fun isWithin_crossingMidnight_boundariesEvaluatedCorrectly() {
        val schedule = RiskSchedule(startHour = 22, startMinute = 0, endHour = 2, endMinute = 0)

        // Inicio inclusivo: 22:00 -> dentro
        assertTrue(schedule.isWithin(LocalTime.of(22, 0)))

        // Antes del inicio: 21:59 -> fuera
        assertFalse(schedule.isWithin(LocalTime.of(21, 59)))

        // Medianoche: 00:00 -> dentro
        assertTrue(schedule.isWithin(LocalTime.of(0, 0)))

        // Madrugada dentro: 01:59 -> dentro
        assertTrue(schedule.isWithin(LocalTime.of(1, 59)))

        // Fin exclusivo: 02:00 -> fuera
        assertFalse(schedule.isWithin(LocalTime.of(2, 0)))

        // Después del fin: 02:01 -> fuera
        assertFalse(schedule.isWithin(LocalTime.of(2, 1)))
    }

    @Test
    fun isWithin_dayWindow_boundariesEvaluatedCorrectly() {
        val schedule = RiskSchedule(startHour = 14, startMinute = 0, endHour = 18, endMinute = 0)

        // Inicio inclusivo: 14:00 -> dentro
        assertTrue(schedule.isWithin(LocalTime.of(14, 0)))

        // Antes del inicio: 13:59 -> fuera
        assertFalse(schedule.isWithin(LocalTime.of(13, 59)))

        // Intermedio: 16:30 -> dentro
        assertTrue(schedule.isWithin(LocalTime.of(16, 30)))

        // Justo antes del fin: 17:59 -> dentro
        assertTrue(schedule.isWithin(LocalTime.of(17, 59)))

        // Fin exclusivo: 18:00 -> fuera
        assertFalse(schedule.isWithin(LocalTime.of(18, 0)))
    }
}
