package com.prantiux.milktick.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prantiux.milktick.repository.AppGraph
import com.prantiux.milktick.repository.MainRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

class RecordsViewModel : ViewModel() {
    private val firestoreRepository: MainRepository = AppGraph.mainRepository
    private var yearObserverJob: Job? = null
    
    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()
    
    fun setSelectedYear(year: Int) {
        if (_uiState.value.selectedYear == year) return
        _uiState.value = _uiState.value.copy(selectedYear = year)
        observeYearData()
    }
    
    fun setCurrentUserId(userId: String) {
        if (_uiState.value.currentUserId == userId) return
        _uiState.value = _uiState.value.copy(currentUserId = userId)
        loadAvailableYears()
        observeYearData()
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
        loadAvailableYears()
        observeYearData()
    }
    
    private fun observeYearData() {
        val userId = _uiState.value.currentUserId
        val selectedYear = _uiState.value.selectedYear

        if (userId.isBlank()) return

        yearObserverJob?.cancel()

        val shouldShowBlockingLoader = _uiState.value.monthlySummaries.isEmpty()
        if (shouldShowBlockingLoader) {
            _uiState.value = _uiState.value.copy(isLoading = true)
        }

        yearObserverJob = viewModelScope.launch {
            firestoreRepository.getMilkEntriesForYear(userId, selectedYear).collectLatest { entries ->
                val monthlySummaries = calculateMonthlySummaries(
                    userId = userId,
                    selectedYear = selectedYear,
                    yearEntries = entries
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    monthlySummaries = monthlySummaries
                )
            }
        }
    }
    
    private suspend fun calculateMonthlySummaries(
        userId: String,
        selectedYear: Int,
        yearEntries: List<com.prantiux.milktick.data.MilkEntry>
    ): List<MonthlySummaryData> {
        val monthlySummaries = mutableListOf<MonthlySummaryData>()

        for (month in 1..12) {
            val yearMonth = YearMonth.of(selectedYear, month)
            val monthEntries = yearEntries.filter { entry ->
                entry.date.year == selectedYear && entry.date.monthValue == month && entry.brought
            }
            val totalDays = monthEntries.size
            val totalLiters = monthEntries.sumOf { it.quantity.toDouble() }.toFloat()
            
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