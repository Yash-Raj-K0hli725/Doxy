package com.cranox.doxy.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

object PDFUtils {

    suspend fun createPDF(
        ctx: Context?,
        _images: List<Uri>,
        filename: String,
        onSuccess: () -> Unit
    ) {
        if (ctx == null) return
        delay(1)
        val images = mutableListOf<Uri>()
        _images.forEach { mUri -> images.add(mUri) }
        val pdfDocument = PdfDocument()
        try {
            val cr = ctx.contentResolver
            val maxWidth = 595
            val maxHeight = 842
//            TODO find a way to fix the page sizes
            images.forEachIndexed { index, uri ->
                val bmp = getBitmap(cr, uri) ?: return@forEachIndexed
                val pageH = minOf(bmp.height, maxHeight)
                val pageW = minOf(bmp.width, maxWidth)
                val pageInfo = PdfDocument
                    .PageInfo.Builder(
                        pageW,
                        pageH,
                        index + 1
                    ).create()
                val page = pdfDocument.startPage(pageInfo)
                drawOnCanvas(pageW, pageH, page.canvas, bmp)
                pdfDocument.finishPage(page)
                bmp.recycle()
            }
            val pdfUri = createPDFUri(cr, filename)
            pdfUri?.let { uri ->
                cr.openOutputStream(uri).use { output ->
                    pdfDocument.writeTo(output)
                    withContext(Dispatchers.Main) {
                        xToast(ctx, "PDF,Created to: $uri")
                        onSuccess.invoke()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("pdfUtil", "error->", e)
        } finally {
            pdfDocument.close()
        }

    }

    private fun drawOnCanvas(pageW: Int, pageH: Int, canvas: Canvas, bitmap: Bitmap) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val scale = minOf(
            pageW.toFloat() / bitmap.width,
            pageH.toFloat() / bitmap.height
        )
        val scaledW = bitmap.width * scale
        val scaledH = bitmap.height * scale

        val start = (pageW - scaledW) / 2f
        val top = (pageH - scaledH) / 2f

        val rect = RectF(start, top, start + scaledW, top + scaledH)
        canvas.drawBitmap(bitmap, null, rect, paint)
    }

    private fun createPDFUri(cr: ContentResolver, filename: String): Uri? {
        val mimeType = "application/pdf"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Doxy")
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cr.insert(MediaStore.Files.getContentUri("external"), values)
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, filename)
            Uri.fromFile(file)
        }
    }

    fun getBitmap(cr: ContentResolver, uri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val source = ImageDecoder.createSource(cr, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE

            }
        } else {
            cr.openInputStream(uri)?.use { inp ->
                BitmapFactory.decodeStream(inp)
            }
        }
    }
}