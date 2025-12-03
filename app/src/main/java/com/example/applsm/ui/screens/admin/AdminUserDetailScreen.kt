package com.example.applsm.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.applsm.data.AdminUserCategoryProgress
import com.example.applsm.data.AdminUserQuizHistory
import com.example.applsm.ui.AppViewModel
import com.example.applsm.ui.UiState
import com.example.applsm.ui.theme.GrayBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(nav: NavController, vm: AppViewModel, userId: Int) {

    LaunchedEffect(userId) {
        vm.loadAdminUserDetail(userId)
    }

    val userDetail = vm.adminUserDetail

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userDetail?.usuario?.nombre ?: "Cargando...", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(GrayBg)
        ) {
            when (val state = vm.uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    if (userDetail != null) {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Tarjeta de resumen
                            item {
                                SummaryCard(userDetail)
                            }
                            
                            // Progreso por categoría
                            item {
                                Text(
                                    "Progreso por Categoría",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(userDetail.progresoCategorias) {
                                CategoryProgressItem(it)
                            }
                            
                            // Historial de quizzes
                            item {
                                Text(
                                    "Historial de Quizzes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            }
                            items(userDetail.historialQuizzes) {
                                QuizHistoryItem(it)
                            }
                        }
                    } else {
                         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No se encontraron detalles para este usuario.")
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun CategoryProgressItem(progress: AdminUserCategoryProgress) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(progress.categoriaNombre, fontWeight = FontWeight.Bold)
                if (progress.estaCompletado) {
                    Text("✓ Completado", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (progress.porcentajeCompletado / 100f).toFloat() }, 
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${progress.porcentajeCompletado.toInt()}% completado • Nivel ${progress.nivel ?: 1} • Pregunta ${progress.indicePregunta ?: 0}/10", 
                style = MaterialTheme.typography.bodySmall, 
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SummaryCard(userDetail: com.example.applsm.data.AdminUserDetail) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumen", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                SummaryItem(
                    label = "Categorías Completadas",
                    value = "${userDetail.resumen.categoriasCompletadas}/${userDetail.resumen.totalCategorias}"
                )
                SummaryItem(
                    label = "Quizzes Realizados",
                    value = userDetail.resumen.quizzesRealizados.toString()
                )
                SummaryItem(
                    label = "Promedio",
                    value = "${userDetail.resumen.promedioPuntaje}%"
                )
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun QuizHistoryItem(history: AdminUserQuizHistory) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(history.titulo ?: "Quiz General", fontWeight = FontWeight.SemiBold)
                Text(history.fechaRealizacion.split("T")[0], style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text("${history.puntaje} pts", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}