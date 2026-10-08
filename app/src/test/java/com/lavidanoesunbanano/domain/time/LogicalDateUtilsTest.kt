package com.lavidanoesunbanano.domain.time

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class LogicalDateUtilsTest {

    private val zoneId = ZoneId.of("America/Bogota")

    @Test
    fun getLogicalDate_before4AM_belongsToPreviousCalendarDay() {
        // 2026-10-08 a las 02:30 AM pertenece al día lógico 2026-10-07
        val localDateTime = LocalDateTime.of(2026, 10, 8, 2, 30, 0)
        val instant = localDateTime.atZone(zoneId).toInstant()

        val logicalDate = LogicalDateUtils.getLogicalDate(instant, zoneId)

        assertEquals(LocalDate.of(2026, 10, 7), logicalDate)
    }

    @Test
    fun getLogicalDate_atExact4AM_belongsToCurrentCalendarDay() {
        // 2026-10-08 a las 04:00 AM inicia el nuevo día lógico 2026-10-08
        val localDateTime = LocalDateTime.of(2026, 10, 8, 4, 0, 0)
        val instant = localDateTime.atZone(zoneId).toInstant()

        val logicalDate = LogicalDateUtils.getLogicalDate(instant, zoneId)

        assertEquals(LocalDate.of(2026, 10, 8), logicalDate)
    }

    @Test
    fun getLogicalDate_lateNight_belongsToCurrentCalendarDay() {
        // 2026-10-07 a las 23:45 pertenece al día lógico 2026-10-07
        val localDateTime = LocalDateTime.of(2026, 10, 7, 23, 45, 0)
        val instant = localDateTime.atZone(zoneId).toInstant()

        val logicalDate = LogicalDateUtils.getLogicalDate(instant, zoneId)

        assertEquals(LocalDate.of(2026, 10, 7), logicalDate)
    }

    @Test
    fun getLogicalDayStartInstant_isExactly4AM() {
        val targetDate = LocalDate.of(2026, 10, 7)
        val startInstant = LogicalDateUtils.getLogicalDayStartInstant(targetDate, zoneId)

        val localStart = LocalDateTime.ofInstant(startInstant, zoneId)
        assertEquals(2026, localStart.year)
        assertEquals(10, localStart.monthValue)
        assertEquals(7, localStart.dayOfMonth)
        assertEquals(4, localStart.hour)
        assertEquals(0, localStart.minute)
    }
}
