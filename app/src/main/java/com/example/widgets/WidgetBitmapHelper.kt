package com.example.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import com.example.model.ExpressiveShapeStyle
import com.example.ui.theme.ExpressivePolygons
import kotlin.math.max
import kotlin.math.min

object WidgetBitmapHelper {

    fun downsampleForWidget(source: Bitmap?, maxDimensionPx: Int = 220): Bitmap? {
        if (source == null || source.isRecycled) return null
        val w = source.width
        val h = source.height
        if (w <= 0 || h <= 0) return null
        if (w <= maxDimensionPx && h <= maxDimensionPx) {
            return source.copy(Bitmap.Config.ARGB_8888, false)
        }
        val scale = maxDimensionPx.toFloat() / max(w, h).toFloat()
        val targetW = max(1, (w * scale).toInt())
        val targetH = max(1, (h * scale).toInt())
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    fun clipToExpressiveShape(
        source: Bitmap?,
        shapeStyle: ExpressiveShapeStyle,
        outputSizePx: Int = 180,
        vinylSpindleCutout: Boolean = false
    ): Bitmap? {
        if (source == null || source.isRecycled) return null
        val out = Bitmap.createBitmap(outputSizePx, outputSizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val fSize = outputSizePx.toFloat()
        val polygon = ExpressivePolygons.polygonFor(shapeStyle)
        val maskPath = ExpressivePolygons.buildScaledAndroidPath(polygon, fSize, fSize)

        canvas.drawPath(maskPath, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // Center-crop source bitmap into square output
        val srcSize = min(source.width, source.height)
        val srcLeft = (source.width - srcSize) / 2
        val srcTop = (source.height - srcSize) / 2
        val srcRect = Rect(srcLeft, srcTop, srcLeft + srcSize, srcTop + srcSize)
        val dstRect = RectF(0f, 0f, fSize, fSize)
        canvas.drawBitmap(source, srcRect, dstRect, paint)

        if (vinylSpindleCutout) {
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            canvas.drawCircle(fSize / 2f, fSize / 2f, fSize * 0.11f, paint)

            paint.xfermode = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = fSize * 0.025f
            paint.color = 0x55FFFFFF
            canvas.drawCircle(fSize / 2f, fSize / 2f, fSize * 0.14f, paint)
            canvas.drawCircle(fSize / 2f, fSize / 2f, fSize * 0.34f, paint)
        }

        return out
    }
}
