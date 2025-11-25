package com.example.applsm.ui

import android.net.Uri
import android.widget.Toast
// Se eliminó androidx.annotation.OptIn para usar la nativa de Kotlin si es necesario o directa
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.applsm.data.Categoria
import com.example.applsm.data.Sena
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// COLORES
val CyanLsm = Color(0xFF78d5fb)
val PinkLsm = Color(0xFFFFB6C1)

@OptIn(ExperimentalMaterial3Api::class) // Solución explícita
@Composable
fun AppNavigation(viewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, viewModel) }
        composable("home") { HomeScreen(navController, viewModel) }

        composable("dictionary/{catId}/{catName}") { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("catId")?.toIntOrNull()
            val catName = backStackEntry.arguments?.getString("catName") ?: "Diccionario"
            DictionaryScreen(navController, viewModel, catId, catName)
        }

        composable("detail/{senaJson}") { backStackEntry ->
            val json = backStackEntry.arguments?.getString("senaJson") ?: ""

            // Parseamos el objeto FUERA de la llamada a la UI para evitar try-catch en composable
            val senaObjeto = remember(json) {
                try {
                    if (json.isNotEmpty()) Gson().fromJson(json, Sena::class.java) else null
                } catch (e: Exception) {
                    null
                }
            }

            if (senaObjeto != null) {
                DetailScreen(navController, senaObjeto)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error al cargar la seña")
                }
            }
        }

        composable("quiz") { QuizScreen(navController, viewModel) }
    }
}

// 1. LOGIN SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(nav: NavController, vm: AppViewModel) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val context = LocalContext.current
    val uiState = vm.uiState

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            Toast.makeText(context, uiState.message, Toast.LENGTH_LONG).show()
            vm.uiState = UiState.Idle
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(CyanLsm), contentAlignment = Alignment.Center) {
            Text("👋", fontSize = 40.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text("SignApp LSM", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { vm.login(email, pass) { nav.navigate("home") { popUpTo("login") { inclusive = true } } } },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanLsm)
        ) {
            if (vm.uiState is UiState.Loading) CircularProgressIndicator(color = Color.White) else Text("Entrar")
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { vm.guestLogin { nav.navigate("home") } }) {
            Text("Entrar como Invitado", color = Color.Gray)
        }
    }
}

// 2. HOME SCREEN
@OptIn(ExperimentalMaterial3Api::class) // Necesario para Scaffold y TopAppBar
@Composable
fun HomeScreen(nav: NavController, vm: AppViewModel) {
    LaunchedEffect(Unit) { vm.cargarHome() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola, ${vm.currentUserName ?: ""}", fontWeight = FontWeight.Bold)
                        Text("¿Qué aprenderás hoy?", fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    IconButton(onClick = { vm.logout { nav.navigate("login") } }) {
                        Icon(Icons.Default.ExitToApp, "Salir")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { nav.navigate("dictionary/-1/Búsqueda Global") }, containerColor = PinkLsm) {
                Icon(Icons.Default.Search, "Buscar")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (vm.currentUserType != "invitado") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { nav.navigate("quiz") },
                    colors = CardDefaults.cardColors(containerColor = CyanLsm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Quiz del Día", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
                            Text("Gana puntos hoy", color = Color.White)
                        }
                        Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            Text("Categorías", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vm.categorias) { cat ->
                    CategoryItem(cat) {
                        nav.navigate("dictionary/${cat.id}/${cat.nombre}")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Necesario para Card en algunas versiones
@Composable
fun CategoryItem(cat: Categoria, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(cat.iconUrl ?: "📁", fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(cat.nombre, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

// 3. DICTIONARY SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(nav: NavController, vm: AppViewModel, catId: Int?, catName: String) {
    var query by remember { mutableStateOf("") }

    LaunchedEffect(catId) {
        vm.buscarSenas("", if (catId != -1) catId else null)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Atrás") }
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    vm.buscarSenas(it, if (catId != -1) catId else null)
                },
                placeholder = { Text("Buscar en $catName...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        if (vm.uiState is UiState.Loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(vm.senas) { sena ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable {
                                val json = URLEncoder.encode(Gson().toJson(sena), StandardCharsets.UTF_8.toString())
                                nav.navigate("detail/$json")
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (!sena.imagenUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = sena.imagenUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(Modifier.size(50.dp).background(Color.LightGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                    Text("👐")
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(sena.palabra, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// 4. DETAIL SCREEN (VIDEO)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun DetailScreen(nav: NavController, sena: Sena) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            if (sena.videoUrl.isNotEmpty()) {
                val mediaItem = MediaItem.fromUri(Uri.parse(sena.videoUrl))
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.Black)) {
            if (sena.videoUrl.isNotEmpty()) {
                AndroidView(factory = {
                    PlayerView(context).apply { player = exoPlayer }
                }, modifier = Modifier.fillMaxSize())
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Video no disponible", color = Color.White)
                }
            }
            IconButton(
                onClick = { nav.popBackStack() },
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
            ) {
                Icon(Icons.Default.ArrowBack, "Atrás", tint = Color.White)
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text(sena.palabra, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = CyanLsm)
            Text(sena.categoriaNombre ?: "General", fontSize = 14.sp, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            Text("Descripción", fontWeight = FontWeight.Bold)
            Text(sena.descripcion ?: "Sin descripción disponible.", color = Color.DarkGray)
        }
    }
}

// 5. QUIZ SCREEN
@Composable
fun QuizScreen(nav: NavController, vm: AppViewModel) {
    LaunchedEffect(Unit) { vm.cargarQuiz() }

    val quiz = vm.quizDelDia
    var currentIdx by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    if (vm.uiState is UiState.Loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (quiz == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay quiz disponible hoy")
            Button(onClick = { nav.popBackStack() }) { Text("Volver") }
        }
        return
    }

    if (finished) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.size(100.dp), tint = Color(0xFFFFD700))
            Text("¡Terminado!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Puntuación: $score", fontSize = 20.sp)
            Spacer(Modifier.height(24.dp))
            Button(onClick = { nav.popBackStack() }) { Text("Volver a Inicio") }
        }
        return
    }

    val question = quiz.preguntas.getOrNull(currentIdx)

    if (question != null) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            LinearProgressIndicator(
                progress = { (currentIdx + 1) / quiz.preguntas.size.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = PinkLsm,
            )
            Spacer(Modifier.height(16.dp))
            Text("Pregunta ${currentIdx + 1}", color = Color.Gray)
            Text(question.texto, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(24.dp))

            question.opciones.forEach { opt ->
                OutlinedButton(
                    onClick = {
                        score += 10
                        if (currentIdx < quiz.preguntas.size - 1) currentIdx++ else finished = true
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(opt, fontSize = 18.sp, color = Color.Black)
                }
            }
        }
    }
}