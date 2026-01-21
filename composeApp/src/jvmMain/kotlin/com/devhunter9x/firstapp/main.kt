package com.devhunter9x.firstapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "firstapp",
    ) {
        App()
    }
}