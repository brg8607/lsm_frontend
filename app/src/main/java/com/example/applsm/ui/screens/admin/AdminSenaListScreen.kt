package com.example.applsm.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.applsm.data.Sena
import com.example.applsm.ui.AppViewModel
import com.example.applsm.ui.theme.GrayBg
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSenaListScreen(nav: NavController, vm: AppViewModel, categoryId: Int, categoryName: String) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedSena by remember { mutableStateOf<Sena?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(categoryId) {
        vm.buscarSenas(catId = categoryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Seña")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(GrayBg)
        ) {
            if (vm.senas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay señas en esta categoría.")
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(vm.senas) { sena ->
                        SenaListItem(
                            sena = sena,
                            onEdit = {
                                selectedSena = sena
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedSena = sena
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo para crear seña
    if (showCreateDialog) {
        SenaDialog(
            title = "Crear Seña",
            sena = null,
            onDismiss = { showCreateDialog = false },
            onConfirm = { palabra, descripcion ->
                scope.launch {
                    vm.crearSena(
                        palabra = palabra,
                        categoriaId = categoryId,
                        descripcion = descripcion
                    )
                    showCreateDialog = false
                    vm.buscarSenas(catId = categoryId)
                }
            }
        )
    }

    // Diálogo para editar seña
    if (showEditDialog && selectedSena != null) {
        SenaDialog(
            title = "Editar Seña",
            sena = selectedSena,
            onDismiss = { showEditDialog = false },
            onConfirm = { palabra, descripcion ->
                scope.launch {
                    vm.editarSena(
                        selectedSena!!.id,
                        palabra,
                        categoryId,
                        descripcion
                    )
                    showEditDialog = false
                    vm.buscarSenas(catId = categoryId)
                }
            }
        )
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog && selectedSena != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Seña") },
            text = { Text("¿Estás seguro de eliminar la seña '${selectedSena?.palabra}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            vm.eliminarSena(selectedSena!!.id)
                            showDeleteDialog = false
                            vm.buscarSenas(catId = categoryId)
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
fun SenaListItem(sena: Sena, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).background(Color.LightGray)) // Placeholder para video/imagen
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sena.palabra, fontWeight = FontWeight.Bold)
                Text("ID: ${sena.id}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar Seña", tint = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar Seña", tint = Color.Red)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenaDialog(
    title: String,
    sena: Sena?,
    onDismiss: () -> Unit,
    onConfirm: (palabra: String, descripcion: String?) -> Unit
) {
    var palabra by remember { mutableStateOf(sena?.palabra ?: "") }
    var descripcion by remember { mutableStateOf(sena?.descripcion ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = palabra,
                    onValueChange = { palabra = it },
                    label = { Text("Palabra *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (palabra.isNotBlank()) {
                        onConfirm(
                            palabra,
                            descripcion.takeIf { it.isNotBlank() }
                        )
                    }
                },
                enabled = palabra.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
