package com.example.applsm.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.applsm.ui.AppViewModel
import com.example.applsm.ui.UiState
import com.example.applsm.ui.theme.CyanLsm
import com.example.applsm.ui.theme.GOOGLE_WEB_CLIENT_ID
import com.example.applsm.ui.theme.PinkLsm
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(nav: NavController, vm: AppViewModel) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val context = LocalContext.current
    val uiState = vm.uiState

    val handleLoginSuccess: (String) -> Unit = { userType ->
        val route = if (userType == "admin") "admin_dashboard" else "main"
        nav.navigate(route) {
            popUpTo("login") { inclusive = true }
        }
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                vm.loginWithGoogle(
                    idToken = account.idToken ?: "",
                    name = account.displayName ?: "Usuario Google",
                    email = account.email ?: "",
                    googleId = account.id ?: "",
                    onSuccess = handleLoginSuccess
                )
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Error Google: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            Toast.makeText(context, uiState.message, Toast.LENGTH_LONG).show()
            vm.uiState = UiState.Idle
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(CyanLsm), contentAlignment = Alignment.Center) { Text("👋", fontSize = 40.sp) }
        Spacer(Modifier.height(24.dp))
        Text("SignApp LSM", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { vm.login(email, pass, onSuccess = handleLoginSuccess) }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = CyanLsm)) { if (vm.uiState is UiState.Loading) CircularProgressIndicator(color = Color.White) else Text("Entrar") }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = { val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(GOOGLE_WEB_CLIENT_ID).requestEmail().build(); val googleSignInClient = GoogleSignIn.getClient(context, gso); googleLauncher.launch(googleSignInClient.signInIntent) }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)) { Text("Iniciar con Google") }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { nav.navigate("register") }) { Text("¿No tienes cuenta? Regístrate aquí", color = PinkLsm) }
        TextButton(onClick = { vm.guestLogin { userType ->
            nav.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        } }) { Text("Entrar como Invitado", color = Color.Gray) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(nav: NavController, vm: AppViewModel) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val context = LocalContext.current
    val uiState = vm.uiState
    LaunchedEffect(uiState) { if (uiState is UiState.Error) { Toast.makeText(context, uiState.message, Toast.LENGTH_LONG).show(); vm.uiState = UiState.Idle } }

    val handleRegisterSuccess: (String) -> Unit = { userType ->
        // New users are typically not admins, so we navigate to main.
        // The logic is here just in case.
        val route = if (userType == "admin") "admin_dashboard" else "main"
        nav.navigate(route) {
            popUpTo("login") { inclusive = true }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Crear Cuenta", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = CyanLsm)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (nombre.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                    vm.register(nombre, email, pass, onSuccess = handleRegisterSuccess)
                } else {
                    Toast.makeText(context, "Llena todo", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PinkLsm)
        ) {
            if (vm.uiState is UiState.Loading) CircularProgressIndicator(color = Color.White) else Text("Registrarse")
        }
        TextButton(onClick = { nav.popBackStack() }) { Text("Volver", color = Color.Gray) }
    }
}