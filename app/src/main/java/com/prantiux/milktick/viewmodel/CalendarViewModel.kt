package com.prantiux.milktick.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prantiux.milktick.data.MilkEntry
import com.prantiux.milktick.data.PaymentRecord
import com.prantiux.milktick.data.local.SyncState
import com.prantiux.milktick.repository.AppGraph
import com.prantiux.milktick.repository.MainRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth

data class EntryDetail(
    val quantity: Float,
    val note: String?
)

enum class PaymentAction {
    ADD_PAYMENT,
    ADD_PREVIOUS_DUE
}

data class CalendarUiState(
    val isLoading: Boolean = false,
    val datesWithEntries: Set<LocalDate> = emptySet(),
    val datesWithNoDelivery: Set<LocalDate> = emptySet(),
    val entryDetailsMap: Map<LocalDate, EntryDetail> = emptyMap(),
    val totalDays: Int = 0,
    val totalLiters: Double = 0.0,
    val totalCost: Double = 0.0,
    val monthDueAmount: Double = 0.0,
    val monthClearedFromLater: Double = 0.0,
    val amountPaid: Double = 0.0,
    val carryDueAmount: Double = 0.0,
    val outstandingAmount: Double = 0.0,
    val advanceCredit: Double = 0.0,
    val dueClearedIn: YearMonth? = null,
    val paymentRecords: List<PaymentRecord> = emptyList(),
    val paymentInputAmount: String = "",
    val paymentInputNote: String = "",
    val isSavingPayment: Boolean = false,
    val activePaymentAction: PaymentAction? = null,
    val defaultQuantity: Float = 0f,
    val monthSyncState: SyncState = SyncState.SYNCED,
    val loadedYearMonth: YearMonth? = null,
    val error: String? = null
)

