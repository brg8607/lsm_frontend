package com.example.applsm.ui.theme

import androidx.compose.ui.graphics.Color

// --- COLORES ---
val CyanLsm = Color(0xFF78d5fb)
val PinkLsm = Color(0xFFFFB6C1)
val GrayBg = Color(0xFFF5F5F5)
val LockedGray = Color(0xFFE0E0E0)
val GoldStar = Color(0xFFFFD700)
val FireOrange = Color(0xFFFF5722)
val CorrectGreen = Color(0xFF4CAF50)
val WrongRed = Color(0xFFF44336)

// --- CONFIGURACIÓN ---
const val BASE_URL_FILES = "http://10.0.2.2:3000"
// REEMPLAZA CON TU CLIENT ID REAL
const val GOOGLE_WEB_CLIENT_ID = "796116611594-5m3l77aigcg2847s9nb9npmn79b358e3.apps.googleusercontent.com"

fun fixUrl(url: String?): String? {
    if (url.isNullOrEmpty()) return null
    return if (url.startsWith("http")) url else "$BASE_URL_FILES$url"
}