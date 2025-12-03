package com.example.applsm.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

// --- MODELOS DE DATOS ---

data class User(
    val id: Int,
    val nombre: String,
    val correo: String?,
    @SerializedName("tipo_usuario") val tipoUsuario: String
)

data class AuthResponse(
    val mensaje: String,
    val token: String?,
    val usuario: User?
)

data class Categoria(
    val id: Int,
    val nombre: String,
    @SerializedName("icon_url") val iconUrl: String?
)

data class Sena(
    val id: Int,
    val palabra: String,
    val descripcion: String?,
    @SerializedName("video_url") val videoUrl: String?,
    @SerializedName("imagen_url") val imagenUrl: String?,
    @SerializedName("categoria_nombre") val categoriaNombre: String?
)

data class Quiz(
    val id: Long?,
    val titulo: String,
    val preguntas: List<Pregunta>
)

data class Pregunta(
    val id: Int,
    @SerializedName("pregunta_texto") val texto: String,
    @SerializedName("video_asociado_url") val videoUrl: String?,
    @SerializedName("imagen_asociada_url") val imagenUrl: String?,
    @SerializedName("opciones") val opciones: List<String>,
    @SerializedName("respuesta_correcta") val respuestaCorrecta: String?
)

data class RachaResponse(
    @SerializedName("racha_actual") val rachaActual: Int,
    @SerializedName("ultima_sesion") val ultimaSesion: String,
    @SerializedName("racha_maxima") val rachaMaxima: Int
)

data class RegistrarSesionResponse(
    val mensaje: String,
    val fecha: String
)

data class EstadoProgreso(
    @SerializedName("categoria_id") val categoriaId: Int,
    @SerializedName("nivel_actual") val nivel: Int,
    @SerializedName("indice_pregunta") val indice: Int,
    @SerializedName("categoria_nombre") val categoriaNombre: String? = null
)

data class ProgresoRequest(
    @SerializedName("categoria_id") val categoriaId: Int,
    @SerializedName("nivel") val nivel: Int,
    @SerializedName("indice") val indice: Int
)

data class ResultadoQuizRequest(
    @SerializedName("quiz_id") val quizId: Int,
    @SerializedName("puntaje") val puntaje: Int
)

// CORREGIDO: Mapeo correcto del backend (id, nivel, indice)
data class ProgresoCategoria(
    @SerializedName("id") val categoriaId: Int,
    @SerializedName("nivel") val nivelActual: Int,
    @SerializedName("indice") val preguntasCompletadas: Int,
    @SerializedName("total_preguntas") val totalPreguntas: Int = 10,
    @SerializedName("completado") val completado: Boolean,
    @SerializedName("bloqueado") val bloqueado: Boolean
)

data class UltimoProgreso(
    @SerializedName("categoria_id") val categoriaId: Int,
    @SerializedName("categoria_nombre") val categoriaNombre: String?,
    val nivel: Int,
    @SerializedName("progreso_percent") val progresoPercent: Float
)

data class Progreso(
    @SerializedName("categoria_id") val categoriaId: Int,
    val nombre: String,
    val porcentaje: Int
)

// --- PUNTOS ---
data class PuntosResponse(
    @SerializedName("puntos") val puntos: Int,
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("total_puntos") val totalPuntos: Int? = null
)

data class SumarPuntosRequest(
    val puntos: Int
)

// --- QUIZ DIARIO ---
data class EstadoQuizDiarioResponse(
    val completado: Boolean,
    val puntuacion: Int? = null,
    val fecha: String
)

data class CompletarQuizDiarioRequest(
    val puntuacion: Int
)

data class CompletarQuizDiarioResponse(
    val mensaje: String,
    val puntuacion: Int,
    val fecha: String
)

// --- MODELOS DE DATOS PARA ADMIN ---

data class AdminUserStat(
    val id: Int,
    val nombre: String,
    val correo: String?,
    @SerializedName("tipo_usuario") val tipoUsuario: String,
    @SerializedName("fecha_registro") val fechaRegistro: String?,
    @SerializedName("quizzes_completados") val quizzesCompletados: Int,
    @SerializedName("progreso_promedio") val progresoPromedio: Double,
    @SerializedName("ultima_actividad") val ultimaActividad: String?
)

data class AdminUserDetail(
    val usuario: AdminUserInfo,
    @SerializedName("progreso_categorias") val progresoCategorias: List<AdminUserCategoryProgress>,
    @SerializedName("historial_quizzes") val historialQuizzes: List<AdminUserQuizHistory>,
    val resumen: AdminUserSummary
)

data class AdminUserInfo(
    val id: Int,
    val nombre: String,
    val correo: String?,
    @SerializedName("tipo_usuario") val tipoUsuario: String,
    @SerializedName("fecha_registro") val fechaRegistro: String?
)

data class AdminUserCategoryProgress(
    @SerializedName("categoria_id") val categoriaId: Int,
    @SerializedName("categoria_nombre") val categoriaNombre: String,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("porcentaje_completado") val porcentajeCompletado: Int,
    @SerializedName("ultimo_acceso") val ultimoAcceso: String?,
    val nivel: Int?,
    @SerializedName("indice_pregunta") val indicePregunta: Int?,
    val completado: Boolean?
)

data class AdminUserQuizHistory(
    val id: Int,
    @SerializedName("quiz_id") val quizId: Int?,
    val puntaje: Int,
    @SerializedName("fecha_realizacion") val fechaRealizacion: String,
    val titulo: String?
)

