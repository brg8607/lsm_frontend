package com.example.applsm.ui.utils

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.*

/**
 * Composable que proporciona una instancia de TextToSpeech lista para usar
 */
@Composable
fun rememberTextToSpeech(): TextToSpeech? {
    val context = LocalContext.current
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var isInitialized by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale("es", "MX")
                isInitialized = true
            }
        }

        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    return if (isInitialized) textToSpeech else null
}

/**
 * Función de extensión para reproducir texto
 */
fun TextToSpeech.hablar(texto: String) {
    speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)
}
