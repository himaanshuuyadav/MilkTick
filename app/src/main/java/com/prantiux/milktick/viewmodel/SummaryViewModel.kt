package com.prantiux.milktick.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prantiux.milktick.data.MilkEntry
import com.prantiux.milktick.data.MonthlyRate
import com.prantiux.milktick.repository.AppGraph
import com.prantiux.milktick.repository.MainRepository
import com.prantiux.milktick.utils.CsvExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

class SummaryViewModel : ViewModel() {
    private val firestoreRepository: MainRepository = AppGraph.mainRepository
    private val csvExporter = CsvExporter()
    
    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()
    
    fun setSelectedYearMonth(yearMonth: YearMonth) {
        _uiState.value = _uiState.value.copy(selectedYearMonth = yearMonth)
        loadMonthlyData()
    }
    
    fun setCurrentUserId(userId: String) {
        _uiState.value = _uiState.value.copy(currentUserId = userId)
        loadMonthlyData()
    }
    
    fun refreshData() {
        loadMonthlyData()
    }
    
    private fun loadMonthlyData() {
        val userId = _uiState.value.currentUserId
        val yearMonth = _uiState.value.selectedYearMonth
        
        if (userId.isNotBlank()) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                firestoreRepository.getMilkEntriesForMonth(userId, yearMonth).collect { entries ->
                    val rate = firestoreRepository.getMonthlyRate(userId, yearMonth)
                    val ratePerLiter = rate?.ratePerLiter ?: 0f
                    
                    val totalDays = entries.count { it.brought }
                    val totalLiters = entries.sumOf { it.quantity.toDouble() }.toFloat()
                    val totalCost = totalLiters * ratePerLiter
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        entries = entries,
                        totalDays = totalDays,
                        totalLiters = totalLiters,
                        totalCost = totalCost,
                        ratePerLiter = ratePerLiter
                    )
                }
            }
        }
    }
    
    fun exportToCsv(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, isExported = false)
            
            val result = csvExporter.exportMonthlyData(
                context = context,
                entries = _uiState.value.entries,
                yearMonth = _uiState.value.selectedYearMonth.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")),
                totalDays = _uiState.value.totalDays,
                totalLiters = _uiState.value.totalLiters,
                totalCost = _uiState.value.totalCost
            )
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    isExported = true,
                    exportMessage = "CSV exported successfully to ${result.getOrNull()}"
                )
                
                // Reset exported state after 3 seconds
                delay(3000)
                _uiState.value = _uiState.value.copy(isExported = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    isExported = false,
                    exportMessage = "Error exporting CSV: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }
    
    fun clearExportMessage() {
        _uiState.value = _uiState.value.copy(exportMessage = null)
    }
}

data class SummaryUiState(
    val selectedYearMonth: YearMonth = YearMonth.now(),
    val currentUserId: String = "",
    val entries: List<MilkEntry> = emptyList(),
    val totalDays: Int = 0,
    val totalLiters: Float = 0f,
    val totalCost: Float = 0f,
    val ratePerLiter: Float = 0f,
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val isExported: Boolean = false,
    val exportMessage: String? = null
) 