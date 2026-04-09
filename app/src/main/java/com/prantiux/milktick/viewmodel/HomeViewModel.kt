package com.prantiux.milktick.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prantiux.milktick.data.MilkEntry
import com.prantiux.milktick.data.local.SyncState
import com.prantiux.milktick.repository.AppGraph
import com.prantiux.milktick.repository.MainRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class HomeViewModel : ViewModel() {
    private val firestoreRepository: MainRepository = AppGraph.mainRepository
    private var todayEntryObserverJob: Job? = null
    private var monthlyRateObserverJob: Job? = null
    private var syncStateObserverJob: Job? = null
    private var activeUserId: String? = null
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun updateBrought(brought: Boolean) {
        val currentState = _uiState.value
        
        // Only allow updates if no entry exists for today or in edit mode
        if (!currentState.hasEntryToday || currentState.isEditMode) {
            if (brought && currentState.defaultQuantity > 0 && currentState.quantityText.isEmpty()) {
                // When toggle is turned on and default quantity exists, use it
                _uiState.value = currentState.copy(
                    brought = brought,
                    quantity = currentState.defaultQuantity,
                    quantityText = currentState.defaultQuantity.toString(),
                    saveButtonState = resolveButtonState(currentState.copy(brought = brought))
                )
            } else {
                _uiState.value = currentState.copy(
                    brought = brought,
                    saveButtonState = resolveButtonState(currentState.copy(brought = brought))
                )
            }
        }
    }
    
    fun updateQuantity(quantity: String) {
        // Only allow updates if no entry exists for today or in edit mode
        if (!_uiState.value.hasEntryToday || _uiState.value.isEditMode) {
            val quantityFloat = quantity.toFloatOrNull() ?: 0f
            _uiState.value = _uiState.value.copy(quantity = quantityFloat, quantityText = quantity)
        }
    }
    
    fun updateNote(note: String) {
        // Only allow updates if no entry exists for today or in edit mode
        if (!_uiState.value.hasEntryToday || _uiState.value.isEditMode) {
            _uiState.value = _uiState.value.copy(note = note)
        }
    }
    
    fun saveMilkEntry(userId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val actionState = resolveButtonState(currentState)

            try {
                val loadingState = when (actionState) {
                    SaveButtonState.REMOVE, SaveButtonState.LOADING_REMOVE -> SaveButtonState.LOADING_REMOVE
                    SaveButtonState.UPDATE, SaveButtonState.LOADING_UPDATE -> SaveButtonState.LOADING_UPDATE
                    else -> SaveButtonState.LOADING
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    saveButtonState = loadingState
                )
                
                // If not brought and we're editing an existing entry, delete it
                if (!currentState.brought && currentState.hasEntryToday && currentState.isEditMode) {
                    val deleteResult = firestoreRepository.deleteMilkEntry(userId, LocalDate.now())
                    
                    if (deleteResult.isSuccess) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "Entry removed successfully",
                            saveButtonState = SaveButtonState.REMOVED,
                            hasEntryToday = false,
                            todayEntry = null,
                            isEditMode = false,
                            // Reset to default values
                            quantity = currentState.defaultQuantity,
                            quantityText = if (currentState.defaultQuantity > 0) currentState.defaultQuantity.toString() else "",
                            note = "",
                            brought = false,
                            syncState = SyncState.PENDING_DELETE
                        )
                        
                        // Hide button after 2 seconds and return to original state
                        delay(2000)
                        _uiState.value = _uiState.value.copy(saveButtonState = SaveButtonState.HIDDEN)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "Error removing entry: ${deleteResult.exceptionOrNull()?.message}",
                            saveButtonState = resolveFailureState(currentState)
                        )
                    }
                    return@launch
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Unexpected error: ${e.message}",
                    saveButtonState = resolveFailureState(_uiState.value)
                )
                return@launch
            }
            
            // Continue with regular save logic
            val entry = MilkEntry(
                date = LocalDate.now(),
                quantity = _uiState.value.quantity,
                brought = _uiState.value.brought,
                note = _uiState.value.note.takeIf { it.isNotBlank() },
                userId = userId
            )
            
            val result = firestoreRepository.saveMilkEntry(entry)
            
            if (result.isSuccess) {
                val successState = when (actionState) {
                    SaveButtonState.UPDATE, SaveButtonState.LOADING_UPDATE -> SaveButtonState.UPDATED
                    else -> SaveButtonState.SAVED
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Data saved successfully",
                    saveButtonState = successState,
                    hasEntryToday = true,
                    todayEntry = entry,
                    isEditMode = false,
                    syncState = SyncState.PENDING_UPDATE
                )
                
                // Hide saved button after 2 seconds and switch to display mode
                delay(2000)
                _uiState.value = _uiState.value.copy(saveButtonState = SaveButtonState.HIDDEN)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Error saving data",
                    saveButtonState = resolveFailureState(currentState)
                )
            }
        }
    }
    
    private fun resetForm() {
        val currentDefaultQuantity = _uiState.value.defaultQuantity
        val defaultQuantityText = if (currentDefaultQuantity > 0) currentDefaultQuantity.toString() else ""
        _uiState.value = HomeUiState(
            defaultQuantity = currentDefaultQuantity,
            quantity = currentDefaultQuantity,
            quantityText = defaultQuantityText
        )
    }
    
    fun loadDefaultQuantity(userId: String) {
        if (activeUserId == userId &&
            todayEntryObserverJob?.isActive == true &&
            monthlyRateObserverJob?.isActive == true &&
            syncStateObserverJob?.isActive == true
        ) {
            return
        }

        activeUserId = userId

        viewModelScope.launch {
            val shouldShowBlockingLoader = _uiState.value.todayEntry == null && _uiState.value.quantityText.isBlank()
            if (shouldShowBlockingLoader) {
                _uiState.value = _uiState.value.copy(isCheckingEntry = true)
            }
            
            val monthlyRate = firestoreRepository.getMonthlyRate(userId, YearMonth.now())
            val defaultQuantity = monthlyRate?.defaultQuantity ?: 0f
            val quantityText = if (defaultQuantity > 0) defaultQuantity.toString() else ""
            
            // Also load today's entry
            val todayEntry = firestoreRepository.getMilkEntryForDate(userId, LocalDate.now())
            
            if (todayEntry != null) {
                // Entry exists for today - show in display mode
                _uiState.value = _uiState.value.copy(
                    defaultQuantity = defaultQuantity,
                    hasEntryToday = true,
                    todayEntry = todayEntry,
                    brought = todayEntry.brought,
                    quantity = todayEntry.quantity,
                    quantityText = todayEntry.quantity.toString(),
                    note = todayEntry.note ?: "",
                    saveButtonState = SaveButtonState.HIDDEN,
                    isEditMode = false,
                    isCheckingEntry = false,
                    syncState = SyncState.SYNCED
                )
            } else {
                // No entry for today - show input mode
                _uiState.value = _uiState.value.copy(
                    defaultQuantity = defaultQuantity,
                    quantity = defaultQuantity,
                    quantityText = quantityText,
                    hasEntryToday = false,
                    todayEntry = null,
                    saveButtonState = SaveButtonState.SAVE,
                    isEditMode = false,
                    isCheckingEntry = false,
                    syncState = SyncState.SYNCED
                )
            }

            observeTodayEntry(userId)
            observeMonthlyRate(userId)
            observeGlobalSyncState(userId)
        }
    }

    private fun observeTodayEntry(userId: String) {
        todayEntryObserverJob?.cancel()
        todayEntryObserverJob = viewModelScope.launch {
            firestoreRepository.observeEntryForDate(userId, LocalDate.now()).collect { entry ->
                val currentState = _uiState.value
                if (entry != null) {
                    _uiState.value = currentState.copy(
                        hasEntryToday = true,
                        todayEntry = entry,
                        brought = entry.brought,
                        quantity = entry.quantity,
                        quantityText = entry.quantity.toString(),
                        note = entry.note ?: "",
                        isEditMode = false,
                        isCheckingEntry = false,
                        saveButtonState = SaveButtonState.HIDDEN
                    )
                } else {
                    val defaultQuantityText = if (currentState.defaultQuantity > 0) currentState.defaultQuantity.toString() else ""
                    _uiState.value = currentState.copy(
                        hasEntryToday = false,
                        todayEntry = null,
                        brought = false,
                        quantity = currentState.defaultQuantity,
                        quantityText = defaultQuantityText,
                        note = "",
                        isEditMode = false,
                        isCheckingEntry = false,
                        saveButtonState = SaveButtonState.SAVE
                    )
                }
            }
        }
    }

    private fun observeMonthlyRate(userId: String) {
        monthlyRateObserverJob?.cancel()
        monthlyRateObserverJob = viewModelScope.launch {
            firestoreRepository.observeMonthlyRate(userId, YearMonth.now()).collect { rate ->
                val currentState = _uiState.value
                val defaultQuantity = rate?.defaultQuantity ?: 0f
                val shouldRefreshInputs = !currentState.hasEntryToday && !currentState.isEditMode
                _uiState.value = currentState.copy(
                    defaultQuantity = defaultQuantity,
                    quantity = if (shouldRefreshInputs) defaultQuantity else currentState.quantity,
                    quantityText = if (shouldRefreshInputs) {
                        if (defaultQuantity > 0) defaultQuantity.toString() else ""
                    } else {
                        currentState.quantityText
                    }
                )
            }
        }
    }

    private fun observeGlobalSyncState(userId: String) {
        syncStateObserverJob?.cancel()
        syncStateObserverJob = viewModelScope.launch {
            firestoreRepository.observeGlobalSyncState(userId).collect { state ->
                _uiState.value = _uiState.value.copy(syncState = state)
            }
        }
    }
    
    fun toggleEditMode() {
        val currentState = _uiState.value
        if (currentState.isEditMode) {
            val savedEntry = currentState.todayEntry
            _uiState.value = if (savedEntry != null) {
                currentState.copy(
                    isEditMode = false,
                    brought = savedEntry.brought,
                    quantity = savedEntry.quantity,
                    quantityText = savedEntry.quantity.toString(),
                    note = savedEntry.note ?: "",
                    saveButtonState = SaveButtonState.HIDDEN
                )
            } else {
                val defaultQuantityText = if (currentState.defaultQuantity > 0) currentState.defaultQuantity.toString() else ""
                currentState.copy(
                    isEditMode = false,
                    brought = false,
                    quantity = currentState.defaultQuantity,
                    quantityText = defaultQuantityText,
                    note = "",
                    saveButtonState = SaveButtonState.HIDDEN
                )
            }
            return
        }

        _uiState.value = currentState.copy(
            isEditMode = true,
            saveButtonState = resolveButtonState(currentState.copy(isEditMode = true))
        )
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun retrySync() {
        viewModelScope.launch {
            firestoreRepository.triggerSync()
            _uiState.value = _uiState.value.copy(
                syncState = SyncState.PENDING_UPDATE
            )
        }
    }

    private fun resolveButtonState(state: HomeUiState): SaveButtonState {
        return when {
            !state.isEditMode && state.hasEntryToday -> SaveButtonState.HIDDEN
            state.isEditMode && state.hasEntryToday && !state.brought -> SaveButtonState.REMOVE
            state.isEditMode && state.hasEntryToday -> SaveButtonState.UPDATE
            else -> SaveButtonState.SAVE
        }
    }

    private fun resolveFailureState(state: HomeUiState): SaveButtonState {
        return when {
            state.hasEntryToday && state.isEditMode && !state.brought -> SaveButtonState.REMOVE
            state.hasEntryToday && state.isEditMode -> SaveButtonState.UPDATE
            else -> SaveButtonState.SAVE
        }
    }
}

data class HomeUiState(
    val brought: Boolean = false,
    val quantity: Float = 0f,
    val quantityText: String = "",
    val defaultQuantity: Float = 0f,
    val note: String = "",
    val isLoading: Boolean = false,
    val isCheckingEntry: Boolean = false, // Loading state for checking existing entry
    val message: String? = null,
    val shouldReset: Boolean = false,
    val saveButtonState: SaveButtonState = SaveButtonState.SAVE,
    val hasEntryToday: Boolean = false,
    val isEditMode: Boolean = false,
    val todayEntry: MilkEntry? = null,
    val syncState: SyncState = SyncState.SYNCED
) 