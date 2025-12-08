package com.prantiux.milktick.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prantiux.milktick.data.MilkEntry
import com.prantiux.milktick.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

class AnalyticsViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()
    
    private val _analyticsData = MutableStateFlow(AnalyticsData())
    val analyticsData: StateFlow<AnalyticsData> = _analyticsData.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadAnalytics(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("AnalyticsViewModel", "Loading analytics for userId: $userId")
                val currentYearMonth = YearMonth.now()
                val previousYearMonth = currentYearMonth.minusMonths(1)
                
                // Get current and previous month data
                val currentMonthEntries = firestoreRepository.getMilkEntriesForMonthSync(userId, currentYearMonth)
                android.util.Log.d("AnalyticsViewModel", "Current month entries: ${currentMonthEntries.size}")
                val previousMonthEntries = firestoreRepository.getMilkEntriesForMonthSync(userId, previousYearMonth)
                android.util.Log.d("AnalyticsViewModel", "Previous month entries: ${previousMonthEntries.size}")
                
                // Calculate current and previous month totals (only count brought entries)
                val currentMonthQuantity = currentMonthEntries.filter { it.brought }.sumOf { it.quantity.toDouble() }.toFloat()
                val previousMonthQuantity = previousMonthEntries.filter { it.brought }.sumOf { it.quantity.toDouble() }.toFloat()
                
                // Calculate average daily consumption
                val daysInCurrentMonth = currentYearMonth.lengthOfMonth()
                val averageDaily = if (daysInCurrentMonth > 0) {
                    (currentMonthQuantity / daysInCurrentMonth * 10).roundToInt() / 10f
                } else 0f
                
                // Calculate delivery consistency
                val expectedDeliveries = daysInCurrentMonth
                val actualDeliveries = currentMonthEntries.count { it.brought }
                val consistency = if (expectedDeliveries > 0) {
                    ((actualDeliveries.toFloat() / expectedDeliveries) * 100).roundToInt()
                } else 0
                
                // Get current month rate from Firebase
                val currentRate = firestoreRepository.getMonthlyRate(userId, currentYearMonth)
                val avgCostPerLiter = currentRate?.ratePerLiter ?: 0f
                
                // Get yearly data (last 12 months)
                val yearlyData = mutableListOf<Float>()
                val costData = mutableListOf<Float>()
                
                for (i in 11 downTo 0) {
                    val month = currentYearMonth.minusMonths(i.toLong())
                    val entries = firestoreRepository.getMilkEntriesForMonthSync(userId, month)
                    val monthQuantity = entries.filter { it.brought }.sumOf { it.quantity.toDouble() }.toFloat()
                    yearlyData.add(monthQuantity)
                    
                    // Get actual rate from Firebase for each month
                    val monthRate = firestoreRepository.getMonthlyRate(userId, month)
                    val rateCost = monthRate?.ratePerLiter ?: 0f
                    costData.add(rateCost)
                }
                
                _analyticsData.value = AnalyticsData(
                    currentMonthQuantity = (currentMonthQuantity * 10).roundToInt() / 10f,
                    previousMonthQuantity = (previousMonthQuantity * 10).roundToInt() / 10f,
                    averageDailyConsumption = averageDaily,
                    costPerLiter = avgCostPerLiter,
                    deliveryConsistency = consistency,
                    yearlyMonthlyData = yearlyData,
                    monthlyCostData = costData
                )
            } catch (e: Exception) {
                android.util.Log.e("AnalyticsViewModel", "Error loading analytics", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

data class AnalyticsData(
    val currentMonthQuantity: Float = 0f,
    val previousMonthQuantity: Float = 0f,
    val averageDailyConsumption: Float = 0f,
    val costPerLiter: Float = 0f,
    val deliveryConsistency: Int = 0,
    val yearlyMonthlyData: List<Float> = List(12) { 0f },
    val monthlyCostData: List<Float> = List(12) { 0f }
)
