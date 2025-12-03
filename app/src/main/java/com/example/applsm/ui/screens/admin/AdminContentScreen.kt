package com.example.applsm.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.applsm.data.Categoria
import com.example.applsm.ui.AppViewModel
import com.example.applsm.ui.theme.GrayBg
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminContentScreen(nav: NavController, vm: AppViewModel) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCategoria by remember { mutableStateOf<Categoria?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        vm.cargarHome()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Categoría")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(GrayBg)
        ) {
            if (vm.categorias.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(vm.categorias) { categoria ->
                        CategoryListItem(
                            categoria = categoria,
                            onClick = {
                                nav.navigate("admin_sena_list/${categoria.id}/${categoria.nombre}")
                            },
                            onEdit = {
                                selectedCategoria = categoria
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedCategoria = categoria
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo para crear categoría
    if (showCreateDialog) {
        CategoriaDialog(
            title = "Crear Categoría",
            categoria = null,
            onDismiss = { showCreateDialog = false },
            onConfirm = { nombre, iconUrl, descripcion ->
                scope.launch {
                    vm.crearCategoria(nombre, iconUrl, descripcion)
                    showCreateDialog = false
                    vm.cargarHome()
                }
            }
        )
    }

    // Diálogo para editar categoría
    if (showEditDialog && selectedCategoria != null) {
        CategoriaDialog(
            title = "Editar Categoría",
            categoria = selectedCategoria,
            onDismiss = { showEditDialog = false },
            onConfirm = { nombre, iconUrl, descripcion ->
                scope.launch {
                    vm.editarCategoria(selectedCategoria!!.id, nombre, iconUrl, descripcion)
                    showEditDialog = false
                    vm.cargarHome()
                }
            }
        )
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog && selectedCategoria != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Categoría") },
            text = { Text("¿Estás seguro de eliminar la categoría '${selectedCategoria?.nombre}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            vm.eliminarCategoria(selectedCategoria!!.id)
                            showDeleteDialog = false
                            vm.cargarHome()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListItem(
    categoria: Categoria,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
            ) {
                Text(categoria.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                }
                IconButton(onClick = onClick) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Ver contenido")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriaDialog(
    title: String,
    categoria: Categoria?,
    onDismiss: () -> Unit,
    onConfirm: (nombre: String, iconUrl: String?, descripcion: String?) -> Unit
) {
    var nombre by remember { mutableStateOf(categoria?.nombre ?: "") }
    var iconUrl by remember { mutableStateOf(categoria?.iconUrl ?: "") }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = iconUrl,
                    onValueChange = { iconUrl = it },
                    label = { Text("URL del Icono") },
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
                    if (nombre.isNotBlank()) {
                        onConfirm(
                            nombre,
                            iconUrl.takeIf { it.isNotBlank() },
                            descripcion.takeIf { it.isNotBlank() }
                        )
                    }
                },
                enabled = nombre.isNotBlank()
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