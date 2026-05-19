package com.davidmn.fitahome.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidmn.fitahome.viewmodel.DayLog
import com.davidmn.fitahome.viewmodel.FitaViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DiarioScreen(vm: FitaViewModel = viewModel()) {
    val metas by vm.metas.collectAsState()
    val log by vm.currentLog.collectAsState()
    val viewDate by vm.viewDate.collectAsState()
    val today = LocalDate.now()
    val isToday = viewDate == today

    val dateLabel = if (isToday) "Hoy" else
        viewDate.format(DateTimeFormatter.ofPattern("EEE, d MMM", java.util.Locale("es")))

    var steps by remember(log) { mutableStateOf(if (log.steps == 0) "" else log.steps.toString()) }
    var sleep by remember(log) { mutableStateOf(if (log.sleep == 0.0) "" else log.sleep.toString()) }
    var water by remember(log) { mutableStateOf(if (log.water == 0.0) "" else log.water.toString()) }
    var workout by remember(log) { mutableStateOf(if (log.workout == 0) "" else log.workout.toString()) }
    var cardio by remember(log) { mutableStateOf(if (log.cardio == 0) "" else log.cardio.toString()) }
    var kcal by remember(log) { mutableStateOf(if (log.kcal == 0) "" else log.kcal.toString()) }
    var weight by remember(log) { mutableStateOf(if (log.weight == 0.0) "" else log.weight.toString()) }
    var breaks by remember(log) { mutableStateOf(log.breaks) }
    var notes by remember(log) { mutableStateOf(log.notes) }
    var guardado by remember { mutableStateOf(false) }

    var limitaciones by remember { mutableStateOf("") }
    var limitacionesGuardadas by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        FirebaseFirestore.getInstance()
            .collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                limitaciones = doc.getString("limitaciones") ?: ""
            }
    }

    if (guardado) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("¡Registro guardado!") },
            text = { Text("Los datos del día se han guardado correctamente.") },
            confirmButton = {
                Button(onClick = {
                    guardado = false
                    vm.clearLog()
                    steps = ""; sleep = ""; water = ""; workout = ""
                    cardio = ""; kcal = ""; weight = ""; breaks = 0; notes = ""
                }) { Text("Aceptar") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        // Navegación fecha
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.changeDate(-1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Día anterior",
                        tint = MaterialTheme.colorScheme.primary)
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(text = dateLabel, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = { vm.changeDate(1) }, enabled = !isToday) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Día siguiente",
                        tint = if (!isToday) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        MetricFieldIcon("Peso", weight, "kg", Icons.Filled.MonitorWeight, Color(0xFFE53935), fullWidth = true) { weight = it }
        Spacer(modifier = Modifier.height(4.dp))
        MetricFieldIcon("Fuerza", workout, "min · Meta: ${metas.workout}m", Icons.Filled.FitnessCenter, Color(0xFFFF6D00)) { workout = it }
        MetricFieldIcon("Cardio", cardio, "min · Meta: ${metas.cardio}m", Icons.Filled.Favorite, Color(0xFFE53935)) { cardio = it }
        MetricFieldIcon("Pasos", steps, "Meta: ${metas.steps}", Icons.Filled.DirectionsWalk, Color(0xFF1E88E5)) { steps = it }
        MetricFieldIcon("Sueño", sleep, "h · Meta: ${metas.sleep}h", Icons.Filled.Bedtime, Color(0xFF5E35B1)) { sleep = it }
        Spacer(modifier = Modifier.height(4.dp))
        MetricFieldIcon("Agua", water, "litros · Meta: ${metas.water}L", Icons.Filled.WaterDrop, Color(0xFF00ACC1), fullWidth = true) { water = it }
        Spacer(modifier = Modifier.height(4.dp))
        MetricFieldIcon("Calorías", kcal, "kcal · Meta: ${metas.kcal}", Icons.Filled.LocalFireDepartment, Color(0xFFFFB300), fullWidth = true) { kcal = it }

        Spacer(modifier = Modifier.height(8.dp))

        // Descansos activos
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF34C759).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Timer, contentDescription = null,
                            tint = Color(0xFF34C759), modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text("Descansos activos", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Meta: ${metas.breaks} veces", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { breaks = maxOf(0, breaks - 1) }) {
                        Icon(Icons.Filled.Remove, contentDescription = "Menos",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    Text(text = breaks.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(onClick = { breaks += 1 }) {
                        Icon(Icons.Filled.Add, contentDescription = "Más",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Notas
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFB300).copy(alpha = 0.08f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.StickyNote2, contentDescription = null,
                        tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                    Text("Diario y Sensaciones", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    placeholder = { Text("¿Cómo te sentiste entrenando hoy?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    maxLines = 10,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Limitaciones
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE53935).copy(alpha = 0.06f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Warning, contentDescription = null,
                        tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                    Text("Mis limitaciones", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = limitaciones,
                    onValueChange = {
                        limitaciones = it
                        limitacionesGuardadas = false
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    placeholder = { Text("Ej: Fascitis plantar, dolor de rodilla...") },
                    maxLines = 5,
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                        FirebaseFirestore.getInstance()
                            .collection("users").document(userId)
                            .set(mapOf("limitaciones" to limitaciones),
                                com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener { limitacionesGuardadas = true }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text(if (limitacionesGuardadas) "¡Actualizado!" else "Actualizar",
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón guardar
        Button(
            onClick = {
                val newLog = DayLog(
                    steps = steps.toIntOrNull() ?: 0,
                    sleep = sleep.toDoubleOrNull() ?: 0.0,
                    water = water.toDoubleOrNull() ?: 0.0,
                    workout = workout.toIntOrNull() ?: 0,
                    cardio = cardio.toIntOrNull() ?: 0,
                    kcal = kcal.toIntOrNull() ?: 0,
                    weight = weight.toDoubleOrNull() ?: 0.0,
                    breaks = breaks,
                    notes = notes
                )
                vm.saveLog(newLog)
                guardado = true
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Guardar registro", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun MetricFieldIcon(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconColor: Color,
    fullWidth: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor,
                        modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(unit, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(90.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}