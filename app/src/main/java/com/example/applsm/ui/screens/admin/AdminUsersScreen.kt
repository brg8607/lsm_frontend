package com.example.applsm.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
import com.example.applsm.data.AdminUserStat
import com.example.applsm.ui.AppViewModel
import com.example.applsm.ui.UiState
import com.example.applsm.ui.theme.GrayBg

@Composable
fun AdminUsersScreen(nav: NavController, vm: AppViewModel) {

    LaunchedEffect(Unit) {
        vm.loadAdminUserStats()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
    ) {
        when (val state = vm.uiState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(vm.adminUserStats) { user ->
                        UserStatListItem(user = user, onClick = {
                            nav.navigate("admin_user_detail/${user.id}")
                        })
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserStatListItem(user: AdminUserStat, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(user.correo ?: "Sin correo", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { user.progresoPromedio.toFloat() / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = "Ver detalles")
        }
    }
}