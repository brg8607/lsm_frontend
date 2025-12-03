package com.example.applsm.ui.screens.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.applsm.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminContainerScreen(rootNavController: NavController, viewModel: AppViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    when(selectedTab) {
                        0 -> "Gestión de Contenido"
                        1 -> "Gestión de Usuarios"
                        2 -> "Gestión de Quizzes"
                        else -> "Panel de Administrador"
                    }, 
                    fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Contenido") },
                    label = { Text("Contenido") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Group, contentDescription = "Usuarios") },
                    label = { Text("Usuarios") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Quiz, contentDescription = "Quizzes") },
                    label = { Text("Quizzes") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                 NavigationBarItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = "Salir") },
                    label = { Text("Salir") },
                    selected = false,
                    onClick = {
                        viewModel.logout {
                            rootNavController.navigate("login") {
                                popUpTo(0) // Limpia toda la pila de navegación
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 || selectedTab == 2) { // Mostrar solo en Contenido y Quizzes
                FloatingActionButton(onClick = {
                    // TODO: Implementar navegación para crear nuevo item
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> AdminContentScreen(rootNavController, viewModel)
                1 -> AdminUsersScreen(rootNavController, viewModel)
                2 -> AdminQuizzesScreen(rootNavController, viewModel)
            }
        }
    }
}