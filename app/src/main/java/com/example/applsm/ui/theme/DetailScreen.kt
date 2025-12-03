package com.example.applsm.ui.screens.detail

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.applsm.data.Sena
import com.example.applsm.ui.theme.*

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun DetailScreen(nav: NavController, sena: Sena) {
    val context = LocalContext.current

    val fullVideoUrl = remember(sena.videoUrl) { fixUrl(sena.videoUrl) }
    val fullImageUrl = remember(sena.imagenUrl) { fixUrl(sena.imagenUrl) }

    val isVideoValido = remember(fullVideoUrl) {
        val url = fullVideoUrl
        if (url.isNullOrEmpty()) {
            false
        } else {
            !url.endsWith(".jpg", ignoreCase = true) &&
                    !url.endsWith(".png", ignoreCase = true) &&
                    !url.endsWith(".jpeg", ignoreCase = true)
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            if (isVideoValido && fullVideoUrl != null) {
                try {
                    setMediaItem(MediaItem.fromUri(Uri.parse(fullVideoUrl)))
                    prepare()
                    playWhenReady = true
                } catch (e: Exception) { Log.e("DEBUG", "Err: ${e.message}") }
            }
        }
    }

    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.Black)) {
            if (isVideoValido) {
                AndroidView(factory = { PlayerView(context).apply { player = exoPlayer } }, modifier = Modifier.fillMaxSize())
            } else {
                if (!fullImageUrl.isNullOrEmpty()) {
                    AsyncImage(model = fullImageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.VideocamOff, null, tint = Color.White, modifier = Modifier.size(48.dp))
                            Text("Sin contenido visual", color = Color.White)
                        }
                    }
                }
            }
            IconButton(onClick = { nav.popBackStack() }, modifier = Modifier.padding(16.dp).align(Alignment.TopStart)) {
                Icon(Icons.Default.ArrowBack, "Atrás", tint = Color.White)
            }
        }
        Column(modifier = Modifier.padding(24.dp)) {
            Text(sena.palabra, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = CyanLsm)
            Text(sena.categoriaNombre ?: "LSM", fontSize = 16.sp, color = Color.Gray)
            Spacer(Modifier.height(16.dp))
            Text("Descripción", fontWeight = FontWeight.Bold)
            Text(sena.descripcion ?: "Aprende esta seña practicando frente al espejo.", color = Color.DarkGray)
        }
    }
}