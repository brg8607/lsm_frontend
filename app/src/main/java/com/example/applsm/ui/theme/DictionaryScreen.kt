package com.example.applsm.ui.screens.dictionary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.applsm.data.Categoria
import com.example.applsm.ui.AppViewModel
import com.example.applsm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryTab(nav: NavController, vm: AppViewModel) {
    LaunchedEffect(Unit) { vm.cargarHome() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CenterAlignedTopAppBar(
            title = { Text("Diccionario Completo", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
        )
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Button(
                onClick = { nav.navigate("dictionary_list/-1/Búsqueda Global") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GrayBg),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buscar palabra...", color = Color.Gray)
                }
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(vm.categorias) { cat ->
                CategoryCard(cat) {
                    nav.navigate("dictionary_list/${cat.id}/${cat.nombre}")
                }
            }
        }
    }
}

@Composable
fun CategoryCard(cat: Categoria, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GrayBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(cat.iconUrl ?: "📁", fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                cat.nombre,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ¡NOTA: He eliminado DictionaryListScreen de aquí porque ya está en su propio archivo!