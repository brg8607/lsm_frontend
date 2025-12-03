package com.example.applsm.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.applsm.data.Categoria
import com.example.applsm.ui.AppViewModel
import com.example.applsm.ui.theme.GrayBg

@Composable
fun AdminContentScreen(nav: NavController, vm: AppViewModel) {

    LaunchedEffect(Unit) {
        vm.cargarHome()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
    ) {
        if (vm.categorias.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(vm.categorias) { categoria ->
                    CategoryListItem(categoria = categoria, onClick = {
                        nav.navigate("admin_sena_list/${categoria.id}/${categoria.nombre}")
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListItem(categoria: Categoria, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(categoria.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Ver contenido")
        }
    }
}