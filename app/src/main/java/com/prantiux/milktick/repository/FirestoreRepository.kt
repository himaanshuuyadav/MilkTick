package com.prantiux.milktick.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.prantiux.milktick.data.MilkEntry
import com.prantiux.milktick.data.MonthlyPayment
import com.prantiux.milktick.data.MonthlyRate
import com.prantiux.milktick.data.MonthlySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class FirestoreRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    // Milk Entries
    suspend fun saveMilkEntry(entry: MilkEntry): Result<Unit> {
        return try {
            Log.d("FirestoreRepo", "Attempting to save milk entry: $entry")
            
            val data = mapOf(
                "date" to entry.date.format(dateFormatter),
                "quantity" to entry.quantity,
                "brought" to entry.brought,
                "note" to entry.note,
                "userId" to entry.userId,
                "timestamp" to System.currentTimeMillis(),
                "yearMonth" to entry.date.format(yearMonthFormatter) // Add for easier querying
            )
            
            // Use hierarchical structure: users/{userId}/entries/{date}
            val docId = entry.date.format(dateFormatter)
            Log.d("FirestoreRepo", "Saving entry to path: users/${entry.userId}/entries/$docId with data: $data")
            
            db.collection("users")
                .document(entry.userId)
                .collection("entries")
                .document(docId)
                .set(data)
                .await()
                
            Log.d("FirestoreRepo", "Successfully saved milk entry")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error saving milk entry", e)
            Result.failure(e)
        }
    }

    suspend fun getMilkEntryForDate(userId: String, date: LocalDate): MilkEntry? {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("entries")
                .whereEqualTo("date", date.format(dateFormatter))
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val doc = snapshot.documents.first()
                MilkEntry(
                    date = LocalDate.parse(doc.getString("date") ?: "", dateFormatter),
                    quantity = doc.getDouble("quantity")?.toFloat() ?: 0f,
                    brought = doc.getBoolean("brought") ?: false,
                    note = doc.getString("note"),
                    userId = doc.getString("userId") ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error fetching milk entry for date", e)
            null
        }
    }

    fun getMilkEntriesForMonth(userId: String, yearMonth: YearMonth): Flow<List<MilkEntry>> = flow {
        try {
            val startDate = yearMonth.atDay(1)
            val endDate = yearMonth.atEndOfMonth()

            // Use new hierarchical structure
            val snapshot = db.collection("users")
                .document(userId)
                .collection("entries")
                .whereGreaterThanOrEqualTo("date", startDate.format(dateFormatter))
                .whereLessThanOrEqualTo("date", endDate.format(dateFormatter))
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            val entries = snapshot.documents.mapNotNull { doc ->
                try {
                    MilkEntry(
                        date = LocalDate.parse(doc.getString("date") ?: "", dateFormatter),
                        quantity = doc.getDouble("quantity")?.toFloat() ?: 0f,
                        brought = doc.getBoolean("brought") ?: false,
                        note = doc.getString("note"),
                        userId = doc.getString("userId") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("FirestoreRepo", "Error parsing milk entry", e)
                    null
                }
            }
            emit(entries)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error fetching milk entries", e)
            emit(emptyList())
        }
    }
    
    suspend fun getMilkEntriesForMonthSync(userId: String, yearMonth: YearMonth): List<MilkEntry> {
        return try {
            val startDate = yearMonth.atDay(1)
            val endDate = yearMonth.atEndOfMonth()

            // Use new hierarchical structure
            val snapshot = db.collection("users")
                .document(userId)
                .collection("entries")
                .whereGreaterThanOrEqualTo("date", startDate.format(dateFormatter))
                .whereLessThanOrEqualTo("date", endDate.format(dateFormatter))
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    MilkEntry(
                        date = LocalDate.parse(doc.getString("date") ?: "", dateFormatter),
                        quantity = doc.getDouble("quantity")?.toFloat() ?: 0f,
                        brought = doc.getBoolean("brought") ?: false,
                        note = doc.getString("note"),
                        userId = doc.getString("userId") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("FirestoreRepo", "Error parsing milk entry", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error fetching milk entries sync", e)
            emptyList()
        }
    }
    
    fun getMilkEntriesForYear(userId: String, year: Int): Flow<List<MilkEntry>> = flow {
        try {
            val startDate = LocalDate.of(year, 1, 1)
            val endDate = LocalDate.of(year, 12, 31)
            
            Log.d("FirestoreRepo", "Fetching entries for userId=$userId, year=$year")
            Log.d("FirestoreRepo", "Date range: ${startDate.format(dateFormatter)} to ${endDate.format(dateFormatter)}")
            
            // Use hierarchical structure: users/{userId}/entries
            val snapshot = db.collection("users")
                .document(userId)
                .collection("entries")
                .whereGreaterThanOrEqualTo("date", startDate.format(dateFormatter))
                .whereLessThanOrEqualTo("date", endDate.format(dateFormatter))
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d("FirestoreRepo", "Fetched ${snapshot.documents.size} documents")

            val entries = snapshot.documents.mapNotNull { doc ->
                try {
                    val entry = MilkEntry(
                        date = LocalDate.parse(doc.getString("date") ?: "", dateFormatter),
                        quantity = doc.getDouble("quantity")?.toFloat() ?: 0f,
                        brought = doc.getBoolean("brought") ?: false,
                        note = doc.getString("note"),
                        userId = doc.getString("userId") ?: ""
                    )
                    Log.d("FirestoreRepo", "Parsed entry: ${entry.date}, qty=${entry.quantity}, brought=${entry.brought}")
                    entry
                } catch (e: Exception) {
                    Log.e("FirestoreRepo", "Error parsing entry: ${e.message}")
                    null
                }
            }
            Log.d("FirestoreRepo", "Successfully parsed ${entries.size} entries")
            emit(entries)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error fetching year entries: ${e.message}", e)
            emit(emptyList())
        }
    }
    
    // Get available years that have entries
    suspend fun getAvailableYears(userId: String): List<Int> {
        return try {
            Log.d("FirestoreRepo", "Fetching available years for userId=$userId")
            
            val snapshot = db.collection("users")
                .document(userId)
                .collection("entries")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val years = snapshot.documents.mapNotNull { doc ->
                try {
                    val dateString = doc.getString("date") ?: return@mapNotNull null
                    val date = LocalDate.parse(dateString, dateFormatter)
                    date.year
                } catch (e: Exception) {
                    Log.e("FirestoreRepo", "Error parsing date for year extraction", e)
                    null
                }
            }.distinct().sorted().reversed()
            
            Log.d("FirestoreRepo", "Found years with entries: $years")
            
            // Always include current year even if no entries
            val currentYear = LocalDate.now().year
            if (!years.contains(currentYear)) {
                return listOf(currentYear) + years
            }
            
            years
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error fetching available years: ${e.message}", e)
            listOf(LocalDate.now().year) // Fallback to current year
        }
    }

    // Monthly Rates - Optimized structure
    suspend fun saveMonthlyRate(rate: MonthlyRate): Result<Unit> {
        return try {
            Log.d("FirestoreRepo", "Attempting to save monthly rate: $rate")
            
            val data = mapOf(
                "yearMonth" to rate.yearMonth.format(yearMonthFormatter),
                "ratePerLiter" to rate.ratePerLiter,
                "defaultQuantity" to rate.defaultQuantity,
                "userId" to rate.userId,
                "timestamp" to System.currentTimeMillis()
            )
            
            // Use hierarchical structure: users/{userId}/rates/{yearMonth}
            val docId = rate.yearMonth.format(yearMonthFormatter)
            Log.d("FirestoreRepo", "Saving rate to path: users/${rate.userId}/rates/$docId with data: $data")
            
            db.collection("users")
                .document(rate.userId)
                .collection("rates")
                .document(docId)
                .set(data)
                .await()
                
            Log.d("FirestoreRepo", "Successfully saved monthly rate")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error saving monthly rate", e)
            Result.failure(e)
        }
    }

    suspend fun getMonthlyRate(userId: String, yearMonth: YearMonth): MonthlyRate? {
        return try {
            // Use new hierarchical structure
            val doc = db.collection("users")
                .document(userId)
                .collection("rates")
                .document(yearMonth.format(yearMonthFormatter))
                .get()
                .await()

            if (doc.exists()) {
                MonthlyRate(
                    yearMonth = YearMonth.parse(doc.getString("yearMonth") ?: "", yearMonthFormatter),
                    ratePerLiter = doc.getDouble("ratePerLiter")?.toFloat() ?: 0f,
                    defaultQuantity = doc.getDouble("defaultQuantity")?.toFloat() ?: 0f,
                    userId = doc.getString("userId") ?: ""
                )
            } else {
                Log.d("FirestoreRepo", "No monthly rate found for $userId at $yearMonth")
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error fetching monthly rate", e)
            null
        }
    }
    
    suspend fun deleteMilkEntry(userId: String, date: LocalDate): Result<Unit> {
        return try {
            val formattedDate = date.format(dateFormatter)
            Log.d("FirestoreRepo", "Deleting milk entry for user $userId on date $formattedDate")
            Log.d("FirestoreRepo", "Delete path: users/$userId/entries/$formattedDate")
            
            // First check if the document exists (using the correct path)
            val document = db.collection("users")
                .document(userId)
                .collection("entries")
                .document(formattedDate)
                .get()
                .await()
            
            Log.d("FirestoreRepo", "Document exists before delete: ${document.exists()}")
            if (document.exists()) {
                Log.d("FirestoreRepo", "Document data: ${document.data}")
            }
            
            // Perform the delete (using the correct path that matches save)
            db.collection("users")
                .document(userId)
                .collection("entries")
                .document(formattedDate)
                .delete()
                .await()
            
            // Verify deletion
            val verifyDelete = db.collection("users")
                .document(userId)
                .collection("entries")
                .document(formattedDate)
                .get()
                .await()
            
            Log.d("FirestoreRepo", "Document exists after delete: ${verifyDelete.exists()}")
            Log.d("FirestoreRepo", "Successfully deleted milk entry")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error deleting milk entry", e)
            Log.e("FirestoreRepo", "Error details: ${e.message}")
            Log.e("FirestoreRepo", "Error cause: ${e.cause}")
            Result.failure(e)
        }
    }
    
    // Monthly Payment Status
    suspend fun saveMonthlyPayment(payment: MonthlyPayment): Result<Unit> {
        return try {
            Log.d("FirestoreRepo", "Saving monthly payment: $payment")
            
            val data = mapOf(
                "yearMonth" to payment.yearMonth.format(yearMonthFormatter),
                "isPaid" to payment.isPaid,
                "paymentNote" to payment.paymentNote,
                "paidDate" to payment.paidDate,
                "userId" to payment.userId,
                "timestamp" to System.currentTimeMillis()
            )
            
            val docId = payment.yearMonth.format(yearMonthFormatter)
            db.collection("users")
                .document(payment.userId)
                .collection("payments")
                .document(docId)
                .set(data)
                .await()
                
            Log.d("FirestoreRepo", "Successfully saved monthly payment")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error saving monthly payment", e)
            Result.failure(e)
        }
    }
    
    suspend fun getMonthlyPayment(userId: String, yearMonth: YearMonth): MonthlyPayment? {
        return try {
            val doc = db.collection("users")
                .document(userId)
                .collection("payments")
                .document(yearMonth.format(yearMonthFormatter))
                .get()
                .await()
                
            if (doc.exists()) {
                MonthlyPayment(
                    yearMonth = YearMonth.parse(doc.getString("yearMonth") ?: "", yearMonthFormatter),
                    userId = doc.getString("userId") ?: "",
                    isPaid = doc.getBoolean("isPaid") ?: false,
                    paymentNote = doc.getString("paymentNote") ?: "",
                    paidDate = doc.getLong("paidDate")
                )
            } else {
                // Return default unpaid status
                MonthlyPayment(
                    yearMonth = yearMonth,
                    userId = userId,
                    isPaid = false,
                    paymentNote = ""
                )
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error fetching monthly payment", e)
            // Return default unpaid status on error
            MonthlyPayment(
                yearMonth = yearMonth,
                userId = userId,
                isPaid = false,
                paymentNote = ""
            )
        }
    }
    
    // Save notification settings
    suspend fun saveNotificationSettings(
        userId: String,
        type: String,
        hour: Int,
        minute: Int
    ): Result<Unit> {
        return try {
            val data = hashMapOf(
                "userId" to userId,
                "${type}Hour" to hour,
                "${type}Minute" to minute,
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            db.collection("users")
                .document(userId)
                .collection("notificationSettings")
                .document(type)
                .set(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error saving notification settings", e)
            Result.failure(e)
        }
    }
} 