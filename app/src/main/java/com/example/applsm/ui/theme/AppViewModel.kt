package com.example.applsm.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.applsm.data.*
import kotlinx.coroutines.launch

// Estado global de la UI
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

    // Datos
    var categorias by mutableStateOf<List<Categoria>>(emptyList())
    var senas by mutableStateOf<List<Sena>>(emptyList())
    var quizDelDia by mutableStateOf<Quiz?>(null)
    var progreso by mutableStateOf<List<Progreso>>(emptyList())

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

    // --- ACCIONES ---

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState = UiState.Loading
            try {
                val response = repo.login(email, pass)
                if (response.isSuccessful && response.body()?.token != null) {
                    val body = response.body()!!
                    repo.saveSession(body.token!!, body.usuario?.nombre ?: "Usuario", body.usuario?.tipoUsuario ?: "normal")
                    uiState = UiState.Success
                    onSuccess()
                } else {
                    uiState = UiState.Error("Error: ${response.code()} - Revise sus credenciales")
                }
            } catch (e: Exception) {
                uiState = UiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // NUEVA FUNCIÓN: REGISTRO
    fun register(nombre: String, email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState = UiState.Loading
            try {
                val response = repo.register(nombre, email, pass)
                if (response.isSuccessful && response.body()?.token != null) {
                    val body = response.body()!!
                    // Guardar sesión automáticamente tras registro
                    repo.saveSession(body.token!!, body.usuario?.nombre ?: nombre, body.usuario?.tipoUsuario ?: "normal")
                    uiState = UiState.Success
                    onSuccess()
                } else {
                    uiState = UiState.Error("Error al registrar: ${response.code()} (El correo podría ya existir)")
                }
            } catch (e: Exception) {
                uiState = UiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun guestLogin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState = UiState.Loading
            try {
                val response = repo.guestLogin()
                if (response.isSuccessful) {
                    val body = response.body()!!
                    repo.saveSession(body.token!!, "Invitado", "invitado")
                    onSuccess()
                }
            } catch (e: Exception) {
                uiState = UiState.Error("Error al entrar como invitado")
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            repo.clearSession()
            onLogout()
        }
    }

    fun cargarHome() {
        viewModelScope.launch {
            try {
                // Cargar Categorias
                val catRes = repo.getCategorias()
                if (catRes.isSuccessful) categorias = catRes.body() ?: emptyList()

                // Cargar Progreso (si no es invitado)
                if (currentUserType != "invitado") {
                    val progRes = repo.getProgreso()
                    if (progRes != null && progRes.isSuccessful) {
                        progreso = progRes.body() ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                println("Error cargando home: ${e.message}")
            }
        }
    }

    fun buscarSenas(query: String = "", catId: Int? = null) {
        viewModelScope.launch {
            uiState = UiState.Loading
            try {
                val res = repo.getSenas(catId, if(query.isNotEmpty()) query else null)
                if (res.isSuccessful) {
                    senas = res.body() ?: emptyList()
                    uiState = UiState.Success
                }
            } catch (e: Exception) {
                uiState = UiState.Error("Error buscando señas")
            }
        }
    }

    fun cargarQuiz() {
        viewModelScope.launch {
            uiState = UiState.Loading
            try {
                val res = repo.getQuizDia()
                if (res != null && res.isSuccessful) {
                    quizDelDia = res.body()
                    uiState = UiState.Success
                } else {
                    uiState = UiState.Error("No hay quiz disponible hoy")
                }
            } catch (e: Exception) {
                uiState = UiState.Error("Error cargando quiz")
            }
        }
    }
    fun obtenerSenaPorId(id: Int): Sena? {
        return senas.find { it.id == id }
    }
}