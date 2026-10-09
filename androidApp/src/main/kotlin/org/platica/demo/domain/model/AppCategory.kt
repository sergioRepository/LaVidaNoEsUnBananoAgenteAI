package org.platica.demo.domain.model

enum class AppCategory(val value: String) {
    RED_SOCIAL("red social"),
    VIDEO("video"),
    JUEGOS("juegos"),
    MENSAJERIA("mensajería"),
    OTRA("otra");

    companion object {
        fun fromString(value: String?): AppCategory {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: OTRA
        }
    }
}
