package com.example.applsm.ui.screens.dictionary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // IMPORTANTE: Soluciona 'Unresolved reference clip'
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.applsm.ui.AppViewModel
import com.example.applsm.ui.UiState
import com.example.applsm.ui.theme.*
import com.example.applsm.ui.utils.hablar
import com.example.applsm.ui.utils.rememberTextToSpeech

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryListScreen(nav: NavController, vm: AppViewModel, catId: Int?, catName: String) {
    var query by remember { mutableStateOf("") }
    val tts = rememberTextToSpeech()
    
    LaunchedEffect(catId) { vm.buscarSenas("", if (catId != -1) catId else null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(catName, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(Color.White)) {

            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    vm.buscarSenas(it, if (catId != -1) catId else null)
                },
                placeholder = { Text("Buscar...") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (vm.uiState is UiState.Loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    items(vm.senas) { sena ->
                        val fullImgUrl = fixUrl(sena.imagenUrl)
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                .clickable { nav.navigate("detail/${sena.id}") },
                            colors = CardDefaults.cardColors(containerColor = GrayBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp), 
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!fullImgUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = fullImgUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        Modifier.size(60.dp).background(Color.LightGray, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) { Text("👐", fontSize = 24.sp) }
                                }
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    sena.palabra, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                // Botón de TTS
                                IconButton(
                                    onClick = { tts?.hablar(sena.palabra) },
                                    enabled = tts != null
                                ) {
                                    Icon(
                                        Icons.Default.VolumeUp, 
                                        "Escuchar",
                                        tint = if (tts != null) PinkLsm else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}