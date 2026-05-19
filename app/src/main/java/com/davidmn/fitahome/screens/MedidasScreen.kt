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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidmn.fitahome.viewmodel.FitaViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

enum class Genero { HOMBRE, MUJER }

data class InstruccionMedida(
    val titulo: String,
    val descripcion: String
)

val instruccionesHombre = mapOf(
    "Pecho" to InstruccionMedida(
        "Pecho (hombre)",
        "Rodea el pecho con la cinta métrica a la altura de las axilas, pasando por encima de los pectorales en su punto más ancho. Mantén los brazos ligeramente separados y respira con normalidad."
    ),
    "Cintura" to InstruccionMedida(
        "Cintura (hombre)",
        "Mide a la altura del ombligo o en el punto más estrecho del abdomen. No metas tripa — la medida debe ser natural, sin tensar el abdomen."
    ),
    "Cadera" to InstruccionMedida(
        "Cadera (hombre)",
        "Coloca la cinta en el punto más ancho de los glúteos, generalmente unos 20 cm por debajo de la cintura. Mantente erguido con los pies juntos."
    )
)

val instruccionesMujer = mapOf(
    "Pecho" to InstruccionMedida(
        "Pecho (mujer)",
        "Rodea el pecho con la cinta a la altura del punto más prominente del busto. Asegúrate de que la cinta esté horizontal y no apriete. Lleva sujetador sin relleno para mayor precisión."
    ),
    "Cintura" to InstruccionMedida(
        "Cintura (mujer)",
        "Mide en el punto más estrecho del torso, generalmente entre las costillas y el ombligo. Suele estar unos 2-3 cm por encima del ombligo. Respira con normalidad y no metas tripa."
    ),
    "Cadera" to InstruccionMedida(
        "Cadera (mujer)",
        "Coloca la cinta en el punto más ancho de caderas y glúteos. Mantente erguida con los pies juntos y el peso distribuido por igual. Suele estar unos 20-25 cm por debajo de la cintura."
    )
)

@Composable
fun MedidasScreen(vm: FitaViewModel = viewModel()) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var genero by remember { mutableStateOf(Genero.HOMBRE) }
    var lastChest by remember { mutableStateOf("-") }
    var lastWaist by remember { mutableStateOf("-") }
    var lastHips by remember { mutableStateOf("-") }
    var chest by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hips by remember { mutableStateOf("") }
    var guardado by remember { mutableStateOf(false) }
    var instruccionActiva by remember { mutableStateOf<InstruccionMedida?>(null) }

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect
        db.collection("users").document(userId).collection("medidas")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1).get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    val doc = docs.documents[0]
                    lastChest = (doc.getDouble("chest") ?: 0.0).let { if (it == 0.0) "-" else "$it cm" }
                    lastWaist = (doc.getDouble("waist") ?: 0.0).let { if (it == 0.0) "-" else "$it cm" }
                    lastHips = (doc.getDouble("hips") ?: 0.0).let { if (it == 0.0) "-" else "$it cm" }
                }
            }
    }

    if (guardado) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("¡Medidas guardadas!") },
            text = { Text("Tus medidas se han guardado correctamente.") },
            confirmButton = {
                Button(onClick = {
                    guardado = false
                    chest = ""; waist = ""; hips = ""
                }) { Text("Aceptar") }
            }
        )
    }

    instruccionActiva?.let { instruccion ->
        AlertDialog(
            onDismissRequest = { instruccionActiva = null },
            title = { Text(instruccion.titulo) },
            text = { Text(instruccion.descripcion, lineHeight = 22.sp) },
            confirmButton = {
                Button(onClick = { instruccionActiva = null }) { Text("Entendido") }
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
            Icon(Icons.Filled.Straighten, contentDescription = null,
                tint = Color(0xFF5E35B1), modifier = Modifier.size(28.dp))
            Text("Medidas corporales", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        // Selector hombre / mujer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { genero = Genero.HOMBRE },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (genero == Genero.HOMBRE) Color(0xFF1E88E5) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (genero == Genero.HOMBRE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Filled.Man, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hombre", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { genero = Genero.MUJER },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (genero == Genero.MUJER) Color(0xFFE91E8C) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (genero == Genero.MUJER) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Filled.Woman, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mujer", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Últimas medidas
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Últimas medidas", fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MedidaResumen("Pecho", lastChest, Color(0xFF1E88E5))
                    MedidaResumen("Cintura", lastWaist, Color(0xFFFF6D00))
                    MedidaResumen("Cadera", lastHips, Color(0xFF5E35B1))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Text("Nuevo registro", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        // Campos con botón de info
        val instrucciones = if (genero == Genero.HOMBRE) instruccionesHombre else instruccionesMujer

        MedidaFieldIconInfo("Pecho", chest, Color(0xFF1E88E5),
            onInfo = { instruccionActiva = instrucciones["Pecho"] }) { chest = it }
        MedidaFieldIconInfo("Cintura", waist, Color(0xFFFF6D00),
            onInfo = { instruccionActiva = instrucciones["Cintura"] }) { waist = it }
        MedidaFieldIconInfo("Cadera", hips, Color(0xFF5E35B1),
            onInfo = { instruccionActiva = instrucciones["Cadera"] }) { hips = it }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val userId = auth.currentUser?.uid ?: return@Button
                val medida = hashMapOf(
                    "chest" to (chest.toDoubleOrNull() ?: 0.0),
                    "waist" to (waist.toDoubleOrNull() ?: 0.0),
                    "hips" to (hips.toDoubleOrNull() ?: 0.0),
                    "fecha" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                db.collection("users").document(userId).collection("medidas")
                    .add(medida)
                    .addOnSuccessListener {
                        lastChest = if (chest.isEmpty()) "-" else "$chest cm"
                        lastWaist = if (waist.isEmpty()) "-" else "$waist cm"
                        lastHips = if (hips.isEmpty()) "-" else "$hips cm"
                        guardado = true
                    }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Guardar medidas", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun MedidaResumen(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp,
                color = color, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
        }
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MedidaFieldIconInfo(
    label: String,
    value: String,
    color: Color,
    onInfo: () -> Unit,
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
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Straighten, contentDescription = null,
                        tint = color, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("cm", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onInfo, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = "Cómo medir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp))
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
}