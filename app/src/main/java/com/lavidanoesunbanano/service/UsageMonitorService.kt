package com.lavidanoesunbanano.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UsageMonitorService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
