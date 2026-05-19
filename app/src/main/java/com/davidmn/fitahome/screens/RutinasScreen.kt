package com.davidmn.fitahome.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidmn.fitahome.viewmodel.FitaViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class EjercicioRow(
    val nombre: String = "",
    val series: String = "",
    val reps: String = "",
    val kg: String = ""
)

@Composable
fun RutinasScreen(vm: FitaViewModel = viewModel()) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var ejercicios by remember { mutableStateOf(List(4) { EjercicioRow() }) }
    var guardado by remember { mutableStateOf(false) }

    if (guardado) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("¡Rutina guardada!") },
            text = { Text("Tu entrenamiento de hoy se ha guardado correctamente.") },
            confirmButton = {
                Button(onClick = {
                    guardado = false
                    ejercicios = List(4) { EjercicioRow() }
                }) { Text("Aceptar") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(Icons.Filled.FitnessCenter, contentDescription = null,
                tint = Color(0xFFFF6D00), modifier = Modifier.size(28.dp))
            Text("Entrenamiento", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        // Cabecera columnas
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Ejercicio", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(3.5f))
                Text("Series", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(0.8f),
                    textAlign = TextAlign.Center)
                Text("Reps", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(0.8f),
                    textAlign = TextAlign.Center)
                Text("Kg", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(0.8f),
                    textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filas de ejercicios
        ejercicios.forEachIndexed { index, ejercicio ->
            EjercicioRowItem(
                ejercicio = ejercicio,
                onUpdate = { updated ->
                    ejercicios = ejercicios.toMutableList().also { it[index] = updated }
                },
                onDelete = if (ejercicios.size > 1) {
                    { ejercicios = ejercicios.toMutableList().also { it.removeAt(index) } }
                } else null
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón añadir fila
        OutlinedButton(
            onClick = { ejercicios = ejercicios + EjercicioRow() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Añadir ejercicio")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón guardar
        Button(
            onClick = {
                val userId = auth.currentUser?.uid ?: return@Button
                val today = java.time.LocalDate.now().toString()
                val rutina = hashMapOf(
                    "fecha" to today,
                    "tipo" to "rutina",
                    "ejercicios" to ejercicios.filter { it.nombre.isNotEmpty() }.map {
                        hashMapOf(
                            "nombre" to it.nombre,
                            "series" to it.series,
                            "reps" to it.reps,
                            "kg" to it.kg
                        )
                    }
                )
                db.collection("users").document(userId)
                    .collection("rutinas").document(today)
                    .set(rutina)
                    .addOnSuccessListener { guardado = true }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Guardar entrenamiento", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(160.dp))
    }
}

@Composable
fun EjercicioRowItem(
    ejercicio: EjercicioRow,
    onUpdate: (EjercicioRow) -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = ejercicio.nombre,
                onValueChange = { onUpdate(ejercicio.copy(nombre = it)) },
                modifier = Modifier.weight(3.5f),
                placeholder = { Text("Ejercicio", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
            )
            OutlinedTextField(
                value = ejercicio.series,
                onValueChange = { onUpdate(ejercicio.copy(series = it)) },
                modifier = Modifier.weight(0.8f),
                placeholder = { Text("0", fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
            )
            OutlinedTextField(
                value = ejercicio.reps,
                onValueChange = { onUpdate(ejercicio.copy(reps = it)) },
                modifier = Modifier.weight(0.8f),
                placeholder = { Text("0", fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
            )
            OutlinedTextField(
                value = ejercicio.kg,
                onValueChange = { onUpdate(ejercicio.copy(kg = it)) },
                modifier = Modifier.weight(0.8f),
                placeholder = { Text("0", fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
            )
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp))
                }
            } else {
                Spacer(modifier = Modifier.size(32.dp))
            }
        }
    }
}