package com.davidmn.fitahome.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.davidmn.fitahome.screens.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Diario : Screen("diario", "Diario", Icons.Filled.DateRange)
    object Dashboard : Screen("dashboard", "Dash", Icons.Filled.BarChart)
    object Medidas : Screen("medidas", "Medidas", Icons.Filled.Straighten)
    object Log : Screen("log", "Log", Icons.Filled.Book)
    object Rutinas : Screen("rutinas", "Rutinas", Icons.Filled.FitnessCenter)
}

@Composable
fun FitaNavigation(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userName = auth.currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Usuario"
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Metas editables
    var metaSteps by remember { mutableStateOf("10000") }
    var metaSleep by remember { mutableStateOf("7.0") }
    var metaWater by remember { mutableStateOf("2.5") }
    var metaKcal by remember { mutableStateOf("2200") }
    var metaWorkout by remember { mutableStateOf("45") }
    var metaCardio by remember { mutableStateOf("20") }
    var metaBreaks by remember { mutableStateOf("8") }
    var settingsGuardadas by remember { mutableStateOf(false) }

    // Cargar metas desde Firebase
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                metaSteps = (doc.getLong("metaSteps") ?: 10000).toString()
                metaSleep = (doc.getDouble("metaSleep") ?: 7.0).toString()
                metaWater = (doc.getDouble("metaWater") ?: 2.5).toString()
                metaKcal = (doc.getLong("metaKcal") ?: 2200).toString()
                metaWorkout = (doc.getLong("metaWorkout") ?: 45).toString()
                metaCardio = (doc.getLong("metaCardio") ?: 20).toString()
                metaBreaks = (doc.getLong("metaBreaks") ?: 8).toString()
            }
    }

    val items = listOf(
        Screen.Diario, Screen.Dashboard, Screen.Medidas, Screen.Log, Screen.Rutinas
    )

    // Diálogo cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        auth.signOut()
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Cerrar sesión") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo ajustes
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Settings, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Text("Mis metas diarias")
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (settingsGuardadas) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF34C759).copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null,
                                    tint = Color(0xFF34C759), modifier = Modifier.size(16.dp))
                                Text("¡Metas guardadas!", fontSize = 13.sp,
                                    color = Color(0xFF34C759), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    MetaField("Pasos diarios", metaSteps, Icons.Filled.DirectionsWalk,
                        Color(0xFF1E88E5)) { metaSteps = it; settingsGuardadas = false }
                    MetaField("Sueño (horas)", metaSleep, Icons.Filled.Bedtime,
                        Color(0xFF5E35B1)) { metaSleep = it; settingsGuardadas = false }
                    MetaField("Agua (litros)", metaWater, Icons.Filled.WaterDrop,
                        Color(0xFF00ACC1)) { metaWater = it; settingsGuardadas = false }
                    MetaField("Calorías (kcal)", metaKcal, Icons.Filled.LocalFireDepartment,
                        Color(0xFFFFB300)) { metaKcal = it; settingsGuardadas = false }
                    MetaField("Fuerza (min)", metaWorkout, Icons.Filled.FitnessCenter,
                        Color(0xFFFF6D00)) { metaWorkout = it; settingsGuardadas = false }
                    MetaField("Cardio (min)", metaCardio, Icons.Filled.Favorite,
                        Color(0xFFE53935)) { metaCardio = it; settingsGuardadas = false }
                    MetaField("Descansos", metaBreaks, Icons.Filled.Timer,
                        Color(0xFF34C759)) { metaBreaks = it; settingsGuardadas = false }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val userId = auth.currentUser?.uid ?: return@Button
                        db.collection("users").document(userId)
                            .set(mapOf(
                                "metaSteps" to (metaSteps.toLongOrNull() ?: 10000L),
                                "metaSleep" to (metaSleep.toDoubleOrNull() ?: 7.0),
                                "metaWater" to (metaWater.toDoubleOrNull() ?: 2.5),
                                "metaKcal" to (metaKcal.toLongOrNull() ?: 2200L),
                                "metaWorkout" to (metaWorkout.toLongOrNull() ?: 45L),
                                "metaCardio" to (metaCardio.toLongOrNull() ?: 20L),
                                "metaBreaks" to (metaBreaks.toLongOrNull() ?: 8L)
                            ), com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener { settingsGuardadas = true }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSettingsDialog = false; settingsGuardadas = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "FitaHome",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Hola, $userName 👋",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row {
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Ajustes",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.Filled.Logout, contentDescription = "Cerrar sesión",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = Screen.Diario.route) {
                composable(Screen.Diario.route) { DiarioScreen() }
                composable(Screen.Dashboard.route) { DashboardScreen() }
                composable(Screen.Medidas.route) { MedidasScreen() }
                composable(Screen.Log.route) { LogScreen() }
                composable(Screen.Rutinas.route) { RutinasScreen() }
            }
        }
    }
}

@Composable
fun MetaField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(label, fontSize = 13.sp, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(80.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
        )
    }
}