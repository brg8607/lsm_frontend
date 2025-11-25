package com.example.applsm.ui

import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
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

// URL BASE PARA RECURSOS
const val BASE_URL_FILES = "http://10.0.2.2:3000"

// Función para completar las URLs relativas que vienen del backend
fun fixUrl(url: String?): String? {
    if (url.isNullOrEmpty()) return null
    return if (url.startsWith("http")) url else "$BASE_URL_FILES$url"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, viewModel) }
        composable("register") { RegisterScreen(navController, viewModel) }
        composable("home") { HomeScreen(navController, viewModel) }

        composable("dictionary/{catId}/{catName}") { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("catId")?.toIntOrNull()
            val catName = backStackEntry.arguments?.getString("catName") ?: "Diccionario"
            DictionaryScreen(navController, viewModel, catId, catName)
        }

        composable("detail/{senaId}") { backStackEntry ->
            val senaId = backStackEntry.arguments?.getString("senaId")?.toIntOrNull()
            val senaObjeto = remember(senaId) {
                if (senaId != null) viewModel.obtenerSenaPorId(senaId) else null
            }

            if (senaObjeto != null) {
                DetailScreen(navController, senaObjeto)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: Seña no encontrada o cargando...")
                    Button(onClick = { navController.popBackStack() }) { Text("Volver") }
                }
            }
        }

        composable("quiz") { QuizScreen(navController, viewModel) }
    }
}

// --- PANTALLAS ---

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

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(CyanLsm), contentAlignment = Alignment.Center) { Text("👋", fontSize = 40.sp) }
        Spacer(Modifier.height(24.dp))
        Text("SignApp LSM", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { vm.login(email, pass) { nav.navigate("home") { popUpTo("login") { inclusive = true } } } }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = CyanLsm)) {
            if (vm.uiState is UiState.Loading) CircularProgressIndicator(color = Color.White) else Text("Entrar")
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { nav.navigate("register") }) { Text("¿No tienes cuenta? Regístrate aquí", color = PinkLsm) }
        TextButton(onClick = { vm.guestLogin { nav.navigate("home") } }) { Text("Entrar como Invitado", color = Color.Gray) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(nav: NavController, vm: AppViewModel) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val context = LocalContext.current
    val uiState = vm.uiState

    LaunchedEffect(uiState) { if (uiState is UiState.Error) { Toast.makeText(context, uiState.message, Toast.LENGTH_LONG).show(); vm.uiState = UiState.Idle } }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Crear Cuenta", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = CyanLsm)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Button(onClick = { if (nombre.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) vm.register(nombre, email, pass) { nav.navigate("home") { popUpTo("login") { inclusive = true } } } else Toast.makeText(context, "Llena todo", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = PinkLsm)) {
            if (vm.uiState is UiState.Loading) CircularProgressIndicator(color = Color.White) else Text("Registrarse")
        }
        TextButton(onClick = { nav.popBackStack() }) { Text("Volver", color = Color.Gray) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController, vm: AppViewModel) {
    LaunchedEffect(Unit) { vm.cargarHome() }
    Scaffold(
        topBar = { TopAppBar(title = { Column { Text("Hola, ${vm.currentUserName ?: ""}", fontWeight = FontWeight.Bold); Text("Aprende hoy", fontSize = 12.sp) } }, actions = { IconButton(onClick = { vm.logout { nav.navigate("login") } }) { Icon(Icons.Default.ExitToApp, "Salir") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)) },
        floatingActionButton = { FloatingActionButton(onClick = { nav.navigate("dictionary/-1/Búsqueda Global") }, containerColor = PinkLsm) { Icon(Icons.Default.Search, "Buscar") } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (vm.currentUserType != "invitado") {
                Card(modifier = Modifier.fillMaxWidth().height(100.dp).clickable { nav.navigate("quiz") }, colors = CardDefaults.cardColors(containerColor = CyanLsm)) {
                    Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Quiz del Día", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp); Text("Gana puntos", color = Color.White) }
                        Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
            Text("Categorías", fontWeight = FontWeight.Bold, fontSize = 18.sp); Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(vm.categorias) { cat -> CategoryItem(cat) { nav.navigate("dictionary/${cat.id}/${cat.nombre}") } }
            }
        }
    }
}

@Composable
fun CategoryItem(cat: Categoria, onClick: () -> Unit) {
    Card(modifier = Modifier.clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(cat.iconUrl ?: "📁", fontSize = 32.sp); Spacer(Modifier.height(8.dp)); Text(cat.nombre, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(nav: NavController, vm: AppViewModel, catId: Int?, catName: String) {
    var query by remember { mutableStateOf("") }
    LaunchedEffect(catId) { vm.buscarSenas("", if (catId != -1) catId else null) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Atrás") }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; vm.buscarSenas(it, if (catId != -1) catId else null) },
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
                    val fullImgUrl = fixUrl(sena.imagenUrl)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { nav.navigate("detail/${sena.id}") },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (!fullImgUrl.isNullOrEmpty()) {
                                AsyncImage(model = fullImgUrl, contentDescription = null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                            } else {
                                Box(Modifier.size(50.dp).background(Color.LightGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Text("👐") }
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

@androidx.annotation.OptIn(UnstableApi::class)
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

    LaunchedEffect(fullVideoUrl) {
        Log.d("DEBUG_VIDEO", "ID: ${sena.id} | URL: $fullVideoUrl | VideoValido: $isVideoValido")
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("DEBUG_VIDEO", "Error Exo: ${error.message}")
                }
            })

            if (isVideoValido && fullVideoUrl != null) {
                try {
                    setMediaItem(MediaItem.fromUri(Uri.parse(fullVideoUrl)))
                    prepare()
                    playWhenReady = true
                } catch (e: Exception) {
                    Log.e("DEBUG_VIDEO", "Err: ${e.message}")
                }
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
            Text(sena.categoriaNombre ?: "LSM", fontSize = 14.sp, color = Color.Gray)
            Spacer(Modifier.height(16.dp))
            Text("Descripción", fontWeight = FontWeight.Bold)
            Text(sena.descripcion ?: "Aprende esta seña practicando frente al espejo.", color = Color.DarkGray)
        }
    }
}

@Composable
fun QuizScreen(nav: NavController, vm: AppViewModel) {
    LaunchedEffect(Unit) { vm.cargarQuiz() }
    val quiz = vm.quizDelDia
    var currentIdx by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    if (vm.uiState is UiState.Loading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
    if (quiz == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay quiz hoy"); Button(onClick = { nav.popBackStack() }) { Text("Volver") } }; return }

    if (finished) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.size(100.dp), tint = Color(0xFFFFD700))
            Text("¡Terminado!", fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Puntuación: $score", fontSize = 20.sp)
            Spacer(Modifier.height(24.dp)); Button(onClick = { nav.popBackStack() }) { Text("Inicio") }
        }
        return
    }
    val question = quiz.preguntas.getOrNull(currentIdx)
    if (question != null) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            LinearProgressIndicator(progress = { (currentIdx + 1) / quiz.preguntas.size.toFloat() }, modifier = Modifier.fillMaxWidth(), color = PinkLsm)
            Spacer(Modifier.height(16.dp)); Text("Pregunta ${currentIdx + 1}", color = Color.Gray); Text(question.texto, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            question.opciones.forEach { opt ->
                OutlinedButton(onClick = { score += 10; if (currentIdx < quiz.preguntas.size - 1) currentIdx++ else finished = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text(opt, fontSize = 18.sp, color = Color.Black) }
            }
        }
    }
}