package com.example.applsm.ui

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.applsm.data.Categoria
import com.example.applsm.data.Sena
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// --- COLORES ---
val CyanLsm = Color(0xFF78d5fb)
val PinkLsm = Color(0xFFFFB6C1)
val GrayBg = Color(0xFFF5F5F5)
val LockedGray = Color(0xFFE0E0E0)
val GoldStar = Color(0xFFFFD700)
val FireOrange = Color(0xFFFF5722)

// --- CONFIGURACIÓN ---
const val BASE_URL_FILES = "http://10.0.2.2:3000"
// REEMPLAZA CON TU CLIENT ID REAL
const val GOOGLE_WEB_CLIENT_ID = "796116611594-5m3l77aigcg2847s9nb9npmn79b358e3.apps.googleusercontent.com"

fun fixUrl(url: String?): String? {
    if (url.isNullOrEmpty()) return null
    return if (url.startsWith("http")) url else "$BASE_URL_FILES$url"
}

// --- NAVEGACIÓN PRINCIPAL ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, viewModel) }
        composable("register") { RegisterScreen(navController, viewModel) }
        composable("main") { MainScreen(navController, viewModel) }

        composable("dictionary_list/{catId}/{catName}") { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("catId")?.toIntOrNull()
            val catName = backStackEntry.arguments?.getString("catName") ?: "Diccionario"
            DictionaryListScreen(navController, viewModel, catId, catName)
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
                    Text("Cargando seña...")
                    Button(onClick = { navController.popBackStack() }) { Text("Volver") }
                }
            }
        }

        composable(
            "quiz?catId={catId}&level={level}&resume={resume}",
            arguments = listOf(
                navArgument("catId") { type = NavType.IntType; defaultValue = -1 },
                navArgument("level") { type = NavType.IntType; defaultValue = 1 },
                navArgument("resume") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val catId = backStackEntry.arguments?.getInt("catId") ?: -1
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            val resume = backStackEntry.arguments?.getBoolean("resume") ?: false

            QuizScreen(navController, viewModel, catId, level, resume)
        }
    }
}

// --- PANTALLA PRINCIPAL (CONTENEDOR DE TABS) ---

@Composable
fun MainScreen(rootNavController: NavController, viewModel: AppViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = CyanLsm.copy(alpha = 0.3f))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Map, contentDescription = "Ruta") },
                    label = { Text("Aprender") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = CyanLsm.copy(alpha = 0.3f))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Diccionario") },
                    label = { Text("Glosario") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = CyanLsm.copy(alpha = 0.3f))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Config") },
                    label = { Text("Perfil") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = PinkLsm.copy(alpha = 0.3f))
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeDashboardTab(rootNavController, viewModel) { selectedTab = 1 }
                1 -> LearnMapTab(rootNavController, viewModel)
                2 -> DictionaryTab(rootNavController, viewModel)
                3 -> SettingsTab(rootNavController, viewModel)
            }
        }
    }
}

