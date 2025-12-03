package com.example.applsm.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.applsm.ui.AppViewModel
import com.example.applsm.ui.UiState
import com.example.applsm.ui.theme.GrayBg
import kotlinx.coroutines.launch

data class AdminQuiz(
    val id: Int,
    val titulo: String,
    val fecha_programada: String?,
    val creado_en: String?,
    val total_preguntas: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuizzesScreen(nav: NavController, vm: AppViewModel) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedQuiz by remember { mutableStateOf<AdminQuiz?>(null) }
    var quizzes by remember { mutableStateOf<List<AdminQuiz>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            quizzes = vm.cargarQuizzes()
            isLoading = false
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Quiz")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize
                .padding(paddingValues)
                .background(GrayBg)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (quizzes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No hay quizzes creados", color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Presiona el botón + para crear uno", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(quizzes) { quiz ->
                        QuizListItem(
                            quiz = quiz,
                            onDelete = {
                                selectedQuiz = quiz
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo para crear quiz
    if (showCreateDialog) {
        CreateQuizDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { titulo, fecha ->
                scope.launch {
                    val success = vm.crearQuiz(titulo, fecha)
                    if (success) {
                        showCreateDialog = false
                        quizzes = vm.cargarQuizzes()
                    }
                }
            }
        )
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog && selectedQuiz != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Quiz") },
            text = { Text("¿Estás seguro de eliminar el quiz '${selectedQuiz?.titulo}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val success = vm.eliminarQuiz(selectedQuiz!!.id)
                            if (success) {
                                showDeleteDialog = false
                                quizzes = vm.cargarQuizzes()
                            }
                        }
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun QuizListItem(quiz: AdminQuiz, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(quiz.titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${quiz.total_preguntas} preguntas",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                quiz.fecha_programada?.let {
                    Text(
                        "Programado: ${it.split('T')[0]}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuizDialog(
    onDismiss: () -> Unit,
    onConfirm: (titulo: String, fecha: String) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Quiz") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título del Quiz *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("2025-12-31") }
                )
                Text(
                    "Nota: El quiz se creará vacío. Deberás agregar preguntas manualmente en la base de datos.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (titulo.isNotBlank() && fecha.isNotBlank()) {
                        onConfirm(titulo, fecha)
                    }
                },
                enabled = titulo.isNotBlank() && fecha.isNotBlank()
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}