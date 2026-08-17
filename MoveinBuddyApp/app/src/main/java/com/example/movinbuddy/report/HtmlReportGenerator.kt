package com.example.movinbuddy.report

import android.content.Context
import android.util.Base64
import com.example.movinbuddy.data.InventoryStore
import java.io.File

object HtmlReportGenerator {

    fun generate(context: Context, store: InventoryStore): File {
        val builder = StringBuilder()
        builder.append(
            """
            <!DOCTYPE html>
            <html><head><meta charset="utf-8">
            <title>MovInBuddy Report</title>
            <style>
                body { font-family: sans-serif; margin: 24px; color: #212121; }
                h1 { color: #6200EE; }
                h2 { color: #6200EE; border-bottom: 2px solid #EEE; padding-bottom: 4px; margin-top: 32px; }
                .item { margin: 16px 0; padding: 12px; background: #F7F7F7; border-radius: 8px; }
                .item-name { font-weight: bold; font-size: 16px; }
                .comment { color: #1565C0; margin: 6px 0; }
                .photos img { width: 140px; height: 140px; object-fit: cover; margin: 4px; border-radius: 6px; }
                .meta { color: #757575; margin-bottom: 24px; }
            </style>
            </head><body>
            """.trimIndent()
        )

        builder.append("<h1>Move-In Inventory Report</h1>")
        builder.append("<div class=\"meta\">")
        if (store.homeInfo.address.isNotBlank()) builder.append("Property: ${escape(store.homeInfo.address)}<br/>")
        if (store.homeInfo.tenantNames.isNotBlank()) builder.append("Tenant(s): ${escape(store.homeInfo.tenantNames)}<br/>")
        if (store.homeInfo.landlordName.isNotBlank()) builder.append("Landlord: ${escape(store.homeInfo.landlordName)}<br/>")
        builder.append("</div>")

        store.getSections().forEach { section ->
            builder.append("<h2>${escape(section.displayName)}</h2>")
            section.items.forEach { item ->
                if (item.comment.isBlank() && item.photos.isEmpty()) return@forEach
                builder.append("<div class=\"item\">")
                builder.append("<div class=\"item-name\">${escape(item.name)}</div>")
                if (item.comment.isNotBlank()) {
                    builder.append("<div class=\"comment\">${escape(item.comment)}</div>")
                }
                if (item.photos.isNotEmpty()) {
                    builder.append("<div class=\"photos\">")
                    item.photos.forEach { photo ->
                        val file = File(photo.filePath)
                        if (file.exists()) {
                            val base64 = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
                            builder.append("<img src=\"data:image/jpeg;base64,$base64\" />")
                        }
                    }
                    builder.append("</div>")
                }
                builder.append("</div>")
            }
        }

        builder.append("</body></html>")

        val reportsDir = File(context.filesDir, "reports").apply { mkdirs() }
        val reportFile = File(reportsDir, "report.html")
        reportFile.writeText(builder.toString())
        return reportFile
    }

    private fun escape(text: String): String =
        text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}