// --- VISTA 1: DASHBOARD DE INICIO ---
@Composable
fun HomeDashboardTab(nav: NavController, vm: AppViewModel, onGoToMap: () -> Unit) {
    LaunchedEffect(Unit) { vm.cargarHome() }

    val ultimoProgreso = vm.ultimoProgreso
    val racha = vm.rachaDias

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(CyanLsm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = vm.currentUserName?.take(1)?.uppercase() ?: "U",
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Hola, ${vm.currentUserName ?: "Estudiante"}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("¡Aprende algo nuevo hoy!", color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocalFireDepartment, null, tint = FireOrange, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Racha de aprendizaje", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("$racha días seguidos ¡Sigue así!", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (vm.currentUserType != "invitado") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clickable { nav.navigate("quiz?catId=-1&level=1") }, // -1 = Quiz del Día
                colors = CardDefaults.cardColors(containerColor = CyanLsm),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 20.dp, y = 20.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Quiz del Día", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 24.sp)
                            Text("Gana puntos extra hoy", color = Color.White.copy(alpha = 0.9f))
                        }
                        Button(
                            onClick = { nav.navigate("quiz?catId=-1&level=1") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp)
                        ) {
                            Text("Jugar ahora", color = CyanLsm, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Tu Curso", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

        if (ultimoProgreso != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PinkLsm.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📚", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("En progreso: Nivel ${ultimoProgreso.nivel}", color = PinkLsm, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(ultimoProgreso.categoriaNombre ?: "Curso", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { ultimoProgreso.progresoPercent },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = PinkLsm,
                        trackColor = GrayBg
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                nav.navigate("quiz?catId=${ultimoProgreso.categoriaId}&level=${ultimoProgreso.nivel}&resume=true")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PinkLsm)
                        ) {
                            Text("Continuar")
                        }
                        OutlinedButton(
                            onClick = onGoToMap,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ver Mapa", color = Color.Gray)
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Aún no has empezado cursos.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onGoToMap, colors = ButtonDefaults.buttonColors(containerColor = CyanLsm)) {
                        Text("Ir al Mapa de Aprendizaje")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// --- VISTA 2: MAPA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnMapTab(nav: NavController, vm: AppViewModel) {
    LaunchedEffect(Unit) { vm.cargarHome() }

    val progresoMapa = vm.mapaProgreso

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
    ) {
        CenterAlignedTopAppBar(
            title = { Text("Tu Camino", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
        )

        if (vm.categorias.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(vm.categorias) { index, cat ->
                    val infoProgreso = progresoMapa[cat.id]

                    var state = 0 // Bloqueado
                    if (index == 0) state = 1 // Primero desbloqueado

                    if (infoProgreso != null) {
                        if (infoProgreso.completado) state = 3
                        else if (!infoProgreso.bloqueado) state = 2
                    }

                    if (index > 0) {
                        val prevCat = vm.categorias[index - 1]
                        val prevProg = progresoMapa[prevCat.id]
                        if (prevProg?.completado == true) {
                            if (state == 0) state = 1
                        }
                    }

                    PathNodeItem(
                        category = cat,
                        index = index,
                        state = state,
                        isLast = index == vm.categorias.lastIndex,
                        nav = nav
                    )
                }
            }
        }
    }
}

@Composable
fun PathNodeItem(category: Categoria, index: Int, state: Int, isLast: Boolean, nav: NavController) {
    val offsetX = when (index % 4) {
        1 -> 40.dp
        3 -> (-40).dp
        else -> 0.dp
    }

    var showActionDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (!isLast) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val startX = center.x + offsetX.toPx()
                val startY = 40.dp.toPx()
                val nextOffsetX = when ((index + 1) % 4) {
                    1 -> 40.dp.toPx()
                    3 -> (-40).dp.toPx()
                    else -> 0f
                }
                val endX = center.x + nextOffsetX
                val endY = size.height + 20.dp.toPx()

                drawLine(
                    color = if (state >= 2) CyanLsm else LockedGray,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 8.dp.toPx(),
                    pathEffect = PathEffect.cornerPathEffect(20f)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(x = offsetX)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        when (state) {
                            3 -> GoldStar
                            2 -> CyanLsm
                            1 -> Color.White
                            else -> LockedGray
                        }
                    )
                    .border(
                        width = 4.dp,
                        color = if (state == 2) PinkLsm else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable(enabled = state > 0) {
                        if (state == 2) showActionDialog = true
                        else if (state == 1 || state == 3) nav.navigate("quiz?catId=${category.id}&level=1")
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state == 0) Icon(Icons.Default.Lock, null, tint = Color.White)
                else Text(text = category.iconUrl ?: "★", fontSize = 32.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = category.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (state == 0) Color.Gray else Color.Black)
                    if (state == 2) Text("En curso", fontSize = 10.sp, color = CyanLsm, fontWeight = FontWeight.Bold)
                    if (state == 3) Text("Completado", fontSize = 10.sp, color = GoldStar, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showActionDialog) {
        Dialog(onDismissRequest = { showActionDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nivel: ${category.nombre}", fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { showActionDialog = false; nav.navigate("quiz?catId=${category.id}&level=1&resume=true") }, colors = ButtonDefaults.buttonColors(containerColor = CyanLsm), modifier = Modifier.fillMaxWidth()) { Text("Continuar") }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { showActionDialog = false; nav.navigate("quiz?catId=${category.id}&level=1&resume=false") }, modifier = Modifier.fillMaxWidth()) { Text("Empezar de nuevo", color = PinkLsm) }
                }
            }
        }
    }
}

// --- VISTA 3: DICCIONARIO ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryTab(nav: NavController, vm: AppViewModel) {
    LaunchedEffect(Unit) { vm.cargarHome() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CenterAlignedTopAppBar(
            title = { Text("Diccionario Completo", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
        )
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Button(
                onClick = { nav.navigate("dictionary_list/-1/Búsqueda Global") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GrayBg),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buscar palabra...", color = Color.Gray)
                }
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(vm.categorias) { cat ->
                CategoryCard(cat) {
                    nav.navigate("dictionary_list/${cat.id}/${cat.nombre}")
                }
            }
        }
    }
}

@Composable
fun CategoryCard(cat: Categoria, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GrayBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(cat.iconUrl ?: "📁", fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                cat.nombre,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// --- VISTA 4: CONFIGURACIÓN ---
@Composable
fun SettingsTab(nav: NavController, vm: AppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
            .padding(16.dp)
    ) {
        Text("Perfil", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(PinkLsm),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(vm.currentUserName ?: "Invitado", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(if (vm.currentUserType == "invitado") "Invitado" else "Estudiante", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingItem(icon = Icons.Default.Notifications, title = "Recordatorios") { }
        SettingItem(icon = Icons.Default.Language, title = "Idioma") { }
        SettingItem(icon = Icons.Default.Info, title = "Ayuda y Soporte") { }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                vm.logout { nav.navigate("login") { popUpTo("main") { inclusive = true } } }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SettingItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

// --- SUB-PANTALLAS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryListScreen(nav: NavController, vm: AppViewModel, catId: Int?, catName: String) {
    var query by remember { mutableStateOf("") }
    LaunchedEffect(catId) { vm.buscarSenas("", if (catId != -1) catId else null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(catName, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(Color.White)) {

            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    vm.buscarSenas(it, if (catId != -1) catId else null)
                },
                placeholder = { Text("Buscar...") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (vm.uiState is UiState.Loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    items(vm.senas) { sena ->
                        val fullImgUrl = fixUrl(sena.imagenUrl)
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                .clickable { nav.navigate("detail/${sena.id}") },
                            colors = CardDefaults.cardColors(containerColor = GrayBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (!fullImgUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = fullImgUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        Modifier.size(60.dp).background(Color.LightGray, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) { Text("👐", fontSize = 24.sp) }
                                }
                                Spacer(Modifier.width(16.dp))
                                Text(sena.palabra, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
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
                        Text("Sin contenido visual", color = Color.White)
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

// --- QUIZ SCREEN CORREGIDA ---
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun QuizScreen(nav: NavController, vm: AppViewModel, catId: Int = -1, level: Int = 1, resume: Boolean = false) {
    LaunchedEffect(Unit) { vm.cargarQuiz(catId, level) }

    val quiz = vm.quizDelDia
    var currentIdx by remember { mutableStateOf(if (resume) 3 else 0) }
    var score by remember { mutableStateOf(if (resume) 30 else 0) }
    var finished by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // CORRECCIÓN: Actualizar índice SOLO si realmente existe un estado guardado válido
    LaunchedEffect(vm.estadoProgreso) {
        if (resume && vm.estadoProgreso?.indice != null && vm.estadoProgreso?.categoriaId == catId) {
            currentIdx = vm.estadoProgreso!!.indice
        }
    }

    if (vm.uiState is UiState.Loading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
    if (quiz == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Cargando quiz..."); Button(onClick = { nav.popBackStack() }) { Text("Volver") } }; return }

    if (finished) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.size(100.dp), tint = Color(0xFFFFD700))
            Text(if (score > 50) "¡Nivel Completado!" else "¡Sigue practicando!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = CyanLsm)
            Text("Puntuación: $score", fontSize = 20.sp, color = Color.Gray)
            Spacer(Modifier.height(24.dp))

            // BOTÓN SIGUIENTE CATEGORÍA
            if (catId != -1) {
                Button(
                    onClick = {
                        vm.guardarAvance(catId, level, 10) // Marcar como completo
                        nav.navigate("main") { popUpTo("main") { inclusive = true } }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanLsm),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Terminar y Volver") }

                Spacer(Modifier.height(12.dp))

                // Intentar ir a la siguiente categoría
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
        return
    }

    val question = quiz.preguntas.getOrNull(currentIdx)

    if (question != null) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
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

            // AREA MULTIMEDIA
            val qVideoUrl = remember(question.videoUrl) { fixUrl(question.videoUrl) }
            val qImageUrl = remember(question.imagenUrl) { fixUrl(question.imagenUrl) }

            val isVideo = remember(qVideoUrl) {
                !qVideoUrl.isNullOrEmpty() &&
                        !qVideoUrl.endsWith(".jpg", true) &&
                        !qVideoUrl.endsWith(".png", true) &&
                        !qVideoUrl.endsWith(".jpeg", true)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                if (isVideo && qVideoUrl != null) {
                    val exoPlayer = remember(qVideoUrl) {
                        ExoPlayer.Builder(context).build().apply {
                            setMediaItem(MediaItem.fromUri(Uri.parse(qVideoUrl)))
                            prepare()
                            playWhenReady = true
                        }
                    }
                    DisposableEffect(qVideoUrl) { onDispose { exoPlayer.release() } }

                    AndroidView(factory = { PlayerView(context).apply { player = exoPlayer } }, modifier = Modifier.fillMaxSize())
                } else if (!qImageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = qImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        onError = { Log.e("QUIZ_IMG", "Error cargando imagen quiz: ${it.result.throwable.message}") }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Mira la seña", color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(question.texto, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
            Spacer(Modifier.weight(1f))

            question.opciones.forEach { opt ->
                Button(
                    onClick = {
                        score += 10
                        // Avanzar o terminar
                        if (currentIdx < quiz.preguntas.size - 1) {
                            val nextIdx = currentIdx + 1
                            currentIdx = nextIdx
                            vm.guardarAvance(catId, level, nextIdx)
                        } else {
                            finished = true
                            vm.guardarAvance(catId, level, 10) // Marcar como completo
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, GrayBg),
                    elevation = ButtonDefaults.buttonElevation(2.dp)
                ) {
                    Text(opt, fontSize = 18.sp, color = Color.Black)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    } else {
        // Si currentIdx se sale de rango por error de estado
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Finalizando quiz...")
            LaunchedEffect(Unit) { finished = true }
        }
    }
}

// LOGIN Y REGISTER (COPIAR IGUAL)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(nav: NavController, vm: AppViewModel) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val context = LocalContext.current
    val uiState = vm.uiState
    val googleLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result -> val task = GoogleSignIn.getSignedInAccountFromIntent(result.data); try { val account = task.getResult(ApiException::class.java); if (account != null) { vm.loginWithGoogle(idToken = account.idToken ?: "", name = account.displayName ?: "Usuario Google", email = account.email ?: "", googleId = account.id ?: "", onSuccess = { nav.navigate("main") { popUpTo("login") { inclusive = true } } }) } } catch (e: ApiException) { Toast.makeText(context, "Error Google: ${e.statusCode}", Toast.LENGTH_LONG).show() } }
    LaunchedEffect(uiState) { if (uiState is UiState.Error) { Toast.makeText(context, uiState.message, Toast.LENGTH_LONG).show(); vm.uiState = UiState.Idle } }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(CyanLsm), contentAlignment = Alignment.Center) { Text("👋", fontSize = 40.sp) }
        Spacer(Modifier.height(24.dp))
        Text("SignApp LSM", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { vm.login(email, pass) { nav.navigate("main") { popUpTo("login") { inclusive = true } } } }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = CyanLsm)) { if (vm.uiState is UiState.Loading) CircularProgressIndicator(color = Color.White) else Text("Entrar") }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = { val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(GOOGLE_WEB_CLIENT_ID).requestEmail().build(); val googleSignInClient = GoogleSignIn.getClient(context, gso); googleLauncher.launch(googleSignInClient.signInIntent) }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)) { Text("Iniciar con Google") }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { nav.navigate("register") }) { Text("¿No tienes cuenta? Regístrate aquí", color = PinkLsm) }
        TextButton(onClick = { vm.guestLogin { nav.navigate("main") } }) { Text("Entrar como Invitado", color = Color.Gray) }
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
        Button(onClick = { if (nombre.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) vm.register(nombre, email, pass) { nav.navigate("main") { popUpTo("login") { inclusive = true } } } else Toast.makeText(context, "Llena todo", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = PinkLsm)) { if (vm.uiState is UiState.Loading) CircularProgressIndicator(color = Color.White) else Text("Registrarse") }
        TextButton(onClick = { nav.popBackStack() }) { Text("Volver", color = Color.Gray) }
    }
}