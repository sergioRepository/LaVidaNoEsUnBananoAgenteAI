package com.lavidanoesunbanano.data.local

import android.content.pm.ApplicationInfo
import android.os.Build
import com.lavidanoesunbanano.domain.model.AppCategory

object AppCategoryMapper {

    fun mapCategory(appInfo: ApplicationInfo, packageName: String): AppCategory {
        // Respaldo manual para apps comunes populares
        val lowerPkg = packageName.lowercase()
        when {
            lowerPkg.contains("instagram") ||
                    lowerPkg.contains("facebook") ||
                    lowerPkg.contains("tiktok") ||
                    lowerPkg.contains("twitter") ||
                    lowerPkg.contains("x.android") ||
                    lowerPkg.contains("threads") ||
                    lowerPkg.contains("reddit") -> return AppCategory.RED_SOCIAL

            lowerPkg.contains("youtube") ||
                    lowerPkg.contains("netflix") ||
                    lowerPkg.contains("twitch") ||
                    lowerPkg.contains("disney") ||
                    lowerPkg.contains("primevideo") -> return AppCategory.VIDEO

            lowerPkg.contains("whatsapp") ||
                    lowerPkg.contains("telegram") ||
                    lowerPkg.contains("messenger") ||
                    lowerPkg.contains("discord") ||
                    lowerPkg.contains("signal") -> return AppCategory.MENSAJERIA

            lowerPkg.contains("game") ||
                    lowerPkg.contains("candycrush") ||
                    lowerPkg.contains("roblox") ||
                    lowerPkg.contains("clash") -> return AppCategory.JUEGOS
        }

        // Categoría declarada por el sistema si API >= 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.RED_SOCIAL
                ApplicationInfo.CATEGORY_VIDEO -> return AppCategory.VIDEO
                ApplicationInfo.CATEGORY_GAME -> return AppCategory.JUEGOS
                else -> {}
            }
        }

        return AppCategory.OTRA
    }
}
