package com.lavidanoesunbanano.test.fakes

import com.lavidanoesunbanano.data.source.UsageEventRecord
import com.lavidanoesunbanano.data.source.UsageEventSource
import com.lavidanoesunbanano.data.source.UsageEventType

class FakeUsageEventSource : UsageEventSource {

    private val events = mutableListOf<UsageEventRecord>()

    fun addEvent(packageName: String, timestampEpochMilli: Long, eventType: UsageEventType) {
        events.add(UsageEventRecord(packageName, timestampEpochMilli, eventType))
    }

    fun clear() {
        events.clear()
    }

    override fun queryEvents(startTimeEpochMilli: Long, endTimeEpochMilli: Long): List<UsageEventRecord> {
        return events.filter { it.timestampEpochMilli in startTimeEpochMilli..endTimeEpochMilli }
            .sortedBy { it.timestampEpochMilli }
    }
}
