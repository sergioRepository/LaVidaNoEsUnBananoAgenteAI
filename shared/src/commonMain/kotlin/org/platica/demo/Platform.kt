package org.platica.demo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform