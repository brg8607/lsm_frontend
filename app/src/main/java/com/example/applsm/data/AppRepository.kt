package com.example.applsm.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class AppRepository(private val context: Context) {

    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val USER_TYPE_KEY = stringPreferencesKey("user_type")

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

    suspend fun login(correo: String, pass: String) = RetrofitClient.api.login(
        mapOf("correo" to correo, "password" to pass)
    )

    suspend fun register(nombre: String, correo: String, pass: String) = RetrofitClient.api.register(
        mapOf("nombre" to nombre, "correo" to correo, "password" to pass)
    )

    suspend fun googleLogin(idToken: String, name: String, email: String, googleId: String) =
        RetrofitClient.api.googleLogin(
            mapOf(
                "token_google" to idToken,
                "nombre" to name,
                "correo" to email,
                "google_uid" to googleId
            )
        )

    suspend fun guestLogin() = RetrofitClient.api.guestLogin()

    suspend fun getCategorias() = RetrofitClient.api.getCategorias()

    suspend fun getSenas(catId: Int? = null, query: String? = null) =
        RetrofitClient.api.getSenas(catId, query)

    // --- NUEVAS FUNCIONES DE CURSO Y QUIZ ---

    suspend fun generarQuiz(catId: Int? = null, nivel: Int = 1) = getToken()?.let { token ->
        RetrofitClient.api.generarQuizDinamico("Bearer $token", catId, nivel)
    }

    suspend fun getRacha() = getToken()?.let { token ->
        RetrofitClient.api.getRacha("Bearer $token")
    }

    suspend fun guardarProgreso(catId: Int, nivel: Int, indice: Int) = getToken()?.let { token ->
        val request = ProgresoRequest(catId, nivel, indice)
        RetrofitClient.api.guardarProgreso("Bearer $token", request)
    }

    suspend fun getProgresoActual() = getToken()?.let { token ->
        RetrofitClient.api.getProgresoActual("Bearer $token")
    }

    suspend fun getProgresoMapa() = getToken()?.let { token ->
        RetrofitClient.api.getProgresoMapa("Bearer $token")
    }

    suspend fun getQuizDia() = getToken()?.let {
        RetrofitClient.api.getQuizDelDia("Bearer $it")
    }

    suspend fun enviarResultadoQuiz(puntaje: Int, quizId: Int) = getToken()?.let {
        val request = ResultadoQuizRequest(quizId, puntaje)
        RetrofitClient.api.enviarResultado("Bearer $it", request)
    }

    suspend fun getProgreso() = getToken()?.let {
        RetrofitClient.api.getProgreso("Bearer $it")
    }

    // --- FUNCIONES DE ADMIN ---

    suspend fun getAdminUserStats() = getToken()?.let { token ->
        RetrofitClient.api.getAdminUserStats("Bearer $token")
    }

    suspend fun getAdminUserProgressDetail(userId: Int) = getToken()?.let { token ->
        RetrofitClient.api.getAdminUserProgressDetail("Bearer $token", userId)
    }
}