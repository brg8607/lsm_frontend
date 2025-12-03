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
    var currentUserToken by mutableStateOf<String?>(null)

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
    var adminMetrics by mutableStateOf<AdminMetrics?>(null)

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            repo.userToken.collect { token -> currentUserToken = token }
        }
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
    var puntos by mutableStateOf(0)
        private set

    var estadoQuizDiario by mutableStateOf<EstadoQuizDiarioResponse?>(null)
        private set

    fun cargarHome() {
        if (currentUserType == "invitado") {
            Log.d("DEBUG_APP", "cargarHome: Usuario invitado, se omite carga de datos")
            return
        }

        viewModelScope.launch {
            Log.d("DEBUG_APP", "cargarHome: Iniciando actualización de datos...")
            Log.d("DEBUG_APP", "cargarHome: UserType=$currentUserType, Token=${currentUserToken?.take(20)}...")
            
            try {
                // 1. Categorías
                val catRes = repo.getCategorias()
                if (catRes != null && catRes.isSuccessful) {
                    categorias = catRes.body() ?: emptyList()
                    Log.d("DEBUG_APP", "Categorías cargadas: ${categorias.size}")
                } else {
                    Log.e("DEBUG_APP", "Error al cargar categorías: ${catRes?.code()} - ${catRes?.errorBody()?.string()}")
                }

                // 2. Mapa de Progreso
                val mapaRes = repo.getProgresoMapa()
                if (mapaRes != null && mapaRes.isSuccessful) {
                    val listaProgreso = mapaRes.body() ?: emptyList()
                    Log.d("DEBUG_APP", "Respuesta Mapa Raw (${listaProgreso.size} categorías):")
                    listaProgreso.forEach { 
                        Log.d("DEBUG_APP", " -> Cat ${it.categoriaId}: Nivel=${it.nivelActual}, Preguntas=${it.preguntasCompletadas}/${it.totalPreguntas}, Completado=${it.completado}, Bloqueado=${it.bloqueado}") 
                    }
                    
                    mapaProgreso = listaProgreso.associateBy { it.categoriaId }
                    Log.d("DEBUG_APP", "Mapa Progreso construido con ${mapaProgreso.size} categorías")
                } else {
                    Log.e("DEBUG_APP", "Fallo al cargar Mapa Progreso. Code: ${mapaRes?.code()} - ${mapaRes?.errorBody()?.string()}")
                }

                // 3. Último Progreso
                val ultimoRes = repo.getProgresoActual()
                if (ultimoRes != null && ultimoRes.isSuccessful) {
                    ultimoProgreso = ultimoRes.body()
                    Log.d("DEBUG_APP", "Último Progreso: Cat ${ultimoProgreso?.categoriaId} Nivel ${ultimoProgreso?.nivel}")
                } else {
                    Log.e("DEBUG_APP", "Error al cargar último progreso: ${ultimoRes?.code()}")
                }

                // 4. Racha - Primero registrar la sesión, luego obtener la racha
                try {
                    val sesionRes = repo.registrarSesion()
                    Log.d("DEBUG_APP", "Sesión registrada: ${sesionRes?.code()}")
                } catch (e: Exception) {
                    Log.e("DEBUG_APP", "Error al registrar sesión: ${e.message}")
                }
                
                val rachaRes = repo.getRachaActual()
                if (rachaRes != null && rachaRes.isSuccessful) {
                    rachaDias = rachaRes.body()?.rachaActual ?: 0
                    Log.d("DEBUG_APP", "Racha cargada: $rachaDias días")
                } else {
                    Log.e("DEBUG_APP", "Error al cargar racha: ${rachaRes?.code()}")
                }
                
                // 5. Puntos
                cargarPuntos()

                // 6. Estado Quiz Diario
                cargarEstadoQuizDiario()

            } catch (e: Exception) {
                Log.e("DEBUG_APP", "Excepción crítica en cargarHome: ${e.message}", e)
            }
        }
    }
    
    fun cargarPuntos() {
        viewModelScope.launch {
            try {
                val res = repo.getPuntos()
                if (res != null && res.isSuccessful) {
                    puntos = res.body()?.puntos ?: 0
                    Log.d("DEBUG_APP", "Puntos cargados: $puntos")
                } else {
                    Log.e("DEBUG_APP", "Error al cargar puntos: ${res?.code()}")
                    // Si falla (ej: columna no existe), usar 0 y no crashear
                    puntos = 0
                }
            } catch (e: Exception) {
                Log.e("DEBUG_APP", "Error en cargarPuntos: ${e.message}", e)
                puntos = 0
            }
        }
    }

    fun sumarPuntos(cantidad: Int) {
        if (currentUserType == "invitado") return
        viewModelScope.launch {
            try {
                val res = repo.sumarPuntos(cantidad)
                if (res != null && res.isSuccessful) {
                    puntos = res.body()?.totalPuntos ?: (puntos + cantidad)
                    Log.d("DEBUG_APP", "Puntos sumados: $cantidad. Total: $puntos")
                } else {
                    Log.e("DEBUG_APP", "Error al sumar puntos: ${res?.code()}")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_APP", "Error en sumarPuntos: ${e.message}", e)
            }
        }
    }

    fun cargarEstadoQuizDiario() {
        viewModelScope.launch {
            try {
                val res = repo.getEstadoQuizDiario()
                if (res != null && res.isSuccessful) {
                    estadoQuizDiario = res.body()
                    Log.d("DEBUG_APP", "Estado Quiz Diario: ${estadoQuizDiario?.completado}")
                } else {
                    Log.e("DEBUG_APP", "Error al cargar estado quiz diario: ${res?.code()}")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_APP", "Error en cargarEstadoQuizDiario: ${e.message}", e)
            }
        }
    }

    fun completarQuizDiario(puntuacion: Int) {
        if (currentUserType == "invitado") return
        viewModelScope.launch {
            val res = repo.completarQuizDiario(puntuacion)
            if (res != null && res.isSuccessful) {
                cargarEstadoQuizDiario() // Recargar el estado
                Log.d("DEBUG_APP", "Quiz diario completado con puntuación: $puntuacion")
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
        if (currentUserType == "invitado" || catId == -1) {
            Log.d("DEBUG_APP", "No se guarda progreso - Usuario: $currentUserType, CatId: $catId")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("DEBUG_APP", "ViewModel - Guardando avance... Cat:$catId Nivel:$nivel Indice:$indicePregunta")
                val response = repo.guardarProgreso(catId, nivel, indicePregunta)
                if (response?.isSuccessful == true) {
                    Log.d("DEBUG_APP", "ViewModel - Progreso guardado exitosamente")
                } else {
                    Log.e("DEBUG_APP", "ViewModel - Error al guardar progreso: ${response?.code()}")
                }

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

    fun loadAdminMetrics() {
        viewModelScope.launch {
            try {
                val response = repo.getAdminMetrics()
                if (response != null && response.isSuccessful) {
                    adminMetrics = response.body()
                    Log.d("DEBUG_APP", "Métricas cargadas: ${adminMetrics}")
                } else {
                    Log.e("DEBUG_APP", "Error al cargar métricas: ${response?.code()}")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_APP", "Error de red al cargar métricas: ${e.message}")
            }
        }
    }

    // --- FUNCIONES DE ADMIN CATEGORIAS ---
    suspend fun crearCategoria(nombre: String, iconUrl: String?, descripcion: String?) {
        try {
            val response = repo.crearCategoria(nombre, iconUrl, descripcion)
            if (response != null && response.isSuccessful) {
                Log.d("DEBUG_APP", "Categoría creada exitosamente")
            } else {
                Log.e("DEBUG_APP", "Error al crear categoría: ${response?.code()}")
            }
        } catch (e: Exception) {
            Log.e("DEBUG_APP", "Error de red al crear categoría: ${e.message}")
        }
    }

    suspend fun editarCategoria(id: Int, nombre: String, iconUrl: String?, descripcion: String?) {
        try {
            val response = repo.editarCategoria(id, nombre, iconUrl, descripcion)
            if (response != null && response.isSuccessful) {
                Log.d("DEBUG_APP", "Categoría editada exitosamente")
            } else {
                Log.e("DEBUG_APP", "Error al editar categoría: ${response?.code()}")
            }
        } catch (e: Exception) {
            Log.e("DEBUG_APP", "Error de red al editar categoría: ${e.message}")
        }
    }

    suspend fun eliminarCategoria(id: Int) {
        try {
            val response = repo.eliminarCategoria(id)
            if (response != null && response.isSuccessful) {
                Log.d("DEBUG_APP", "Categoría eliminada exitosamente")
            } else {
                val errorBody = response?.errorBody()?.string()
                Log.e("DEBUG_APP", "Error al eliminar categoría: ${response?.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.e("DEBUG_APP", "Error de red al eliminar categoría: ${e.message}")
        }
    }

    // --- FUNCIONES DE ADMIN SEÑAS ---

    suspend fun crearSena(palabra: String, categoriaId: Int, descripcion: String?): Boolean {
        return try {
            val response = repo.crearSena(palabra, categoriaId, descripcion)
            if (response != null && response.isSuccessful) {
                Log.d("DEBUG_APP", "Seña creada exitosamente: ${response.body()}")
                true
            } else {
                Log.e("DEBUG_APP", "Error al crear seña: ${response?.code()} - ${response?.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e("DEBUG_APP", "Error de red al crear seña: ${e.message}", e)
            false
        }
    }

    suspend fun editarSena(id: Int, palabra: String, categoriaId: Int, descripcion: String?): Boolean {
        return try {
            val response = repo.editarSena(id, palabra, categoriaId, descripcion)
            if (response != null && response.isSuccessful) {
                Log.d("DEBUG_APP", "Seña editada exitosamente")
                true
            } else {
                Log.e("DEBUG_APP", "Error al editar seña: ${response?.code()} - ${response?.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e("DEBUG_APP", "Error de red al editar seña: ${e.message}", e)
            false
        }
    }

    suspend fun eliminarSena(id: Int): Boolean {
        return try {
            val response = repo.eliminarSena(id)
            if (response != null && response.isSuccessful) {
                Log.d("DEBUG_APP", "Seña eliminada exitosamente")
                true
            } else {
                Log.e("DEBUG_APP", "Error al eliminar seña: ${response?.code()} - ${response?.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e("DEBUG_APP", "Error de red al eliminar seña: ${e.message}", e)
            false
        }
    }

    // --- ADMIN QUIZZES ---

    suspend fun cargarQuizzes(): List<com.example.applsm.ui.screens.admin.AdminQuiz> {
        return try {
            val response = repo.listarQuizzes()
            if (response != null && response.isSuccessful) {
                val rawList = response.body() ?: emptyList()
                rawList.map { quiz ->
                    com.example.applsm.ui.screens.admin.AdminQuiz(
                        id = (quiz["id"] as? Double)?.toInt() ?: 0,
                        titulo = quiz["titulo"] as? String ?: "",
                        fecha_programada = quiz["fecha_programada"] as? String,
                        creado_en = quiz["creado_en"] as? String,
                        total_preguntas = (quiz["total_preguntas"] as? Double)?.toInt() ?: 0
                    )
                }
            } else {
                Log.e("DEBUG_APP", "Error al cargar quizzes: ${response?.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("DEBUG_APP", "Error de red al cargar quizzes: ${e.message}")
            emptyList()
        }
    }

    suspend fun crearQuiz(titulo: String, fecha: String): Boolean {
        return try {
            // Crear un quiz vacío (sin preguntas por ahora)
            val response = repo.crearQuiz(titulo, fecha, emptyList())
            if (response != null && response.isSuccessful) {
                Log.d("DEBUG_APP", "Quiz creado exitosamente")
                true
            } else {
                Log.e("DEBUG_APP", "Error al crear quiz: ${response?.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("DEBUG_APP", "Error de red al crear quiz: ${e.message}")
            false
        }
    }

    suspend fun eliminarQuiz(id: Int): Boolean {
        return try {
            val response = repo.eliminarQuiz(id)
            if (response != null && response.isSuccessful) {
                Log.d("DEBUG_APP", "Quiz eliminado exitosamente")
                true
            } else {
                Log.e("DEBUG_APP", "Error al eliminar quiz: ${response?.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("DEBUG_APP", "Error de red al eliminar quiz: ${e.message}")
            false
        }
    }
}