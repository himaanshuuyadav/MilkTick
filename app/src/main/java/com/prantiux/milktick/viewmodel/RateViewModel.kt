package com.prantiux.milktick.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prantiux.milktick.data.MonthlyRate
import com.prantiux.milktick.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

class RateViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()
    
    private val _uiState = MutableStateFlow(RateUiState())
    val uiState: StateFlow<RateUiState> = _uiState.asStateFlow()
    
    fun updateRate(rate: String) {
        val rateFloat = rate.toFloatOrNull() ?: 0f
        _uiState.value = _uiState.value.copy(rate = rateFloat, rateText = rate)
    }
    
    fun updateDefaultQuantity(quantity: String) {
        val quantityFloat = quantity.toFloatOrNull() ?: 0f
        _uiState.value = _uiState.value.copy(defaultQuantity = quantityFloat, defaultQuantityText = quantity)
    }
    
    fun updateYearMonth(yearMonth: YearMonth) {
        _uiState.value = _uiState.value.copy(selectedYearMonth = yearMonth)
        loadRate()
    }
    
    fun saveRate(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, saveButtonState = SaveButtonState.LOADING)
            
            val rate = MonthlyRate(
                yearMonth = _uiState.value.selectedYearMonth,
                ratePerLiter = _uiState.value.rate,
                defaultQuantity = _uiState.value.defaultQuantity,
                userId = userId
            )
            
            val result = firestoreRepository.saveMonthlyRate(rate)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Rate saved successfully",
                    saveButtonState = SaveButtonState.SAVED,
                    isEditMode = false,
                    hasData = true
                )
                
                // Hide saved button after 2 seconds
                kotlinx.coroutines.delay(2000)
                _uiState.value = _uiState.value.copy(saveButtonState = SaveButtonState.HIDDEN)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Error saving rate",
                    saveButtonState = SaveButtonState.SAVE
                )
            }
        }
    }
    
    fun enableEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = true,
            saveButtonState = SaveButtonState.SAVE
        )
    }
    
    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(
            isEditMode = false,
            saveButtonState = SaveButtonState.HIDDEN
        )
        loadRate() // Reload original values
    }
    
    private fun loadRate() {
        viewModelScope.launch {
            val userId = _uiState.value.currentUserId
            if (userId.isNotBlank()) {
                val rate = firestoreRepository.getMonthlyRate(userId, _uiState.value.selectedYearMonth)
                val hasExistingData = rate != null && rate.ratePerLiter > 0
                
                _uiState.value = _uiState.value.copy(
                    rate = rate?.ratePerLiter ?: 0f,
                    rateText = if (rate?.ratePerLiter != null && rate.ratePerLiter > 0) rate.ratePerLiter.toString() else "",
                    defaultQuantity = rate?.defaultQuantity ?: 0f,
                    defaultQuantityText = if (rate?.defaultQuantity != null && rate.defaultQuantity > 0) rate.defaultQuantity.toString() else "",
                    hasData = hasExistingData,
                    isEditMode = !hasExistingData, // Edit mode if no data exists
                    saveButtonState = if (hasExistingData) SaveButtonState.HIDDEN else SaveButtonState.SAVE
                )
            }
        }
    }
    
    fun setCurrentUserId(userId: String) {
        _uiState.value = _uiState.value.copy(currentUserId = userId)
        loadRate()
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

data class RateUiState(
    val selectedYearMonth: YearMonth = YearMonth.now(),
    val rate: Float = 0f,
    val rateText: String = "",
    val defaultQuantity: Float = 0f,
    val defaultQuantityText: String = "",
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val hasData: Boolean = false,
    val isEditMode: Boolean = true, // Start in edit mode for new data
    val saveButtonState: SaveButtonState = SaveButtonState.SAVE
) 