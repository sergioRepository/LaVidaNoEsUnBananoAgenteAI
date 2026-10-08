package com.lavidanoesunbanano.domain.model

data class TriggerApp(
    val packageName: String,
    val appName: String,
    val isTrigger: Boolean = false,
    val category: AppCategory = AppCategory.OTRA
)
