package com.lavidanoesunbanano.core.time

import com.lavidanoesunbanano.domain.time.Clock
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemClock @Inject constructor() : Clock {
    override fun now(): Instant = Instant.now()
    override fun zoneId(): ZoneId = ZoneId.systemDefault()
}
