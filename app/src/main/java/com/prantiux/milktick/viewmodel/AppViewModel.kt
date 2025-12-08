package com.prantiux.milktick.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Shared ViewModel to manage app initialization and user session
 * This ensures data is loaded only once when the app starts
 */
class AppViewModel : ViewModel() {
    private val _isAppInitialized = MutableStateFlow(false)
    val isAppInitialized: StateFlow<Boolean> = _isAppInitialized.asStateFlow()
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()
    
    private val _dataRefreshTrigger = MutableStateFlow(0L)
    val dataRefreshTrigger: StateFlow<Long> = _dataRefreshTrigger.asStateFlow()
    
    fun initializeApp(userId: String) {
        if (!_isAppInitialized.value) {
            _currentUserId.value = userId
            _isAppInitialized.value = true
        }
    }
    
    fun resetApp() {
        _isAppInitialized.value = false
        _currentUserId.value = null
        _dataRefreshTrigger.value = 0L
    }
    
    fun updateCurrentUser(userId: String?) {
        // Only update if user actually changed
        if (_currentUserId.value != userId) {
            _currentUserId.value = userId
            if (userId != null) {
                _isAppInitialized.value = true
            } else {
                resetApp()
            }
        }
    }
    
    fun triggerDataRefresh() {
        val newValue = System.currentTimeMillis()
        android.util.Log.d("AppViewModel", "triggerDataRefresh called - new value: $newValue")
        _dataRefreshTrigger.value = newValue
    }
}