data class AdminUserSummary(
    @SerializedName("total_categorias") val totalCategorias: Int,
    @SerializedName("categorias_completadas") val categoriasCompletadas: Int,
    @SerializedName("quizzes_realizados") val quizzesRealizados: Int,
    @SerializedName("promedio_puntaje") val promedioPuntaje: String
)

// --- INTERFAZ DE SERVICIO API ---

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body creds: Map<String, String>): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body data: Map<String, String>): Response<AuthResponse>

    @POST("api/auth/google")
    suspend fun googleLogin(@Body data: Map<String, String>): Response<AuthResponse>

    @POST("api/auth/guest")
    suspend fun guestLogin(): Response<AuthResponse>

    @GET("api/categorias")
    suspend fun getCategorias(): Response<List<Categoria>>

    @GET("api/senas")
    suspend fun getSenas(
        @Query("categoria_id") catId: Int? = null,
        @Query("busqueda") query: String? = null
    ): Response<List<Sena>>

    @GET("api/quiz/generarDinamico")
    suspend fun generarQuizDinamico(
        @Header("Authorization") token: String,
        @Query("categoria_id") catId: Int? = null,
        @Query("nivel") nivel: Int = 1
    ): Response<Quiz>

    @GET("api/progreso/mapa")
    suspend fun getProgresoMapa(@Header("Authorization") token: String): Response<List<ProgresoCategoria>>

    @POST("api/progreso/guardar")
    suspend fun guardarProgreso(
        @Header("Authorization") token: String,
        @Body datos: ProgresoRequest
    ): Response<Map<String, String>>

    @GET("api/progreso/actual")
    suspend fun getProgresoActual(@Header("Authorization") token: String): Response<UltimoProgreso>

    @GET("api/racha/actual")
    suspend fun getRachaActual(@Header("Authorization") token: String): Response<RachaResponse>

    @POST("api/sesion/registrar")
    suspend fun registrarSesion(@Header("Authorization") token: String): Response<RegistrarSesionResponse>

    @GET("api/quiz/hoy")
    suspend fun getQuizDelDia(@Header("Authorization") token: String): Response<Quiz>

    @POST("api/quiz/resultado")
    suspend fun enviarResultado(
        @Header("Authorization") token: String,
        @Body resultado: ResultadoQuizRequest
    ): Response<Map<String, Any>>

    @GET("api/progreso")
    suspend fun getProgreso(@Header("Authorization") token: String): Response<List<Progreso>>

    @Multipart
    @POST("api/admin/senas")
    suspend fun subirSena(
        @Header("Authorization") token: String,
        @Part("palabra") palabra: RequestBody,
        @Part("categoria_id") catId: RequestBody,
        @Part video: MultipartBody.Part
    ): Response<Map<String, Any>>

    // --- PUNTOS ---
    @GET("api/puntos/actual")
    suspend fun getPuntosActuales(@Header("Authorization") token: String): Response<PuntosResponse>

    @POST("api/puntos/sumar")
    suspend fun sumarPuntos(
        @Header("Authorization") token: String,
        @Body request: SumarPuntosRequest
    ): Response<PuntosResponse>

    // --- QUIZ DIARIO ---
    @GET("api/quiz/diario/estado")
    suspend fun getEstadoQuizDiario(@Header("Authorization") token: String): Response<EstadoQuizDiarioResponse>

    @POST("api/quiz/diario/completar")
    suspend fun completarQuizDiario(
        @Header("Authorization") token: String,
        @Body request: CompletarQuizDiarioRequest
    ): Response<CompletarQuizDiarioResponse>

    // --- ENDPOINTS DE ADMIN ---
    @GET("api/admin/stats/users")
    suspend fun getAdminUserStats(@Header("Authorization") token: String): Response<List<AdminUserStat>>

    @GET("api/admin/stats/progress/{userId}")
    suspend fun getAdminUserProgressDetail(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Response<AdminUserDetail>

    // --- ADMIN CATEGORIAS ---
    @POST("api/admin/categorias")
    suspend fun crearCategoria(
        @Header("Authorization") token: String,
        @Body data: Map<String, String?>
    ): Response<Map<String, Any>>

    @PUT("api/admin/categorias/{id}")
    suspend fun editarCategoria(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body data: Map<String, String?>
    ): Response<Map<String, String>>

    @DELETE("api/admin/categorias/{id}")
    suspend fun eliminarCategoria(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Map<String, String>>

    // --- ADMIN SEÑAS ---
    @PUT("api/admin/senas/{id}")
    suspend fun editarSena(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body data: Map<String, Any?>
    ): Response<Map<String, String>>

    @DELETE("api/admin/senas/{id}")
    suspend fun eliminarSena(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Map<String, String>>

    // --- ADMIN QUIZZES ---
    @GET("api/admin/quiz")
    suspend fun listarQuizzes(
        @Header("Authorization") token: String
    ): Response<List<Map<String, Any>>>

    @POST("api/admin/quiz")
    suspend fun crearQuiz(
        @Header("Authorization") token: String,
        @Body data: Map<String, Any?>
    ): Response<Map<String, Any>>

    @DELETE("api/admin/quiz/{id}")
    suspend fun eliminarQuiz(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Map<String, String>>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val api: ApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}