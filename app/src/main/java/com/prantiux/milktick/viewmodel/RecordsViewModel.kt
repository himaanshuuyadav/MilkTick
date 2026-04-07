package com.prantiux.milktick.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prantiux.milktick.repository.AppGraph
import com.prantiux.milktick.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

class RecordsViewModel : ViewModel() {
    private val firestoreRepository: MainRepository = AppGraph.mainRepository
    
    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()
    
    fun setSelectedYear(year: Int) {
        _uiState.value = _uiState.value.copy(selectedYear = year)
        loadYearData()
    }
    
    fun setCurrentUserId(userId: String) {
        _uiState.value = _uiState.value.copy(currentUserId = userId)
        loadAvailableYears()
        loadYearData()
    }
    
    private fun loadAvailableYears() {
        val userId = _uiState.value.currentUserId
        if (userId.isNotBlank()) {
            viewModelScope.launch {
                val years = firestoreRepository.getAvailableYears(userId)
                _uiState.value = _uiState.value.copy(availableYears = years)
            }
        }
    }
    
    fun refreshData() {
        loadYearData()
    }
    
    private fun loadYearData() {
        val userId = _uiState.value.currentUserId
        val year = _uiState.value.selectedYear

         if (userId.isNotBlank()) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val monthlySummaries = calculateMonthlySummaries(userId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    monthlySummaries = monthlySummaries
                )
            }
        }
    }
    
    private suspend fun calculateMonthlySummaries(userId: String): List<MonthlySummaryData> {
        val monthlySummaries = mutableListOf<MonthlySummaryData>()

        for (month in 1..12) {
            val yearMonth = YearMonth.of(_uiState.value.selectedYear, month)
            val totalDays = firestoreRepository.getMonthlyDeliveryDays(userId, yearMonth)
            val totalLiters = firestoreRepository.getMonthlyTotalQuantity(userId, yearMonth)
            
            // Get rate for this month
            val rate = firestoreRepository.getMonthlyRate(userId, yearMonth)
            val ratePerLiter = rate?.ratePerLiter ?: 0f
            val totalCost = totalLiters * ratePerLiter
            
            monthlySummaries.add(
                MonthlySummaryData(
                    yearMonth = yearMonth,
                    totalDays = totalDays,
                    totalLiters = totalLiters,
                    totalCost = totalCost
                )
            )
        }
        
        return monthlySummaries
    }
}

data class RecordsUiState(
    val selectedYear: Int = java.time.LocalDate.now().year,
    val currentUserId: String = "",
    val monthlySummaries: List<MonthlySummaryData> = emptyList(),
    val isLoading: Boolean = false,
    val availableYears: List<Int> = listOf(java.time.LocalDate.now().year)
)

data class MonthlySummaryData(
    val yearMonth: YearMonth,
    val totalDays: Int,
    val totalLiters: Float,
    val totalCost: Float
) 