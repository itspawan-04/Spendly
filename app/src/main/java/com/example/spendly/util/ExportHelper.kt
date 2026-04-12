package com.example.spendly.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.spendly.data.db.CategoryEntity
import com.example.spendly.data.db.ExpenseEntity
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

object ExportHelper {
    private val dateFmt: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun csvUri(context: Context, expenses: List<ExpenseEntity>, categories: Map<Long, CategoryEntity>): Uri {
        val header = "date,title,amount,category,notes\n"
        val body = expenses.joinToString("\n") { e ->
            val cat = categories[e.categoryId]?.name.orEmpty()
            listOf(
                e.date.format(dateFmt),
                csvEscape(e.title),
                (e.amountMinor / 100.0).toString(),
                csvEscape(cat),
                csvEscape(e.notes),
            ).joinToString(",")
        }
        val file = File(context.cacheDir, "spendly_export_${System.currentTimeMillis()}.csv")
        FileOutputStream(file).use { it.write((header + body).toByteArray(Charsets.UTF_8)) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun csvEscape(s: String): String {
        val needsQuote = s.contains(',') || s.contains('"') || s.contains('\n')
        val escaped = s.replace("\"", "\"\"")
        return if (needsQuote) "\"$escaped\"" else escaped
    }

    fun pdfUri(
        context: Context,
        title: String,
        expenses: List<ExpenseEntity>,
        categories: Map<Long, CategoryEntity>,
        summaryLines: List<String>,
    ): Uri {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = 48f
        val titlePaint = Paint().apply {
            textSize = 18f
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            textSize = 11f
            isAntiAlias = true
        }
        fun newPageIfNeeded(extra: Float = 24f) {
            if (y + extra > 800f) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 48f
            }
        }
        canvas.drawText(title, 48f, y, titlePaint)
        y += 32f
        summaryLines.forEach { line ->
            newPageIfNeeded()
            canvas.drawText(line.take(90), 48f, y, bodyPaint)
            y += 18f
        }
        y += 12f
        canvas.drawText("Expenses", 48f, y, titlePaint)
        y += 26f
        expenses.sortedBy { it.dateEpochDay }.forEach { e ->
            newPageIfNeeded(40f)
            val cat = categories[e.categoryId]?.name.orEmpty()
            val line = "${e.date}  ${e.title.take(40)}  ${e.amountMinor / 100.0}  $cat"
            canvas.drawText(line.take(95), 48f, y, bodyPaint)
            y += 16f
        }
        document.finishPage(page)
        val file = File(context.cacheDir, "spendly_report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun share(context: Context, uri: Uri, mime: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Spendly"))
    }
}
