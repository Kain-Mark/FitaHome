package com.davidmn.fitahome.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidmn.fitahome.viewmodel.FitaViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

data class WeekData(
    val date: String = "",
    val steps: Int = 0,
    val sleep: Double = 0.0,
    val water: Double = 0.0,
    val kcal: Int = 0,
    val workout: Int = 0,
    val cardio: Int = 0
)

@Composable
fun DashboardScreen(vm: FitaViewModel = viewModel()) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var weekData by remember { mutableStateOf<List<WeekData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var vistaCircular by remember { mutableStateOf(false) }

    // Metas
    val metas by vm.metas.collectAsState()
    val metaSteps = metas.steps
    val metaSleep = metas.sleep
    val metaWater = metas.water
    val metaKcal = metas.kcal
    val metaWorkout = metas.workout
    val metaCardio = metas.cardio

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect
        val dates = (0..6).map { LocalDate.now().minusDays(it.toLong()).toString() }
        db.collection("users").document(userId).collection("logs").get()
            .addOnSuccessListener { docs ->
                val allData = docs.associate { doc ->
                    doc.id to WeekData(
                        date = doc.id,
                        steps = (doc.getLong("steps") ?: 0).toInt(),
                        sleep = doc.getDouble("sleep") ?: 0.0,
                        water = doc.getDouble("water") ?: 0.0,
                        kcal = (doc.getLong("kcal") ?: 0).toInt(),
                        workout = (doc.getLong("workout") ?: 0).toInt(),
                        cardio = (doc.getLong("cardio") ?: 0).toInt()
                    )
                }
                weekData = dates.map { date -> allData[date] ?: WeekData(date = date) }.reversed()
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = { vistaCircular = false }) {
                    Text("≡", fontSize = 32.sp,
                        color = if (!vistaCircular) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { vistaCircular = true }) {
                    Text("◎", fontSize = 32.sp,
                        color = if (vistaCircular) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val todayData = weekData.lastOrNull() ?: WeekData()
            val totalSteps = weekData.sumOf { it.steps }
            val avgSleep = weekData.filter { it.sleep > 0 }.map { it.sleep }.average().let { if (it.isNaN()) 0.0 else it }
            val totalWater = weekData.sumOf { it.water }
            val totalKcal = weekData.sumOf { it.kcal }
            val totalWorkout = weekData.sumOf { it.workout }
            val totalCardio = weekData.sumOf { it.cardio }

            val metrics = listOf(
                Triple("Calorías", todayData.kcal.toDouble(), metaKcal.toDouble()),
                Triple("Fuerza", todayData.workout.toDouble(), metaWorkout.toDouble()),
                Triple("Cardio", todayData.cardio.toDouble(), metaCardio.toDouble()),
                Triple("Pasos", todayData.steps.toDouble(), metaSteps.toDouble()),
                Triple("Sueño", todayData.sleep, metaSleep),
                Triple("Agua", todayData.water, metaWater)
            )

            val colors = listOf(
                Color(0xFFFFB300),
                Color(0xFFFF6D00),
                Color(0xFFE53935),
                Color(0xFF1E88E5),
                Color(0xFF5E35B1),
                Color(0xFF00ACC1)
            )

            if (vistaCircular) {
                // Vista circular
                Text("Progreso de hoy", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp))
                val rows = metrics.chunked(3)
                rows.forEach { rowMetrics ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowMetrics.forEachIndexed { idx, (label, current, target) ->
                            val globalIdx = metrics.indexOf(Triple(label, current, target))
                            Card(modifier = Modifier.weight(1f)) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgress(
                                        progress = (current / target).coerceIn(0.0, 1.0).toFloat(),
                                        color = colors[globalIdx],
                                        size = 72.dp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${current.toInt()} / ${target.toInt()}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                // Vista barras
                Text("Resumen últimos 7 días", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResumenCard("Pasos", totalSteps.toString(), "total", Modifier.weight(1f))
                    ResumenCard("Sueño", String.format("%.1f", avgSleep), "h media", Modifier.weight(1f))
                    ResumenCard("Agua", String.format("%.1f", totalWater), "L total", Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResumenCard("Calorías", totalKcal.toString(), "kcal total", Modifier.weight(1f))
                    ResumenCard("Fuerza", "${totalWorkout}min", "total", Modifier.weight(1f))
                    ResumenCard("Cardio", "${totalCardio}min", "total", Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Detalle por día", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp))

                weekData.forEach { day ->
                    DayCard(day)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(160.dp))
        }
    }
}

@Composable
fun CircularProgress(progress: Float, color: Color, size: androidx.compose.ui.unit.Dp) {
    val bgColor = color.copy(alpha = 0.15f)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = size.toPx() * 0.12f
            val inset = stroke / 2
            drawArc(
                color = bgColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(this.size.width - stroke, this.size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(this.size.width - stroke, this.size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${(progress * 100).toInt()}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ResumenCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(unit, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DayCard(day: WeekData) {
    val shortDate = try {
        val d = java.time.LocalDate.parse(day.date)
        d.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("es"))
            .replaceFirstChar { it.uppercase() } + " ${d.dayOfMonth}"
    } catch (e: Exception) { day.date }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(shortDate, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.width(56.dp))
            MiniStat("👣", day.steps.toString())
            MiniStat("🌙", "${day.sleep}h")
            MiniStat("💧", "${day.water}L")
            MiniStat("🔥", "${day.kcal}")
        }
    }
}

@Composable
fun MiniStat(emoji: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 16.sp)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}