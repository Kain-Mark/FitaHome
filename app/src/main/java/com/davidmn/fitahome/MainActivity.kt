package com.davidmn.fitahome

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidmn.fitahome.navigation.FitaNavigation
import com.davidmn.fitahome.ui.theme.FitaHomeTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitaHomeTheme {
                FitaHomeApp()
            }
        }
    }
}

@Composable
fun FitaHomeApp() {
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    if (currentUser == null) {
        LoginScreen(
            onLoginSuccess = { currentUser = auth.currentUser }
        )
    } else {
        FitaNavigation(onLogout = { currentUser = null })
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: ApiException) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "FitaHome",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tu panel fitness personal",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = { launcher.launch(googleSignInClient.signInIntent) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Entrar con Google", fontSize = 16.sp)
        }
    }
}