package org.platica.demo.data.source

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealUsageEventSource @Inject constructor(
    @ApplicationContext private val context: Context
) : UsageEventSource {

    private val usageStatsManager: UsageStatsManager? by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    override fun queryEvents(startTimeEpochMilli: Long, endTimeEpochMilli: Long): List<UsageEventRecord> {
        val manager = usageStatsManager ?: return emptyList()
        val usageEvents = manager.queryEvents(startTimeEpochMilli, endTimeEpochMilli) ?: return emptyList()

        val results = mutableListOf<UsageEventRecord>()
        val seen = mutableSetOf<String>()
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            val pkg = event.packageName ?: continue
            val time = event.timeStamp

            val type: UsageEventType? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> UsageEventType.FOREGROUND
                    UsageEvents.Event.ACTIVITY_PAUSED -> UsageEventType.BACKGROUND
                    else -> null
                }
            } else {
                when (event.eventType) {
                    @Suppress("DEPRECATION")
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> UsageEventType.FOREGROUND
                    @Suppress("DEPRECATION")
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> UsageEventType.BACKGROUND
                    else -> null
                }
            }

            if (type != null) {
                val key = "$pkg-$time-$type"
                if (seen.add(key)) {
                    results.add(UsageEventRecord(packageName = pkg, timestampEpochMilli = time, eventType = type))
                }
            }
        }

        return results.sortedBy { it.timestampEpochMilli }
    }
}
