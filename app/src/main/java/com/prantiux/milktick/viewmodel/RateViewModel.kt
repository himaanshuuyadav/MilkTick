package com.prantiux.milktick.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prantiux.milktick.data.MonthlyRate
import com.prantiux.milktick.data.local.SyncState
import com.prantiux.milktick.repository.AppGraph
import com.prantiux.milktick.repository.MainRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

class RateViewModel : ViewModel() {
    private val firestoreRepository: MainRepository = AppGraph.mainRepository
    private var syncStateObserverJob: Job? = null
    
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
            val currentState = _uiState.value
            val loadingState = if (currentState.saveButtonState == SaveButtonState.UPDATE || currentState.hasData) {
                SaveButtonState.LOADING_UPDATE
            } else {
                SaveButtonState.LOADING
            }

            _uiState.value = _uiState.value.copy(saveButtonState = loadingState)
            
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
                    syncState = SyncState.PENDING_UPDATE,
                    saveButtonState = if (loadingState == SaveButtonState.LOADING_UPDATE) {
                        SaveButtonState.UPDATED
                    } else {
                        SaveButtonState.SAVED
                    },
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
                    saveButtonState = if (loadingState == SaveButtonState.LOADING_UPDATE) {
                        SaveButtonState.UPDATE
                    } else {
                        SaveButtonState.SAVE
                    }
                )
            }
        }
    }
    
    fun enableEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = true,
            saveButtonState = if (_uiState.value.hasData) SaveButtonState.UPDATE else SaveButtonState.SAVE
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
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    saveButtonState = SaveButtonState.HIDDEN
                )
                val rate = firestoreRepository.getMonthlyRate(userId, _uiState.value.selectedYearMonth)
                val hasExistingData = rate != null && rate.ratePerLiter > 0
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    rate = rate?.ratePerLiter ?: 0f,
                    rateText = if (rate?.ratePerLiter != null && rate.ratePerLiter > 0) rate.ratePerLiter.toString() else "",
                    defaultQuantity = rate?.defaultQuantity ?: 0f,
                    defaultQuantityText = if (rate?.defaultQuantity != null && rate.defaultQuantity > 0) rate.defaultQuantity.toString() else "",
                    hasData = hasExistingData,
                    isEditMode = !hasExistingData, // Edit mode if no data exists
                    saveButtonState = if (hasExistingData) SaveButtonState.HIDDEN else SaveButtonState.SAVE,
                    syncState = SyncState.SYNCED
                )

                observeRateSyncState(userId, _uiState.value.selectedYearMonth)
            }
        }
    }

    private fun observeRateSyncState(userId: String, yearMonth: YearMonth) {
        syncStateObserverJob?.cancel()
        syncStateObserverJob = viewModelScope.launch {
            firestoreRepository.observeRateSyncState(userId, yearMonth).collect { state ->
                _uiState.value = _uiState.value.copy(syncState = state ?: SyncState.SYNCED)
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
    val isLoading: Boolean = true,
    val message: String? = null,
    val hasData: Boolean = false,
    val isEditMode: Boolean = false,
    val saveButtonState: SaveButtonState = SaveButtonState.HIDDEN,
    val syncState: SyncState = SyncState.SYNCED
) 