package com.example.movinbuddy.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.example.movinbuddy.data.InventoryStore
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    private const val PAGE_WIDTH = 612
    private const val PAGE_HEIGHT = 792
    private const val MARGIN = 40f

    private data class PhotoRef(val code: String, val sectionName: String, val itemName: String, val photoPath: String)

    fun generate(context: Context, store: InventoryStore): Uri? {
        return try {
            val document = PdfDocument()

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 16f; isFakeBoldText = true; color = Color.BLACK; textAlign = Paint.Align.CENTER }
            val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9f; color = Color.DKGRAY; textAlign = Paint.Align.CENTER }
            val fieldLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9.5f; isFakeBoldText = true; color = Color.BLACK }
            val fieldValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9.5f; color = Color.BLACK }
            val sectionHeadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f; isFakeBoldText = true; color = Color.WHITE }
            val columnHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9f; isFakeBoldText = true; color = Color.BLACK }
            val itemNamePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10f; color = Color.BLACK }
            val refPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 7.5f; color = Color.DKGRAY }
            val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9.5f; color = Color.rgb(0x15, 0x65, 0xC0) }
            val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 0.75f; color = Color.rgb(0x99, 0x99, 0x99) }
            val sectionBandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(0x40, 0x40, 0x40) }
            val captionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 8.5f; color = Color.DKGRAY }
            val captionBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9.5f; isFakeBoldText = true; color = Color.BLACK }
            val sectionHeadingPaintDark = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f; isFakeBoldText = true; color = Color.BLACK }
            val bodyTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9.5f; color = Color.BLACK }

            var pageNumber = 1
            var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            var canvas = page.canvas
            var y = MARGIN

            val contentLeft = MARGIN
            val contentRight = PAGE_WIDTH - MARGIN
            val contentWidthF = contentRight - contentLeft
            val itemColWidth = contentWidthF * 0.34f
            val commentColLeft = contentLeft + itemColWidth
            val commentColWidth = (contentRight - commentColLeft).toInt()

            fun newPage() {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas
                y = MARGIN
            }

            fun ensureSpace(height: Float) {
                if (y + height > PAGE_HEIGHT - MARGIN) newPage()
            }

            fun drawTableHeader(sectionLabel: String? = null) {
                ensureSpace(if (sectionLabel != null) 38f else 20f)
                if (sectionLabel != null) {
                    canvas.drawRect(contentLeft, y, contentRight, y + 18f, sectionBandPaint)
                    canvas.drawText(sectionLabel, contentLeft + 4f, y + 13f, sectionHeadingPaint)
                    y += 18f
                }
                canvas.drawText("ITEM", contentLeft + 4f, y + 13f, columnHeaderPaint)
                canvas.drawText("CONDITION / COMMENTS", commentColLeft + 4f, y + 13f, columnHeaderPaint)
                y += 18f
                canvas.drawLine(contentLeft, y, contentRight, y, gridPaint)
                y += 4f
            }

            // ---- Header ----
            canvas.drawText("RESIDENTIAL INVENTORY AND CONDITION FORM", (PAGE_WIDTH / 2).toFloat(), y + 14f, titlePaint)
            y += 18f
            canvas.drawText("Move-In / Move-Out Inspection Record", (PAGE_WIDTH / 2).toFloat(), y + 10f, subTitlePaint)
            y += 22f

            canvas.drawLine(contentLeft, y, contentRight, y, gridPaint)
            y += 12f
            canvas.drawText("Property Address:", contentLeft, y, fieldLabelPaint)
            canvas.drawText(store.homeInfo.address, contentLeft + 90f, y, fieldValuePaint)
            canvas.drawLine(contentLeft + 90f, y + 2f, contentRight, y + 2f, gridPaint)
            y += 16f
            canvas.drawText("Tenant(s):", contentLeft, y, fieldLabelPaint)
            canvas.drawText(store.homeInfo.tenantNames, contentLeft + 90f, y, fieldValuePaint)
            canvas.drawLine(contentLeft + 90f, y + 2f, contentRight, y + 2f, gridPaint)
            y += 16f
            canvas.drawText("Landlord/Agent:", contentLeft, y, fieldLabelPaint)
            canvas.drawText(store.homeInfo.landlordName, contentLeft + 90f, y, fieldValuePaint)
            canvas.drawLine(contentLeft + 90f, y + 2f, contentRight, y + 2f, gridPaint)
            y += 16f
            canvas.drawLine(contentLeft, y, contentRight, y, gridPaint)
            y += 16f

            // ---- Sections & Items (every item is listed; blank comments stay blank) ----
            val photoRefs = mutableListOf<PhotoRef>()

            store.getSections().forEachIndexed { sectionIndex, section ->
                if (section.items.isEmpty()) return@forEachIndexed

                val sectionLabel = section.displayName.uppercase()
                drawTableHeader(sectionLabel)

                section.items.forEachIndexed { itemIndex, item ->
                    val code = "S${sectionIndex + 1}.${itemIndex + 1}"

                    val commentText = item.comment
                    val commentLayout = if (commentText.isNotBlank()) {
                        StaticLayout.Builder.obtain(commentText, 0, commentText.length, bodyPaint, commentColWidth).build()
                    } else null

                    val nameLayout = StaticLayout.Builder
                        .obtain(item.name, 0, item.name.length, itemNamePaint, (itemColWidth - 8f).toInt())
                        .build()

                    val hasPhotos = item.photos.isNotEmpty()
                    val rowContentHeight = maxOf(nameLayout.height + (if (hasPhotos) 11f else 0f), commentLayout?.height?.toFloat() ?: 11f)
                    val rowHeight = rowContentHeight + 10f

                    val pageBefore = pageNumber
                    ensureSpace(rowHeight)
                    if (pageNumber != pageBefore) drawTableHeader("$sectionLabel (continued)")
                    val rowTop = y

                    canvas.save()
                    canvas.translate(contentLeft + 4f, rowTop + 5f)
                    nameLayout.draw(canvas)
                    canvas.restore()
                    if (hasPhotos) {
                        canvas.drawText("Ref $code  (${item.photos.size} photo${if (item.photos.size > 1) "s" else ""})", contentLeft + 4f, rowTop + 5f + nameLayout.height + 9f, refPaint)
                    }

                    if (commentLayout != null) {
                        canvas.save()
                        canvas.translate(commentColLeft + 4f, rowTop + 5f)
                        commentLayout.draw(canvas)
                        canvas.restore()
                    }

                    y = rowTop + rowHeight
                    canvas.drawLine(contentLeft, y, contentRight, y, gridPaint)
                    canvas.drawLine(commentColLeft, rowTop, commentColLeft, y, gridPaint)
                    canvas.drawLine(contentLeft, rowTop, contentLeft, y, gridPaint)
                    canvas.drawLine(contentRight, rowTop, contentRight, y, gridPaint)

                    item.photos.forEachIndexed { photoIndex, photo ->
                        photoRefs.add(PhotoRef("$code-P${photoIndex + 1}", section.displayName, item.name, photo.filePath))
                    }
                }
                y += 14f
            }

            // ---- Signatures ----
            val ackText = "By signing below, the undersigned Tenant(s) and Landlord/Agent acknowledge that this " +
                "inventory and condition report accurately reflects the condition of the property as of the date signed."
            val ackLayout = StaticLayout.Builder
                .obtain(ackText, 0, ackText.length, bodyTextPaint, contentWidthF.toInt())
                .build()

            val tenantNames = store.homeInfo.tenantNames
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
            val tenantLineCount = if (tenantNames.isEmpty()) 2 else tenantNames.size.coerceAtMost(4)
            val tenantRowCount = (tenantLineCount + 1) / 2
            val sigRowHeight = 52f

            val headerBlockHeight = 6f + 16f + 16f
            val ackBlockHeight = ackLayout.height + 20f
            val tenantBlockHeight = tenantRowCount * sigRowHeight
            val landlordBlockHeight = 8f + sigRowHeight
            val totalSignatureHeight = headerBlockHeight + ackBlockHeight + tenantBlockHeight + landlordBlockHeight

            ensureSpace(totalSignatureHeight)

            y += 6f
            canvas.drawLine(contentLeft, y, contentRight, y, gridPaint)
            y += 16f
            canvas.drawText("SIGNATURES", contentLeft, y, sectionHeadingPaintDark)
            y += 16f

            canvas.save()
            canvas.translate(contentLeft, y)
            ackLayout.draw(canvas)
            canvas.restore()
            y += ackLayout.height + 20f

            val sigColWidth = (contentWidthF - 20f) / 2f
            val sigRightColLeft = contentLeft + sigColWidth + 20f

            fun drawSignatureLine(x: Float, topY: Float, width: Float, printedName: String, label: String) {
                if (printedName.isNotBlank()) {
                    canvas.drawText(printedName, x, topY, fieldValuePaint)
                }
                canvas.drawLine(x, topY + 20f, x + width - 70f, topY + 20f, gridPaint)
                canvas.drawLine(x + width - 60f, topY + 20f, x + width, topY + 20f, gridPaint)
                canvas.drawText(label, x, topY + 32f, refPaint)
                canvas.drawText("Date", x + width - 60f, topY + 32f, refPaint)
            }

            var i = 0
            while (i < tenantLineCount) {
                val rowTop = y
                val leftName = tenantNames.getOrElse(i) { "" }
                drawSignatureLine(contentLeft, rowTop, sigColWidth, leftName, "Tenant Signature")
                if (i + 1 < tenantLineCount) {
                    val rightName = tenantNames.getOrElse(i + 1) { "" }
                    drawSignatureLine(sigRightColLeft, rowTop, sigColWidth, rightName, "Tenant Signature")
                }
                y = rowTop + sigRowHeight
                i += 2
            }

            y += 8f
            drawSignatureLine(contentLeft, y, contentWidthF, store.homeInfo.landlordName, "Landlord / Landlord's Agent")
            y += sigRowHeight

            // ---- Appendix ----
            if (photoRefs.isNotEmpty()) {
                newPage()
                canvas.drawText("APPENDIX: PHOTOS", (PAGE_WIDTH / 2).toFloat(), y + 14f, titlePaint)
                y += 12f
                canvas.drawText("Each photo is labeled with its reference code and traceable section/item.", (PAGE_WIDTH / 2).toFloat(), y + 10f, subTitlePaint)
                y += 26f

                val cols = 2
                val cellWidth = (PAGE_WIDTH - 2 * MARGIN) / cols
                val imageSize = cellWidth - 16f
                var col = 0

                photoRefs.forEach { ref ->
                    ensureSpace(imageSize + 36f)
                    val x = MARGIN + col * cellWidth
                    val bitmap = decodeSampledBitmap(ref.photoPath, imageSize.toInt(), imageSize.toInt())
                    if (bitmap != null) {
                        canvas.drawBitmap(bitmap, null, RectF(x, y, x + imageSize, y + imageSize), null)
                    }
                    canvas.drawText(ref.code, x, y + imageSize + 12f, captionBoldPaint)
                    val caption = "${ref.sectionName} > ${ref.itemName}"
                    val truncated = TextUtils.ellipsize(caption, captionPaint, imageSize, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(truncated, x, y + imageSize + 24f, captionPaint)

                    col++
                    if (col >= cols) {
                        col = 0
                        y += imageSize + 36f
                    }
                }
            }

            document.finishPage(page)

            val pdfDir = File(context.filesDir, "pdfs").apply { mkdirs() }
            val pdfFile = File(pdfDir, "MovInBuddy_Inventory.pdf")
            FileOutputStream(pdfFile).use { document.writeTo(it) }
            document.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeSampledBitmap(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, boundsOptions)
        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return null

        var inSampleSize = 1
        var halfHeight = boundsOptions.outHeight / 2
        var halfWidth = boundsOptions.outWidth / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
        return BitmapFactory.decodeFile(path, decodeOptions)
    }
}
