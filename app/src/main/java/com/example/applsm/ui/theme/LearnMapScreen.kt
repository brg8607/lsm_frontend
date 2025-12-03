package com.example.applsm.ui.screens.learn

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.applsm.data.Categoria
import com.example.applsm.ui.AppViewModel
import com.example.applsm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnMapTab(nav: NavController, vm: AppViewModel) {
    // Recargar datos cada vez que la pantalla se muestra (Resume)
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                vm.cargarHome()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                    
                    // DEBUG LOGS
                    SideEffect {
                        Log.d("DEBUG_MAP", "Cat: ${cat.nombre} (ID: ${cat.id}) | Index: $index | State: $state")
                        Log.d("DEBUG_MAP", "   -> InfoProgreso: $infoProgreso")
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