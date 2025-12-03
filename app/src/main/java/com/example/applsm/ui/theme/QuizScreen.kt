package com.example.applsm.ui.screens.quiz

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.applsm.data.Pregunta
import com.example.applsm.ui.AppViewModel
import com.example.applsm.ui.UiState
import com.example.applsm.ui.theme.*

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun QuizScreen(nav: NavController, vm: AppViewModel, catId: Int = -1, level: Int = 1, resume: Boolean = false) {
    LaunchedEffect(key1 = catId, key2 = level) {
        vm.cargarQuiz(catId, level)
    }

    val quiz = vm.quizDelDia
    var currentIdx by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    // ESTADOS PARA LA LÓGICA DE RESPUESTA
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswerChecked by remember { mutableStateOf(false) }

    // Recuperar progreso
    LaunchedEffect(vm.estadoProgreso) {
        if (!initialized && resume && vm.estadoProgreso != null && vm.estadoProgreso?.categoriaId == catId) {
            currentIdx = vm.estadoProgreso!!.indice
            initialized = true
        } else if (!initialized) {
            currentIdx = 0
            score = 0
            initialized = true
        }
    }

    if (vm.uiState is UiState.Loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (quiz == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cargando quiz..."); Button(onClick = { nav.popBackStack() }) { Text("Volver") }
        }
        return
    }

    if (finished) {
        QuizFinishedContent(nav, vm, score, catId, level)
        return
    }

    val question = quiz.preguntas.getOrNull(currentIdx)

    if (question != null) {
        // LOG: Ver datos de la pregunta actual
        SideEffect {
            Log.d("DEBUG_QUIZ", "Pregunta: '${question.texto}' | CorrectaRaw: '${question.respuestaCorrecta}'")
        }

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            // Barra de Progreso
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
                LinearProgressIndicator(
                    progress = { (currentIdx + 1) / quiz.preguntas.size.toFloat() },
                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = PinkLsm,
                    trackColor = GrayBg
                )
            }

            Spacer(Modifier.height(16.dp))

            // Multimedia
            MultimediaContent(question)

            Spacer(Modifier.height(16.dp))
            Text(question.texto, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)

            Spacer(Modifier.weight(1f))

            // LISTA DE OPCIONES
            question.opciones.forEach { opt ->

                val isSelected = opt == selectedOption

                // LÓGICA DE COLOR
                // Normalizamos las cadenas para comparar (trim y lowercase)
                val normalizedOpt = opt.trim().lowercase()
                val normalizedCorrect = question.respuestaCorrecta?.trim()?.lowercase() ?: ""
                
                val isCorrect = normalizedOpt == normalizedCorrect


                val backgroundColor = when {
                    isAnswerChecked && isCorrect -> CorrectGreen // Verde si es la correcta (al comprobar)
                    isAnswerChecked && isSelected && !isCorrect -> WrongRed // Rojo si elegí mal
                    !isAnswerChecked && isSelected -> CyanLsm // Azul al seleccionar (antes de comprobar)
                    else -> Color.White
                }

                val textColor = if (isAnswerChecked && (isCorrect || (isSelected && !isCorrect)) || (!isAnswerChecked && isSelected))
                    Color.White else Color.Black

                Button(
                    onClick = {
                        // Solo permite seleccionar si no se ha comprobado aún
                        if (!isAnswerChecked) {
                            selectedOption = opt
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, if(isSelected) CyanLsm else GrayBg),
                    elevation = ButtonDefaults.buttonElevation(2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(opt, fontSize = 18.sp, color = textColor)
                        if (isAnswerChecked) {
                            if (isCorrect) {
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                            } else if (isSelected) {
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.Cancel, null, tint = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // BOTÓN DE ACCIÓN (COMPROBAR -> SIGUIENTE)
            Button(
                onClick = {
                    if (!isAnswerChecked) {
                        // 1. Comprobar
                        if (selectedOption != null) {
                            isAnswerChecked = true
                            
                            val normSelected = selectedOption!!.trim().lowercase()
                            val normCorrect = question.respuestaCorrecta?.trim()?.lowercase() ?: ""
                            
                            Log.d("DEBUG_QUIZ", "Comprobando: Seleccionado='$normSelected' vs Correcto='$normCorrect'")
                            
                            if (normSelected == normCorrect) {
                                score += 10
                            }
                        }
                    } else {
                        // 2. Avanzar
                        if (currentIdx < quiz.preguntas.size - 1) {
                            currentIdx++
                            vm.guardarAvance(catId, level, currentIdx)
                            // Resetear para siguiente pregunta
                            selectedOption = null
                            isAnswerChecked = false
                        } else {
                            finished = true
                            vm.guardarAvance(catId, level, 10)
                            // Sumar puntos al finalizar el quiz
                            vm.sumarPuntos(score)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAnswerChecked) {
                        val normSel = selectedOption?.trim()?.lowercase() ?: ""
                        val normCorr = question.respuestaCorrecta?.trim()?.lowercase() ?: ""
                        if (normSel == normCorr) CorrectGreen else WrongRed
                    } else PinkLsm
                ),
                enabled = selectedOption != null // Deshabilitado si no ha seleccionado nada
            ) {
                Text(if (!isAnswerChecked) "Comprobar" else if (currentIdx < quiz.preguntas.size - 1) "Siguiente" else "Finalizar")
            }
        }
    }
}

@Composable
fun QuizFinishedContent(nav: NavController, vm: AppViewModel, score: Int, catId: Int, level: Int) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.size(100.dp), tint = GoldStar)
        Text(if (score >= 60) "¡Nivel Completado!" else "¡Sigue practicando!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = CyanLsm)
        Text("Puntuación: $score", fontSize = 20.sp, color = Color.Gray)
        Spacer(Modifier.height(24.dp))

        if (catId != -1) {
            Button(
                onClick = {
                    vm.guardarAvance(catId, level, 10)
                    nav.navigate("main") { popUpTo("main") { inclusive = true } }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanLsm),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Terminar y Volver") }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    vm.guardarAvance(catId, level, 10)
                    nav.navigate("quiz?catId=${catId + 1}&level=1&resume=false") {
                        popUpTo("main")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Siguiente Nivel", color = PinkLsm) }
        } else {
            Button(onClick = { nav.navigate("main") { popUpTo("main") { inclusive = true } } }, modifier = Modifier.fillMaxWidth()) { Text("Volver al Inicio") }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun MultimediaContent(pregunta: Pregunta) {
    val context = LocalContext.current
    val qVideoUrl = remember(pregunta.videoUrl) { fixUrl(pregunta.videoUrl) }
    val qImageUrl = remember(pregunta.imagenUrl) { fixUrl(pregunta.imagenUrl) }

    val isVideo = remember(qVideoUrl) {
        !qVideoUrl.isNullOrEmpty() && !qVideoUrl.endsWith(".jpg", true) && !qVideoUrl.endsWith(".png", true) && !qVideoUrl.endsWith(".jpeg", true)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
    ) {
        if (isVideo && qVideoUrl != null) {
            val exoPlayer = remember { 
                Log.d("DEBUG_VIDEO", "Creando nueva instancia de ExoPlayer")
                ExoPlayer.Builder(context).build() 
            }

            // Actualizar fuente de video cuando cambia la URL
            LaunchedEffect(qVideoUrl) {
                Log.d("DEBUG_VIDEO", "Cargando video: $qVideoUrl")
                exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(qVideoUrl)))
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }

            // Liberar recursos al salir de la pantalla o cambiar de tipo de contenido
            DisposableEffect(Unit) { 
                onDispose { 
                    Log.d("DEBUG_VIDEO", "Liberando ExoPlayer")
                    exoPlayer.release() 
                } 
            }

            AndroidView(
                factory = { 
                    PlayerView(context).apply { 
                        player = exoPlayer 
                        useController = true // Opcional: mostrar controles
                    } 
                }, 
                modifier = Modifier.fillMaxSize()
            )
        } else if (!qImageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = qImageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Mira la seña", color = Color.White)
            }
        }
    }
}