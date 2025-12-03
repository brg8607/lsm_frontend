package com.example.applsm.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.applsm.ui.AppViewModel
import com.example.applsm.ui.theme.*

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
                    .clickable { nav.navigate("quiz?catId=-1&level=1") },
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