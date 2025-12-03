package com.example.applsm.ui

import android.app.Application
import android.util.Log // IMPORTANTE: Necesario para los logs
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.applsm.data.*
import kotlinx.coroutines.launch

sealed class UiState {
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
    object Idle : UiState()
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AppRepository(application)

    var uiState by mutableStateOf<UiState>(UiState.Idle)
    var currentUserType by mutableStateOf<String?>(null)
    var currentUserName by mutableStateOf<String?>(null)

    // Datos Principales
    var categorias by mutableStateOf<List<Categoria>>(emptyList())
    var senas by mutableStateOf<List<Sena>>(emptyList())

    // --- VARIABLES DE ESTADO ---
    var quizDelDia by mutableStateOf<Quiz?>(null)
    var mapaProgreso by mutableStateOf<Map<Int, ProgresoCategoria>>(emptyMap())
    var ultimoProgreso by mutableStateOf<UltimoProgreso?>(null)
    var rachaDias by mutableStateOf(0)
    var estadoProgreso by mutableStateOf<EstadoProgreso?>(null)

    // --- ADMIN STATE ---
    var adminUserStats by mutableStateOf<List<AdminUserStat>>(emptyList())
    var adminUserDetail by mutableStateOf<AdminUserDetail?>(null)

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            repo.userType.collect { type -> currentUserType = type }
        }
        viewModelScope.launch {
            repo.userName.collect { name -> currentUserName = name }
        }
    }

    // --- AUTH ---
    fun login(email: String, pass: String, onSuccess: (userType: String) -> Unit) {
        viewModelScope.launch {
            uiState = UiState.Loading
            try {
                val response = repo.login(email, pass)
                if (response.isSuccessful && response.body()?.token != null) {
                    val body = response.body()!!
                    val userType = body.usuario?.tipoUsuario ?: "normal"
                    repo.saveSession(body.token!!, body.usuario?.nombre ?: "Usuario", userType)
                    uiState = UiState.Success
                    onSuccess(userType)
                } else uiState = UiState.Error("Credenciales incorrectas")
            } catch (e: Exception) { uiState = UiState.Error("Error de red: ${e.message}") }
        }
    }

    fun loginWithGoogle(idToken: String, name: String, email: String, googleId: String, onSuccess: (userType: String) -> Unit) {
        viewModelScope.launch {
            uiState = UiState.Loading
            try {
                val response = repo.googleLogin(idToken, name, email, googleId)
                if (response.isSuccessful && response.body()?.token != null) {
                    val body = response.body()!!
                    val userType = body.usuario?.tipoUsuario ?: "normal"
                    repo.saveSession(body.token!!, body.usuario?.nombre ?: name, userType)
                    uiState = UiState.Success
                    onSuccess(userType)
                } else uiState = UiState.Error("Error Google Login")
            } catch (e: Exception) { uiState = UiState.Error("Error de red") }
        }
    }

    fun guestLogin(onSuccess: (userType: String) -> Unit) {
        viewModelScope.launch {
            uiState = UiState.Loading
            try {
                val response = repo.guestLogin()
                if (response.isSuccessful) {
                    repo.saveSession(response.body()!!.token!!, "Invitado", "invitado")
                    onSuccess("invitado")
                }
            } catch (e: Exception) { uiState = UiState.Error("Error invitado") }
        }
    }

    fun register(nombre: String, email: String, pass: String, onSuccess: (userType: String) -> Unit) {
        viewModelScope.launch {
            uiState = UiState.Loading
            try {
                val response = repo.register(nombre, email, pass)
                if (response.isSuccessful && response.body()?.token != null) {
                    val body = response.body()!!
                    val userType = body.usuario?.tipoUsuario ?: "normal"
                    repo.saveSession(body.token!!, body.usuario?.nombre ?: nombre, userType)
                    uiState = UiState.Success
                    onSuccess(userType)
                } else uiState = UiState.Error("Error registro")
            } catch (e: Exception) { uiState = UiState.Error("Error red") }
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            repo.clearSession()
            onLogout()
        }
    }

    // --- CARGA DE DATOS HOME ---
    fun cargarHome() {
        viewModelScope.launch {
            Log.d("DEBUG_APP", "cargarHome: Iniciando actualización de datos...")
            try {
                // 1. Categorías
                val catRes = repo.getCategorias()
                if (catRes.isSuccessful) {
                    categorias = catRes.body() ?: emptyList()
                    Log.d("DEBUG_APP", "Categorías cargadas: ${categorias.size}")
                } else {
                    Log.e("DEBUG_APP", "Error al cargar categorías: ${catRes.code()}")
                }

                if (currentUserType != "invitado") {
                    // 2. Mapa de Progreso
                    val mapaRes = repo.getProgresoMapa()
                    if (mapaRes != null && mapaRes.isSuccessful) {
                        mapaProgreso = mapaRes.body()?.associateBy { it.categoriaId } ?: emptyMap()
                        Log.d("DEBUG_APP", "Mapa Progreso cargado. Elementos: ${mapaProgreso.size}")
                    } else {
                        Log.e("DEBUG_APP", "Fallo al cargar Mapa Progreso. Code: ${mapaRes?.code()}")
                    }

                    // 3. Último Progreso
                    val progRes = repo.getProgresoActual()
                    if (progRes != null && progRes.isSuccessful) {
                        ultimoProgreso = progRes.body()
                        Log.d("DEBUG_APP", "Último Progreso: Cat ${ultimoProgreso?.categoriaId} Nivel ${ultimoProgreso?.nivel}")

                        if (ultimoProgreso != null) {
                            estadoProgreso = EstadoProgreso(
                                categoriaId = ultimoProgreso!!.categoriaId,
                                nivel = ultimoProgreso!!.nivel,
                                indice = (ultimoProgreso!!.progresoPercent * 10).toInt(),
                                categoriaNombre = ultimoProgreso!!.categoriaNombre
                            )
                        }
                    }

                    // 4. Racha
                    val rachaRes = repo.getRacha()
                    if (rachaRes != null && rachaRes.isSuccessful) {
                        rachaDias = rachaRes.body()?.dias ?: 0
                    }
                }
            } catch (e: Exception) {
                Log.e("DEBUG_APP", "Excepción crítica en cargarHome: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun buscarSenas(query: String = "", catId: Int? = null) {
        viewModelScope.launch {
            try {
                val res = repo.getSenas(catId, if(query.isNotEmpty()) query else null)
                if (res.isSuccessful) senas = res.body() ?: emptyList()
            } catch (e: Exception) { }
        }
    }

    // --- LÓGICA DE QUIZ CON LOGS ---

    fun cargarQuiz(catId: Int = -1, nivel: Int = 1) {
        viewModelScope.launch {
            uiState = UiState.Loading
            // LOG: Ver qué estamos pidiendo
            Log.d("DEBUG_APP", ">>> Solicitando Quiz: CatID=$catId, Nivel=$nivel")

            try {
                val realCatId = if (catId == -1) null else catId
                val res = repo.generarQuiz(realCatId, nivel)

                if (res != null && res.isSuccessful) {
                    quizDelDia = res.body()
                    val preguntasCount = quizDelDia?.preguntas?.size ?: 0

                    // LOG: Ver qué recibimos
                    Log.d("DEBUG_APP", "<<< Quiz Recibido EXITOSAMENTE. Preguntas: $preguntasCount")
                    if (preguntasCount == 0) {
                        Log.w("DEBUG_APP", "ALERTA: El quiz llegó vacío (0 preguntas).")
                    }

                    uiState = UiState.Success
                } else {
                    // LOG: Ver error del servidor
                    val errorBody = res?.errorBody()?.string() ?: "Sin cuerpo de error"
                    Log.e("DEBUG_APP", "<<< Error del Servidor: Código ${res?.code()}. Mensaje: $errorBody")

                    uiState = UiState.Error("No se pudo generar el quiz. Código: ${res?.code()}")
                }
            } catch (e: Exception) {
                // LOG: Ver error de conexión/app
                Log.e("DEBUG_APP", "!!! Excepción (Crash/Red) al cargar quiz: ${e.message}")
                e.printStackTrace()
                uiState = UiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // Guardar avance
    fun guardarAvance(catId: Int, nivel: Int, indicePregunta: Int) {
        if (currentUserType == "invitado" || catId == -1) return

        viewModelScope.launch {
            try {
                Log.d("DEBUG_APP", "Guardando avance... Cat:$catId Nivel:$nivel Indice:$indicePregunta")
                repo.guardarProgreso(catId, nivel, indicePregunta)

                estadoProgreso = EstadoProgreso(catId, nivel, indicePregunta)
                
                // ACTUALIZACIÓN LOCAL OPTIMISTA DEL MAPA
                // Esto permite que el mapa se actualice sin esperar a recargar todo desde el servidor
                val currentProg = mapaProgreso[catId]
                val isCompleted = indicePregunta >= 10 // Asumimos 10 por el código del Quiz
                
                val newProg = currentProg?.copy(
                    preguntasCompletadas = indicePregunta,
                    completado = isCompleted,
                    bloqueado = false // Si avanzamos, no está bloqueado
                ) ?: ProgresoCategoria(
                    categoriaId = catId,
                    nivelActual = nivel,
                    preguntasCompletadas = indicePregunta,
                    totalPreguntas = 10,
                    completado = isCompleted,
                    bloqueado = false
                )
                
                // Forzamos la actualización del estado
                mapaProgreso = mapaProgreso.toMutableMap().apply { put(catId, newProg) }

                if (indicePregunta >= 9) {
                    Log.d("DEBUG_APP", "Nivel completado, recargando home...")
                    cargarHome()
                }
            } catch (e: Exception) {
                Log.e("DEBUG_APP", "Error guardando progreso: ${e.message}")
            }
        }
    }

    fun obtenerSenaPorId(id: Int): Sena? {
        return senas.find { it.id == id }
    }

    // --- FUNCIONES DE ADMIN ---
    fun loadAdminUserStats() {
        viewModelScope.launch {
            uiState = UiState.Loading
            try {
                val response = repo.getAdminUserStats()
                if (response != null && response.isSuccessful) {
                    adminUserStats = response.body() ?: emptyList()
                    uiState = UiState.Success
                } else {
                    uiState = UiState.Error("Error al cargar estadísticas de usuarios: ${response?.code()}")
                }
            } catch (e: Exception) {
                uiState = UiState.Error("Error de red: ${e.message}")
            }
        }
    }

    fun loadAdminUserDetail(userId: Int) {
        viewModelScope.launch {
            uiState = UiState.Loading
            adminUserDetail = null // Limpiar detalle anterior
            try {
                val response = repo.getAdminUserProgressDetail(userId)
                if (response != null && response.isSuccessful) {
                    adminUserDetail = response.body()
                    uiState = UiState.Success
                } else {
                    uiState = UiState.Error("Error al cargar detalles del usuario: ${response?.code()}")
                }
            } catch (e: Exception) {
                uiState = UiState.Error("Error de red: ${e.message}")
            }
        }
    }
}