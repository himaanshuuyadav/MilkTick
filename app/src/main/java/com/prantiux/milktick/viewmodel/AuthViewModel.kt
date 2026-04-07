package com.prantiux.milktick.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.prantiux.milktick.repository.AppGraph
import com.prantiux.milktick.repository.MainRepository
import com.prantiux.milktick.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val mainRepository: MainRepository = AppGraph.mainRepository
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            authRepository.getAuthStateFlow().collect { user ->
                _currentUser.value = user
                if (user != null) {
                    if (mainRepository.shouldRunInitialSync(user.uid)) {
                        mainRepository.performInitialSyncIfNeeded(user.uid)
                    }
                }
                _authState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signIn(email, password)
            _authState.value = if (result.isSuccess) {
                val user = result.getOrNull()!!
                if (mainRepository.shouldRunInitialSync(user.uid)) {
                    mainRepository.performInitialSyncIfNeeded(user.uid)
                }
                AuthState.Authenticated(user)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Sign in failed")
            }
        }
    }
    
    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signUp(email, password, name)
            _authState.value = if (result.isSuccess) {
                val user = result.getOrNull()!!
                if (mainRepository.shouldRunInitialSync(user.uid)) {
                    mainRepository.performInitialSyncIfNeeded(user.uid)
                }
                AuthState.Authenticated(user)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Sign up failed")
            }
        }
    }
    
    fun signOut() {
        authRepository.signOut()
    }

    fun updateProfile(name: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.updateProfile(name)
            onResult(result.isSuccess)
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
} 