package com.prantiux.milktick.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prantiux.milktick.repository.AppGraph
import com.prantiux.milktick.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

class AnalyticsViewModel : ViewModel() {
    private val firestoreRepository: MainRepository = AppGraph.mainRepository
    
    private val _analyticsData = MutableStateFlow(AnalyticsData())
    val analyticsData: StateFlow<AnalyticsData> = _analyticsData.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadAnalytics(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentYearMonth = YearMonth.now()
                val previousYearMonth = currentYearMonth.minusMonths(1)

                // Aggregate directly from Room
                val currentMonthQuantity = firestoreRepository.getMonthlyTotalQuantity(userId, currentYearMonth)
                val previousMonthQuantity = firestoreRepository.getMonthlyTotalQuantity(userId, previousYearMonth)
                
                // Calculate average daily consumption
                val daysInCurrentMonth = currentYearMonth.lengthOfMonth()
                val averageDaily = if (daysInCurrentMonth > 0) {
                    (currentMonthQuantity / daysInCurrentMonth * 10).roundToInt() / 10f
                } else 0f
                
                // Calculate delivery consistency
                val expectedDeliveries = daysInCurrentMonth
                val actualDeliveries = firestoreRepository.getMonthlyDeliveryDays(userId, currentYearMonth)
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
                    val monthQuantity = firestoreRepository.getMonthlyTotalQuantity(userId, month)
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
