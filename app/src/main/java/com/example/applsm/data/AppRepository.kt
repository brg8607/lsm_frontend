package com.example.applsm.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.Response

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

    suspend fun getToken(): String? {
        val token = userToken.first()
        android.util.Log.d("DEBUG_APP", "Repository.getToken() = ${token?.take(30)}...")
        return token
    }

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

    // --- FUNCIONES DE CURSO Y QUIZ ---

    suspend fun generarQuiz(catId: Int? = null, nivel: Int = 1) = getToken()?.let { token ->
        RetrofitClient.api.generarQuizDinamico("Bearer $token", catId, nivel)
    }

    suspend fun registrarSesion() = getToken()?.let { token ->
        RetrofitClient.api.registrarSesion("Bearer $token")
    }

    suspend fun getRachaActual() = getToken()?.let { token ->
        RetrofitClient.api.getRachaActual("Bearer $token")
    } ?: run {
        android.util.Log.e("DEBUG_APP", "Repository.getRachaActual() - TOKEN ES NULL")
        null
    }

    suspend fun guardarProgreso(catId: Int, nivel: Int, indice: Int) = getToken()?.let { token ->
        val request = ProgresoRequest(catId, nivel, indice)
        android.util.Log.d("DEBUG_APP", "Repository - Guardando progreso: catId=$catId, nivel=$nivel, indice=$indice")
        val response = RetrofitClient.api.guardarProgreso("Bearer $token", request)
        android.util.Log.d("DEBUG_APP", "Repository - Respuesta guardarProgreso: ${response.code()} - ${response.body()}")
        if (!response.isSuccessful) {
            android.util.Log.e("DEBUG_APP", "Repository - Error guardarProgreso: ${response.errorBody()?.string()}")
        }
        response
    }

    suspend fun getProgresoActual() = getToken()?.let { token ->
        RetrofitClient.api.getProgresoActual("Bearer $token")
    } ?: run {
        android.util.Log.e("DEBUG_APP", "Repository.getProgresoActual() - TOKEN ES NULL")
        null
    }

    suspend fun getProgresoMapa() = getToken()?.let { token ->
        RetrofitClient.api.getProgresoMapa("Bearer $token")
    } ?: run {
        android.util.Log.e("DEBUG_APP", "Repository.getProgresoMapa() - TOKEN ES NULL")
        null
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

    // --- PUNTOS ---

    suspend fun getPuntos(): Response<PuntosResponse>? {
        val token = getToken() ?: return null
        return try {
            RetrofitClient.api.getPuntosActuales("Bearer $token")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun sumarPuntos(puntos: Int): Response<PuntosResponse>? {
        val token = getToken() ?: return null
        return try {
            RetrofitClient.api.sumarPuntos("Bearer $token", SumarPuntosRequest(puntos))
        } catch (e: Exception) {
            null
        }
    }

    // --- QUIZ DIARIO ---

    suspend fun getEstadoQuizDiario(): Response<EstadoQuizDiarioResponse>? {
        val token = getToken() ?: return null
        return try {
            RetrofitClient.api.getEstadoQuizDiario("Bearer $token")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun completarQuizDiario(puntuacion: Int): Response<CompletarQuizDiarioResponse>? {
        val token = getToken() ?: return null
        return try {
            RetrofitClient.api.completarQuizDiario("Bearer $token", CompletarQuizDiarioRequest(puntuacion))
        } catch (e: Exception) {
            null
        }
    }

    // --- FUNCIONES DE ADMIN ---

    suspend fun getAdminUserStats() = getToken()?.let { token ->
        RetrofitClient.api.getAdminUserStats("Bearer $token")
    }

    suspend fun getAdminUserProgressDetail(userId: Int) = getToken()?.let { token ->
        RetrofitClient.api.getAdminUserProgressDetail("Bearer $token", userId)
    }

    suspend fun getAdminMetrics() = getToken()?.let { token ->
        RetrofitClient.api.getAdminMetrics("Bearer $token")
    }

    // --- ADMIN CATEGORIAS ---

    suspend fun crearCategoria(nombre: String, iconUrl: String?, descripcion: String?) = getToken()?.let { token ->
        val data = mapOf(
            "nombre" to nombre,
            "icon_url" to iconUrl,
            "descripcion" to descripcion
        )
        RetrofitClient.api.crearCategoria("Bearer $token", data)
    }

    suspend fun editarCategoria(id: Int, nombre: String, iconUrl: String?, descripcion: String?) = getToken()?.let { token ->
        val data = mapOf(
            "nombre" to nombre,
            "icon_url" to iconUrl,
            "descripcion" to descripcion
        )
        RetrofitClient.api.editarCategoria("Bearer $token", id, data)
    }

    suspend fun eliminarCategoria(id: Int) = getToken()?.let { token ->
        RetrofitClient.api.eliminarCategoria("Bearer $token", id)
    }

    // --- ADMIN SEÑAS ---

    // --- ADMIN SEÑAS ---

    suspend fun crearSena(palabra: String, categoriaId: Int, descripcion: String?) = getToken()?.let { token ->
        val data = CrearSenaRequest(
            palabra = palabra,
            categoriaId = categoriaId,
            descripcion = descripcion
        )
        RetrofitClient.api.crearSena("Bearer $token", data)
    }

    suspend fun editarSena(id: Int, palabra: String, categoriaId: Int, descripcion: String?) = getToken()?.let { token ->
        val data = CrearSenaRequest(
            palabra = palabra,
            categoriaId = categoriaId,
            descripcion = descripcion
        )
        RetrofitClient.api.editarSena("Bearer $token", id, data)
    }

    suspend fun eliminarSena(id: Int) = getToken()?.let { token ->
        RetrofitClient.api.eliminarSena("Bearer $token", id)
    }

    // --- ADMIN QUIZZES ---

    suspend fun listarQuizzes() = getToken()?.let { token ->
        RetrofitClient.api.listarQuizzes("Bearer $token")
    }

    suspend fun crearQuiz(titulo: String, fechaDisponible: String, preguntas: List<Map<String, String>>) = getToken()?.let { token ->
        val data = mapOf(
            "titulo" to titulo,
            "fecha_disponible" to fechaDisponible,
            "preguntas" to preguntas
        )
        RetrofitClient.api.crearQuiz("Bearer $token", data)
    }

    suspend fun eliminarQuiz(id: Int) = getToken()?.let { token ->
        RetrofitClient.api.eliminarQuiz("Bearer $token", id)
    }
}