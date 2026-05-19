package com.davidmn.fitahome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DayLog(
    val steps: Int = 0,
    val sleep: Double = 0.0,
    val water: Double = 0.0,
    val workout: Int = 0,
    val cardio: Int = 0,
    val breaks: Int = 0,
    val kcal: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0,
    val carbs: Int = 0,
    val weight: Double = 0.0,
    val chest: Double = 0.0,
    val waist: Double = 0.0,
    val hips: Double = 0.0,
    val notes: String = ""
)

data class UserMetas(
    val steps: Int = 10000,
    val sleep: Double = 7.0,
    val water: Double = 2.5,
    val kcal: Int = 2200,
    val workout: Int = 45,
    val cardio: Int = 20,
    val breaks: Int = 8
)

class FitaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _currentLog = MutableStateFlow(DayLog())
    val currentLog: StateFlow<DayLog> = _currentLog

    private val _viewDate = MutableStateFlow(LocalDate.now())
    val viewDate: StateFlow<LocalDate> = _viewDate

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _metas = MutableStateFlow(UserMetas())
    val metas: StateFlow<UserMetas> = _metas

    init {
        loadLog(LocalDate.now())
        loadMetas()
    }

    fun loadMetas() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                _metas.value = UserMetas(
                    steps = (doc.getLong("metaSteps") ?: 10000).toInt(),
                    sleep = doc.getDouble("metaSleep") ?: 7.0,
                    water = doc.getDouble("metaWater") ?: 2.5,
                    kcal = (doc.getLong("metaKcal") ?: 2200).toInt(),
                    workout = (doc.getLong("metaWorkout") ?: 45).toInt(),
                    cardio = (doc.getLong("metaCardio") ?: 20).toInt(),
                    breaks = (doc.getLong("metaBreaks") ?: 8).toInt()
                )
            }
    }

    fun changeDate(days: Long) {
        val newDate = _viewDate.value.plusDays(days)
        if (!newDate.isAfter(LocalDate.now())) {
            _viewDate.value = newDate
            loadLog(newDate)
        }
    }

    fun loadLog(date: LocalDate) {
        val userId = auth.currentUser?.uid ?: return
        val dateStr = date.toString()
        _isLoading.value = true

        db.collection("users")
            .document(userId)
            .collection("logs")
            .document(dateStr)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    _currentLog.value = DayLog(
                        steps = (doc.getLong("steps") ?: 0).toInt(),
                        sleep = doc.getDouble("sleep") ?: 0.0,
                        water = doc.getDouble("water") ?: 0.0,
                        workout = (doc.getLong("workout") ?: 0).toInt(),
                        cardio = (doc.getLong("cardio") ?: 0).toInt(),
                        breaks = (doc.getLong("breaks") ?: 0).toInt(),
                        kcal = (doc.getLong("kcal") ?: 0).toInt(),
                        protein = (doc.getLong("protein") ?: 0).toInt(),
                        fat = (doc.getLong("fat") ?: 0).toInt(),
                        carbs = (doc.getLong("carbs") ?: 0).toInt(),
                        weight = doc.getDouble("weight") ?: 0.0,
                        chest = doc.getDouble("chest") ?: 0.0,
                        waist = doc.getDouble("waist") ?: 0.0,
                        hips = doc.getDouble("hips") ?: 0.0,
                        notes = doc.getString("notes") ?: ""
                    )
                } else {
                    _currentLog.value = DayLog()
                }
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    fun saveLog(log: DayLog) {
        val userId = auth.currentUser?.uid ?: return
        val dateStr = _viewDate.value.toString()
        viewModelScope.launch {
            db.collection("users")
                .document(userId)
                .collection("logs")
                .document(dateStr)
                .set(log)
        }
    }

    fun updateField(field: String, value: Any) {
        val userId = auth.currentUser?.uid ?: return
        val dateStr = _viewDate.value.toString()
        db.collection("users")
            .document(userId)
            .collection("logs")
            .document(dateStr)
            .update(field, value)
            .addOnFailureListener {
                db.collection("users")
                    .document(userId)
                    .collection("logs")
                    .document(dateStr)
                    .set(mapOf(field to value))
            }
    }

    fun clearLog() {
        _currentLog.value = DayLog()
    }
}