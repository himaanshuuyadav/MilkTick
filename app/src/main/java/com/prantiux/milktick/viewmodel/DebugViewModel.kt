package com.prantiux.milktick.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prantiux.milktick.data.MilkEntry
import com.prantiux.milktick.data.MonthlyRate
import com.prantiux.milktick.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class DebugViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()
    
    private val _debugState = MutableStateFlow(DebugUiState())
    val debugState: StateFlow<DebugUiState> = _debugState.asStateFlow()
    
    fun testFirestoreConnection() {
        Log.d("DebugViewModel", "testFirestoreConnection called")
        viewModelScope.launch {
            _debugState.value = _debugState.value.copy(isLoading = true, message = "Testing connection...")
            Log.d("DebugViewModel", "Starting connection test")
            
            val result = firestoreRepository.testFirestoreConnection()
            val message = if (result.isSuccess) {
                "✅ ${result.getOrNull()}"
            } else {
                val error = result.exceptionOrNull()
                when {
                    error?.message?.contains("PERMISSION_DENIED") == true -> {
                        "❌ PERMISSION DENIED: You need to update Firebase security rules.\n\nGo to Firebase Console → Firestore → Rules and use the rules from firebase-rules.txt file in your project."
                    }
                    error?.message?.contains("User not authenticated") == true -> {
                        "❌ USER NOT AUTHENTICATED: Please log out and log back in."
                    }
                    else -> {
                        "❌ Connection failed: ${error?.message}"
                    }
                }
            }
            
            Log.d("DebugViewModel", "Connection test result: $message")
            _debugState.value = _debugState.value.copy(
                isLoading = false,
                message = message
            )
        }
    }
    
    fun testAuthenticationStatus() {
        Log.d("DebugViewModel", "testAuthenticationStatus called")
        viewModelScope.launch {
            _debugState.value = _debugState.value.copy(isLoading = true, message = "Checking authentication...")
            
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                
                val message = if (currentUser != null) {
                    "✅ AUTHENTICATED\n" +
                    "User ID: ${currentUser.uid}\n" +
                    "Email: ${currentUser.email}\n" +
                    "Email Verified: ${currentUser.isEmailVerified}\n" +
                    "Provider: ${currentUser.providerData.firstOrNull()?.providerId ?: "unknown"}"
                } else {
                    "❌ NOT AUTHENTICATED\nPlease log out and log back in."
                }
                
                _debugState.value = _debugState.value.copy(
                    isLoading = false,
                    message = message
                )
            } catch (e: Exception) {
                _debugState.value = _debugState.value.copy(
                    isLoading = false,
                    message = "❌ Auth check failed: ${e.message}"
                )
            }
        }
    }
    
    fun testSaveMilkEntry(userId: String) {
        Log.d("DebugViewModel", "testSaveMilkEntry called for user: $userId")
        viewModelScope.launch {
            _debugState.value = _debugState.value.copy(isLoading = true, message = "Testing milk entry save...")
            
            val testEntry = MilkEntry(
                date = LocalDate.now(),
                quantity = 2.5f,
                brought = true,
                note = "Debug test entry",
                userId = userId
            )
            
            Log.d("DebugViewModel", "Saving test entry: $testEntry")
            val result = firestoreRepository.saveMilkEntry(testEntry)
            val message = if (result.isSuccess) {
                "✅ Test milk entry saved successfully"
            } else {
                "❌ Milk entry save failed: ${result.exceptionOrNull()?.message}"
            }
            
            Log.d("DebugViewModel", "Milk entry test result: $message")
            _debugState.value = _debugState.value.copy(
                isLoading = false,
                message = message
            )
        }
    }
    
    fun testSaveMonthlyRate(userId: String) {
        viewModelScope.launch {
            _debugState.value = _debugState.value.copy(isLoading = true, message = "Testing monthly rate save...")
            
            val testRate = MonthlyRate(
                yearMonth = YearMonth.now(),
                ratePerLiter = 60.0f,
                defaultQuantity = 2.0f,
                userId = userId
            )
            
            val result = firestoreRepository.saveMonthlyRate(testRate)
            _debugState.value = _debugState.value.copy(
                isLoading = false,
                message = if (result.isSuccess) {
                    "✅ Test monthly rate saved successfully"
                } else {
                    "❌ Monthly rate save failed: ${result.exceptionOrNull()?.message}"
                }
            )
        }
    }
    
    fun testLoadData(userId: String) {
        viewModelScope.launch {
            _debugState.value = _debugState.value.copy(isLoading = true, message = "Testing data load...")
            
            try {
                val monthlyRate = firestoreRepository.getMonthlyRate(userId, YearMonth.now())
                val message = if (monthlyRate != null) {
                    "✅ Monthly rate loaded: ₹${monthlyRate.ratePerLiter}/L, Default: ${monthlyRate.defaultQuantity}L"
                } else {
                    "⚠️ No monthly rate found for current month"
                }
                
                _debugState.value = _debugState.value.copy(
                    isLoading = false,
                    message = message
                )
            } catch (e: Exception) {
                _debugState.value = _debugState.value.copy(
                    isLoading = false,
                    message = "❌ Data load failed: ${e.message}"
                )
            }
        }
    }
    
    fun testDeletePermissions(userId: String) {
        Log.d("DebugViewModel", "testDeletePermissions called for user: $userId")
        viewModelScope.launch {
            _debugState.value = _debugState.value.copy(isLoading = true, message = "Testing delete permissions...")
            
            try {
                // First, create a test entry to delete
                val testEntry = MilkEntry(
                    date = LocalDate.now().minusDays(1), // Use yesterday to avoid conflicts
                    quantity = 1.0f,
                    brought = true,
                    note = "Test entry for delete permissions",
                    userId = userId
                )
                
                Log.d("DebugViewModel", "Creating test entry for deletion test...")
                val createResult = firestoreRepository.saveMilkEntry(testEntry)
                
                if (createResult.isSuccess) {
                    Log.d("DebugViewModel", "✅ Test entry created, now testing delete...")
                    
                    // Add a small delay to ensure the document is fully written
                    kotlinx.coroutines.delay(1000)
                    
                    // Log the exact document ID that will be deleted
                    val testDate = LocalDate.now().minusDays(1)
                    val documentId = "${userId}_${testDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
                    Log.d("DebugViewModel", "Attempting to delete document ID: $documentId")
                    Log.d("DebugViewModel", "Collection: milkEntries")
                    Log.d("DebugViewModel", "User ID: $userId")
                    Log.d("DebugViewModel", "Date: $testDate")
                    
                    // Now try to delete it
                    val deleteResult = firestoreRepository.deleteMilkEntry(userId, testDate)
                    
                    val message = if (deleteResult.isSuccess) {
                        "✅ DELETE PERMISSIONS WORKING!\n\nSuccessfully created and deleted a test entry. Your Firestore security rules allow delete operations."
                    } else {
                        val error = deleteResult.exceptionOrNull()
                        when {
                            error?.message?.contains("PERMISSION_DENIED") == true -> {
                                "❌ DELETE PERMISSION DENIED!\n\n" +
                                "Your Firestore security rules are blocking delete operations.\n\n" +
                                "To fix this, update your Firestore rules in Firebase Console:\n" +
                                "1. Go to Firebase Console\n" +
                                "2. Select Firestore Database\n" +
                                "3. Go to Rules tab\n" +
                                "4. Add delete permission for authenticated users\n\n" +
                                "Example rule:\n" +
                                "allow delete: if request.auth != null && resource.data.userId == request.auth.uid;"
                            }
                            error?.message?.contains("UNAUTHENTICATED") == true -> {
                                "❌ UNAUTHENTICATED: User authentication failed during delete"
                            }
                            else -> {
                                "❌ Delete failed with unknown error: ${error?.message}"
                            }
                        }
                    }
                    
                    _debugState.value = _debugState.value.copy(
                        isLoading = false,
                        message = message
                    )
                } else {
                    _debugState.value = _debugState.value.copy(
                        isLoading = false,
                        message = "❌ Could not create test entry for delete test: ${createResult.exceptionOrNull()?.message}"
                    )
                }
                
            } catch (e: Exception) {
                _debugState.value = _debugState.value.copy(
                    isLoading = false,
                    message = "❌ Delete permissions test failed: ${e.message}"
                )
            }
        }
    }
    
    fun clearDebugMessage() {
        _debugState.value = _debugState.value.copy(message = null)
    }
}

data class DebugUiState(
    val isLoading: Boolean = false,
    val message: String? = null
)