class CalendarViewModel(
    private val repository: MainRepository = AppGraph.mainRepository
) : ViewModel() {

    private data class MonthBucket(
        val yearMonth: YearMonth,
        var due: Double,
        var paid: Double = 0.0,
        var paidFromLater: Double = 0.0,
        var clearedIn: YearMonth? = null
    )
    
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()
    
    private var currentUserId: String = ""
    private var currentYearMonth: YearMonth? = null
    private var monthDataObserverJob: Job? = null
    private var monthSyncObserverJob: Job? = null
    
    fun loadMonthData(userId: String, yearMonth: YearMonth, forceReload: Boolean = false) {
        val isSameRequest = currentUserId == userId && currentYearMonth == yearMonth
        if (!forceReload && isSameRequest && monthDataObserverJob?.isActive == true) {
            return
        }

        currentUserId = userId
        currentYearMonth = yearMonth

        val shouldShowBlockingLoader = _uiState.value.loadedYearMonth != yearMonth
        if (shouldShowBlockingLoader) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        }

        monthDataObserverJob?.cancel()
        monthDataObserverJob = viewModelScope.launch {
            repository.getMilkEntriesForMonth(userId, yearMonth).collectLatest { entries ->
                try {
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
                
                val paymentRecords = repository.getPaymentRecordsForMonth(userId, yearMonth)
                val allRecords = repository.getAllPaymentRecordsForUser(userId)
                val entryMonths = repository.getAllEntryYearMonths(userId)

                val monthSet = (entryMonths + allRecords.map { it.appliedYearMonth } + listOf(yearMonth)).toSortedSet()
                val buckets = monthSet.associateWith { month ->
                    MonthBucket(
                        yearMonth = month,
                        due = repository.getMonthlyCharge(userId, month)
                    )
                }.toMutableMap()

                // Negative adjustments increase due of the applied month.
                allRecords.filter { it.amount < 0 }.forEach { record ->
                    val bucket = buckets[record.appliedYearMonth] ?: return@forEach
                    bucket.due += kotlin.math.abs(record.amount)
                }

                // Positive payments clear oldest month dues first (FIFO).
                allRecords.filter { it.amount > 0 }.forEach { record ->
                    var remaining = record.amount
                    for (month in monthSet) {
                        if (remaining <= 0.0) break
                        val bucket = buckets[month] ?: continue
                        val monthRemaining = (bucket.due - bucket.paid).coerceAtLeast(0.0)
                        if (monthRemaining <= 0.0) continue

                        val applied = minOf(remaining, monthRemaining)
                        bucket.paid += applied
                        if (record.appliedYearMonth > month) {
                            bucket.paidFromLater += applied
                        }
                        remaining -= applied

                        if ((bucket.due - bucket.paid) <= 0.0001 && bucket.clearedIn == null) {
                            bucket.clearedIn = record.appliedYearMonth
                        }
                    }
                }

                val target = buckets[yearMonth] ?: MonthBucket(yearMonth = yearMonth, due = totalCost)
                // "Settled" should reflect transactions recorded in this month,
                // independent of FIFO due allocation across historical buckets.
                val monthPaid = paymentRecords.filter { it.amount > 0.0 }.sumOf { it.amount }
                val monthOutstanding = (target.due - target.paid).coerceAtLeast(0.0)

                val totalCostUntilMonth = repository.getTotalCostUntil(userId, yearMonth)
                val totalPaymentsUntilMonth = repository.getTotalPaymentsUntil(userId, yearMonth)
                val carryDueAmount = (totalCostUntilMonth - totalPaymentsUntilMonth).coerceAtLeast(0.0)

                val totalPaidAllMonths = buckets.values.sumOf { it.paid }
                val globalCredit = (allRecords.filter { it.amount > 0 }.sumOf { it.amount } - totalPaidAllMonths).coerceAtLeast(0.0)
                
                _uiState.value = CalendarUiState(
                    isLoading = false,
                    datesWithEntries = datesWithEntries,
                    datesWithNoDelivery = datesWithNoDelivery,
                    entryDetailsMap = entryDetailsMap,
                    totalDays = broughtDays,
                    totalLiters = totalQuantity,
                    defaultQuantity = defaultQuantity,
                    totalCost = totalCost,
                    monthDueAmount = target.due,
                    monthClearedFromLater = target.paidFromLater,
                    amountPaid = monthPaid,
                    carryDueAmount = carryDueAmount,
                    outstandingAmount = monthOutstanding,
                    advanceCredit = globalCredit,
                    dueClearedIn = target.clearedIn,
                    paymentRecords = paymentRecords,
                    monthSyncState = _uiState.value.monthSyncState,
                    loadedYearMonth = yearMonth
                )

                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }

        observeMonthSyncState(userId, yearMonth)
    }

    private fun observeMonthSyncState(userId: String, yearMonth: YearMonth) {
        monthSyncObserverJob?.cancel()
        monthSyncObserverJob = viewModelScope.launch {
            repository.observeMonthSyncState(userId, yearMonth).collect { state ->
                _uiState.value = _uiState.value.copy(monthSyncState = state ?: SyncState.SYNCED)
            }
        }
    }
    
    fun updatePaymentInput(amount: String) {
        _uiState.value = _uiState.value.copy(paymentInputAmount = amount)
    }

    fun updatePaymentNote(note: String) {
        _uiState.value = _uiState.value.copy(paymentInputNote = note)
    }

    fun savePaymentRecord(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val yearMonth = currentYearMonth ?: return@launch
            val amount = _uiState.value.paymentInputAmount.toDoubleOrNull() ?: 0.0
            if (amount <= 0.0) {
                onComplete()
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isSavingPayment = true,
                activePaymentAction = PaymentAction.ADD_PAYMENT,
                monthSyncState = SyncState.PENDING_UPDATE
            )
            delay(1200)
            repository.addPaymentRecord(
                userId = currentUserId,
                yearMonth = yearMonth,
                amount = amount,
                note = _uiState.value.paymentInputNote
            )

            loadMonthData(currentUserId, yearMonth, forceReload = true)
            _uiState.value = _uiState.value.copy(
                isSavingPayment = false,
                activePaymentAction = null,
                paymentInputAmount = "",
                paymentInputNote = ""
            )
            onComplete()
        }
    }

    fun savePreviousDueAdjustment(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val yearMonth = currentYearMonth ?: return@launch
            val amount = _uiState.value.paymentInputAmount.toDoubleOrNull() ?: 0.0
            if (amount <= 0.0) {
                onComplete()
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isSavingPayment = true,
                activePaymentAction = PaymentAction.ADD_PREVIOUS_DUE,
                monthSyncState = SyncState.PENDING_UPDATE
            )
            delay(1200)
            repository.addPreviousDueAdjustment(
                userId = currentUserId,
                yearMonth = yearMonth,
                amount = amount,
                note = _uiState.value.paymentInputNote
            )

            loadMonthData(currentUserId, yearMonth, forceReload = true)
            _uiState.value = _uiState.value.copy(
                isSavingPayment = false,
                activePaymentAction = null,
                paymentInputAmount = "",
                paymentInputNote = ""
            )
            onComplete()
        }
    }

    fun deletePaymentRecord(recordId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val yearMonth = currentYearMonth ?: return@launch
            _uiState.value = _uiState.value.copy(
                isSavingPayment = true,
                activePaymentAction = null,
                monthSyncState = SyncState.PENDING_UPDATE
            )
            repository.deletePaymentRecord(recordId)
            loadMonthData(currentUserId, yearMonth, forceReload = true)
            _uiState.value = _uiState.value.copy(isSavingPayment = false, activePaymentAction = null)
            onComplete()
        }
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

                // Force refresh derived totals immediately for edited historical dates.
                loadMonthData(currentUserId, YearMonth.from(date))

                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
                onComplete()
            }
        }
    }
}
