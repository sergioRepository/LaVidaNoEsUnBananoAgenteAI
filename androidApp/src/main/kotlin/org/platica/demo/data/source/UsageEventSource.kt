package org.platica.demo.data.source

enum class UsageEventType {
    FOREGROUND,
    BACKGROUND
}

data class UsageEventRecord(
    val packageName: String,
    val timestampEpochMilli: Long,
    val eventType: UsageEventType
)

interface UsageEventSource {
    fun queryEvents(startTimeEpochMilli: Long, endTimeEpochMilli: Long): List<UsageEventRecord>
}
