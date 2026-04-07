package com.prantiux.milktick.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prantiux.milktick.data.MilkEntry
import com.prantiux.milktick.data.MonthlyPayment
import com.prantiux.milktick.data.local.SyncState
import com.prantiux.milktick.repository.AppGraph
import com.prantiux.milktick.repository.MainRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class EntryDetail(
    val quantity: Float,
    val note: String?
)

data class CalendarUiState(
    val isLoading: Boolean = false,
    val datesWithEntries: Set<LocalDate> = emptySet(),
    val datesWithNoDelivery: Set<LocalDate> = emptySet(),
    val entryDetailsMap: Map<LocalDate, EntryDetail> = emptyMap(),
    val totalDays: Int = 0,
    val totalLiters: Double = 0.0,
    val totalCost: Double = 0.0,
    val isPaid: Boolean = false,
    val paymentNote: String = "",
    val isEditingPayment: Boolean = false,
    val defaultQuantity: Float = 0f,
    val monthSyncState: SyncState = SyncState.SYNCED,
    val error: String? = null
)

class CalendarViewModel(
    private val repository: MainRepository = AppGraph.mainRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()
    
    private var currentUserId: String = ""
    private var currentYearMonth: YearMonth? = null
    private var monthSyncObserverJob: Job? = null
    
    fun loadMonthData(userId: String, yearMonth: YearMonth) {
        currentUserId = userId
        currentYearMonth = yearMonth
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val entries = repository.getMilkEntriesForMonthSync(userId, yearMonth)
                val datesWithEntries = entries.filter { it.brought }.map { it.date }.toSet()
                val datesWithNoDelivery = entries.filter { !it.brought }.map { it.date }.toSet()
                val totalQuantity = entries.filter { it.brought }.sumOf { it.quantity.toDouble() }
                val broughtDays = entries.count { it.brought }
                
                // Create entry details map
                val entryDetailsMap = entries.associate { entry ->
                    entry.date to EntryDetail(
                        quantity = entry.quantity,
                        note = entry.note
                    )
                }
                
                // Get rate for this month
                val rate = repository.getMonthlyRate(userId, yearMonth)
                val ratePerLiter = rate?.ratePerLiter ?: 0f
                val defaultQuantity = rate?.defaultQuantity ?: 0f
                val totalCost = totalQuantity * ratePerLiter
                
                // Load payment status
                val payment = repository.getMonthlyPayment(userId, yearMonth)
                
                _uiState.value = CalendarUiState(
                    isLoading = false,
                    datesWithEntries = datesWithEntries,
                    datesWithNoDelivery = datesWithNoDelivery,
                    entryDetailsMap = entryDetailsMap,
                    totalDays = broughtDays,
                    totalLiters = totalQuantity,
                    defaultQuantity = defaultQuantity,
                    totalCost = totalCost,
                    isPaid = payment?.isPaid ?: false,
                    paymentNote = payment?.paymentNote ?: "",
                    isEditingPayment = false,
                    monthSyncState = SyncState.SYNCED
                )

                observeMonthSyncState(userId, yearMonth)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun observeMonthSyncState(userId: String, yearMonth: YearMonth) {
        monthSyncObserverJob?.cancel()
        monthSyncObserverJob = viewModelScope.launch {
            repository.observeMonthSyncState(userId, yearMonth).collect { state ->
                _uiState.value = _uiState.value.copy(monthSyncState = state ?: SyncState.SYNCED)
            }
        }
    }
    
    fun updatePaymentStatus(isPaid: Boolean) {
        viewModelScope.launch {
            val yearMonth = currentYearMonth ?: return@launch
            _uiState.value = _uiState.value.copy(isPaid = isPaid)
            
            val payment = MonthlyPayment(
                yearMonth = yearMonth,
                userId = currentUserId,
                isPaid = isPaid,
                paymentNote = _uiState.value.paymentNote,
                paidDate = if (isPaid) System.currentTimeMillis() else null
            )
            _uiState.value = _uiState.value.copy(monthSyncState = SyncState.PENDING_UPDATE)
            repository.saveMonthlyPayment(payment)
        }
    }
    
    fun updatePaymentNote(note: String) {
        _uiState.value = _uiState.value.copy(paymentNote = note)
    }
    
    fun savePaymentNote() {
        viewModelScope.launch {
            val yearMonth = currentYearMonth ?: return@launch
            val payment = MonthlyPayment(
                yearMonth = yearMonth,
                userId = currentUserId,
                isPaid = _uiState.value.isPaid,
                paymentNote = _uiState.value.paymentNote,
                paidDate = if (_uiState.value.isPaid) System.currentTimeMillis() else null
            )
            _uiState.value = _uiState.value.copy(monthSyncState = SyncState.PENDING_UPDATE)
            repository.saveMonthlyPayment(payment)
            _uiState.value = _uiState.value.copy(isEditingPayment = false)
        }
    }
    
    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(isEditingPayment = !_uiState.value.isEditingPayment)
    }
    
    fun updateMilkEntry(
        date: LocalDate,
        quantity: Float,
        brought: Boolean,
        note: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val entry = MilkEntry(
                    date = date,
                    quantity = quantity,
                    brought = brought,
                    note = note,
                    userId = currentUserId
                )
                _uiState.value = _uiState.value.copy(monthSyncState = SyncState.PENDING_UPDATE)
                repository.saveMilkEntry(entry)
                
                // Reload data for the current month
                currentYearMonth?.let { yearMonth ->
                    loadMonthData(currentUserId, yearMonth)
                }
                
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
                onComplete()
            }
        }
    }
}
