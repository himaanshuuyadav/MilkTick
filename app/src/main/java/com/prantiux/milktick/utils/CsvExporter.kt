package com.prantiux.milktick.utils

import android.content.Context
import android.os.Environment
import com.prantiux.milktick.data.MilkEntry
import java.io.File
import java.io.FileWriter
import java.time.format.DateTimeFormatter

class CsvExporter {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun exportMonthlyData(
        @Suppress("UNUSED_PARAMETER") context: Context,
        entries: List<MilkEntry>,
        yearMonth: String,
        totalDays: Int,
        totalLiters: Float,
        totalCost: Float
    ): Result<String> {
        return try {
            // Use external storage public directory for Documents
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val milktickDir = File(documentsDir, "MilkTick")
            if (!milktickDir.exists()) {
                milktickDir.mkdirs()
            }
            
            val fileName = "milk_data_$yearMonth.csv"
            val file = File(milktickDir, fileName)
            
            FileWriter(file).use { writer ->
                // Write header
                writer.append("Date,Quantity (L),Brought,Note\n")
                
                // Write data
                entries.forEach { entry ->
                    writer.append("${entry.date.format(dateFormatter)},")
                    writer.append("${entry.quantity},")
                    writer.append("${if (entry.brought) "Yes" else "No"},")
                    writer.append("${entry.note ?: ""}\n")
                }
                
                // Write summary
                writer.append("\n")
                writer.append("Summary\n")
                writer.append("Total Days Milk Brought,$totalDays\n")
                writer.append("Total Liters,$totalLiters\n")
                writer.append("Total Cost,₹${String.format("%.2f", totalCost)}\n")
            }
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 