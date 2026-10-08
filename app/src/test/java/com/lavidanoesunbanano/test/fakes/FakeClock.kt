package com.lavidanoesunbanano.test.fakes

import com.lavidanoesunbanano.domain.time.Clock
import java.time.Instant
import java.time.ZoneId

class FakeClock(
    var currentInstant: Instant = Instant.parse("2026-10-07T22:30:00Z"),
    var currentZoneId: ZoneId = ZoneId.of("America/Bogota")
) : Clock {

    override fun now(): Instant = currentInstant
    override fun zoneId(): ZoneId = currentZoneId

    fun setTime(instant: Instant) {
        currentInstant = instant
    }

    fun advanceBySeconds(seconds: Long) {
        currentInstant = currentInstant.plusSeconds(seconds)
    }

    fun advanceByMinutes(minutes: Long) {
        currentInstant = currentInstant.plusSeconds(minutes * 60L)
    }
}
