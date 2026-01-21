package com.devhunter9x.firstapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform