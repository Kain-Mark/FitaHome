package com.davidmn.fitahome.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidmn.fitahome.viewmodel.FitaViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class LogEntry(
    val date: String = "",
    val steps: Int = 0,
    val sleep: Double = 0.0,
    val water: Double = 0.0,
    val weight: Double = 0.0,
    val kcal: Int = 0,
    val notes: String = ""
)

data class RutinaEntry(
    val date: String = "",
    val ejercicios: List<Map<String, String>> = emptyList()
)

sealed class LogItem {
    data class Diario(val entry: LogEntry) : LogItem()
    data class Rutina(val entry: RutinaEntry) : LogItem()
}

@Composable
fun LogScreen(vm: FitaViewModel = viewModel()) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var items by remember { mutableStateOf<List<LogItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect
        val allItems = mutableListOf<LogItem>()
        var loadedCount = 0

        // Cargar logs diarios
        db.collection("users").document(userId).collection("logs").get()
            .addOnSuccessListener { docs ->
                docs.forEach { doc ->
                    allItems.add(LogItem.Diario(LogEntry(
                        date = doc.id,
                        steps = (doc.getLong("steps") ?: 0).toInt(),
                        sleep = doc.getDouble("sleep") ?: 0.0,
                        water = doc.getDouble("water") ?: 0.0,
                        weight = doc.getDouble("weight") ?: 0.0,
                        kcal = (doc.getLong("kcal") ?: 0).toInt(),
                        notes = doc.getString("notes") ?: ""
                    )))
                }
                loadedCount++
                if (loadedCount == 2) {
                    items = allItems.sortedByDescending {
                        when (it) {
                            is LogItem.Diario -> it.entry.date
                            is LogItem.Rutina -> it.entry.date
                        }
                    }
                    isLoading = false
                }
            }

        // Cargar rutinas
        db.collection("users").document(userId).collection("rutinas").get()
            .addOnSuccessListener { docs ->
                docs.forEach { doc ->
                    val ejercicios = (doc.get("ejercicios") as? List<*>)?.map { e ->
                        val map = e as? Map<*, *>
                        mapOf(
                            "nombre" to (map?.get("nombre") as? String ?: ""),
                            "series" to (map?.get("series") as? String ?: ""),
                            "reps" to (map?.get("reps") as? String ?: ""),
                            "kg" to (map?.get("kg") as? String ?: "")
                        )
                    } ?: emptyList()
                    allItems.add(LogItem.Rutina(RutinaEntry(date = doc.id, ejercicios = ejercicios)))
                }
                loadedCount++
                if (loadedCount == 2) {
                    items = allItems.sortedByDescending {
                        when (it) {
                            is LogItem.Diario -> it.entry.date
                            is LogItem.Rutina -> it.entry.date
                        }
                    }
                    isLoading = false
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(Icons.Filled.MenuBook, contentDescription = null,
                tint = Color(0xFFFFB300), modifier = Modifier.size(28.dp))
            Text("Historial", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Aún no hay registros",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold)
                    Text("Añade tu primer registro en el Diario",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items) { item ->
                    when (item) {
                        is LogItem.Diario -> LogEntryCard(item.entry)
                        is LogItem.Rutina -> RutinaLogCard(item.entry)
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun RutinaLogCard(entry: RutinaEntry) {
    var expanded by remember { mutableStateOf(false) }

    val formattedDate = try {
        val d = java.time.LocalDate.parse(entry.date)
        d.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es"))
            .replaceFirstChar { it.uppercase() } + ", ${d.dayOfMonth} de " +
                d.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es"))
                    .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) { entry.date }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.FitnessCenter, contentDescription = null,
                        tint = Color(0xFFFF6D00), modifier = Modifier.size(18.dp))
                    Text(formattedDate, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFF6D00).copy(alpha = 0.12f)
                    ) {
                        Text("Rutina · ${entry.ejercicios.size} ejercicios",
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6D00),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (expanded && entry.ejercicios.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))

                // Cabecera
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ejercicio", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(3f))
                    Text("Series", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Text("Reps", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Text("Kg", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }

                Spacer(modifier = Modifier.height(6.dp))

                entry.ejercicios.forEach { ej ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(ej["nombre"] ?: "", fontSize = 13.sp,
                            modifier = Modifier.weight(3f))
                        Text(ej["series"] ?: "-", fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text(ej["reps"] ?: "-", fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text(ej["kg"] ?: "-", fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun LogEntryCard(entry: LogEntry) {
    val formattedDate = try {
        val d = java.time.LocalDate.parse(entry.date)
        d.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es"))
            .replaceFirstChar { it.uppercase() } + ", ${d.dayOfMonth} de " +
                d.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es"))
                    .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) { entry.date }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(formattedDate, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                if (entry.weight > 0) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE53935).copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.MonitorWeight, contentDescription = null,
                                tint = Color(0xFFE53935), modifier = Modifier.size(14.dp))
                            Text("${entry.weight} kg", fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LogStat("👣", entry.steps.toString(), "pasos")
                LogStat("🌙", "${entry.sleep}h", "sueño")
                LogStat("💧", "${entry.water}L", "agua")
                LogStat("🔥", "${entry.kcal}", "kcal")
            }

            if (entry.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))
                Text(entry.notes, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun LogStat(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 18.sp)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}