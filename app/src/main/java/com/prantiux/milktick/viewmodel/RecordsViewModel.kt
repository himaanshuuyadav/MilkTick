package com.prantiux.milktick.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prantiux.milktick.data.MilkEntry
import com.prantiux.milktick.data.MonthlyRate
import com.prantiux.milktick.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

class RecordsViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()
    
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
        
        android.util.Log.d("RecordsViewModel", "Loading data for userId=$userId, year=$year")
        
         if (userId.isNotBlank()) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                firestoreRepository.getMilkEntriesForYear(userId, year).collect { entries ->
                    android.util.Log.d("RecordsViewModel", "Received ${entries.size} entries from Firestore")
                    entries.forEach { entry ->
                        android.util.Log.d("RecordsViewModel", "Entry: date=${entry.date}, quantity=${entry.quantity}, brought=${entry.brought}")
                    }
                    val monthlySummaries = calculateMonthlySummaries(entries, userId)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        monthlySummaries = monthlySummaries
                    )
                }
            }
        }
    }
    
    private suspend fun calculateMonthlySummaries(entries: List<MilkEntry>, userId: String): List<MonthlySummaryData> {
        val monthlySummaries = mutableListOf<MonthlySummaryData>()
        
        android.util.Log.d("RecordsViewModel", "Calculating summaries for ${entries.size} entries")
        
        for (month in 1..12) {
            val yearMonth = YearMonth.of(_uiState.value.selectedYear, month)
            val monthEntries = entries.filter { entry ->
                val matches = entry.date.year == yearMonth.year && entry.date.monthValue == yearMonth.monthValue
                if (matches) {
                    android.util.Log.d("RecordsViewModel", "Month $month: Found entry on ${entry.date} with quantity ${entry.quantity}, brought: ${entry.brought}")
                }
                matches
            }
            
            val totalDays = monthEntries.count { it.brought }
            val totalLiters = monthEntries.filter { it.brought }.sumOf { it.quantity.toDouble() }.toFloat()
            
            if (monthEntries.isNotEmpty()) {
                android.util.Log.d("RecordsViewModel", "Month $month summary: $totalDays days, $totalLiters L from ${monthEntries.size} entries")
            }
            
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