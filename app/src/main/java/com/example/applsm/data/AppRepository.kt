package com.example.applsm.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Configuración de DataStore (Base de datos local pequeñita para el token)
private val Context.dataStore by preferencesDataStore("user_prefs")

class AppRepository(private val context: Context) {

    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val USER_TYPE_KEY = stringPreferencesKey("user_type") // 'normal', 'admin', 'invitado'

    // --- GESTIÓN DE TOKEN Y SESIÓN ---

    val userToken: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME_KEY] }
    val userType: Flow<String?> = context.dataStore.data.map { it[USER_TYPE_KEY] }

    suspend fun saveSession(token: String, name: String, type: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_NAME_KEY] = name
            prefs[USER_TYPE_KEY] = type
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun getToken(): String? = userToken.first()

    // --- LLAMADAS A LA API ---

    // Auth
    suspend fun login(correo: String, pass: String) = RetrofitClient.api.login(
        mapOf("correo" to correo, "password" to pass)
    )

    suspend fun register(nombre: String, correo: String, pass: String) = RetrofitClient.api.register(
        mapOf("nombre" to nombre, "correo" to correo, "password" to pass)
    )

    suspend fun guestLogin() = RetrofitClient.api.guestLogin()

    // Contenido
    suspend fun getCategorias() = RetrofitClient.api.getCategorias()

    suspend fun getSenas(catId: Int? = null, query: String? = null) =
        RetrofitClient.api.getSenas(catId, query)

    // Quiz
    suspend fun getQuizDia() = getToken()?.let {
        RetrofitClient.api.getQuizDelDia("Bearer $it")
    }

    suspend fun enviarResultadoQuiz(puntaje: Int, quizId: Int) = getToken()?.let {
        RetrofitClient.api.enviarResultado("Bearer $it", mapOf("quiz_id" to quizId, "puntaje" to puntaje))
    }

    // Progreso
    suspend fun getProgreso() = getToken()?.let {
        RetrofitClient.api.getProgreso("Bearer $it")
    }
}