package com.example.applsm.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.applsm.ui.AppViewModel
import com.example.applsm.ui.screens.dictionary.DictionaryTab
import com.example.applsm.ui.screens.home.HomeDashboardTab
import com.example.applsm.ui.screens.learn.LearnMapTab
import com.example.applsm.ui.screens.settings.SettingsTab
import com.example.applsm.ui.theme.CyanLsm
import com.example.applsm.ui.theme.PinkLsm

@Composable
fun MainContainerScreen(rootNavController: NavController, viewModel: AppViewModel) {
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