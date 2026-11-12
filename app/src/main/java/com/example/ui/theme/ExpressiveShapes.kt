package com.example.ui.theme

import android.graphics.Matrix
import android.graphics.Path as AndroidPath
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import com.example.model.ExpressiveShapeStyle

object ExpressivePolygons {

    fun fourSidedCookie(): RoundedPolygon {
        return RoundedPolygon.star(
            numVerticesPerRadius = 4,
            radius = 1f,
            innerRadius = 0.72f,
            rounding = CornerRounding(radius = 0.34f, smoothing = 0.4f)
        )
    }

    fun scallopedFlower(): RoundedPolygon {
        return RoundedPolygon.star(
            numVerticesPerRadius = 8,
            radius = 1f,
            innerRadius = 0.80f,
            rounding = CornerRounding(radius = 0.24f, smoothing = 0.35f)
        )
    }

    fun expressiveSquircle(): RoundedPolygon {
        return RoundedPolygon(
            numVertices = 4,
            radius = 1.15f,
            rounding = CornerRounding(radius = 0.42f, smoothing = 0.65f)
        ).transformed { x, y ->
            // Rotate 45 degrees so flat sides align with square container
            val angle = Math.PI / 4.0
            val cos = kotlin.math.cos(angle).toFloat()
            val sin = kotlin.math.sin(angle).toFloat()
            androidx.graphics.shapes.TransformResult(x * cos - y * sin, x * sin + y * cos)
        }
    }

    fun polygonFor(style: ExpressiveShapeStyle): RoundedPolygon = when (style) {
        ExpressiveShapeStyle.SCALLOPED_FLOWER -> scallopedFlower()
        ExpressiveShapeStyle.FOUR_SIDED_COOKIE -> fourSidedCookie()
        ExpressiveShapeStyle.SQUIRCLE -> expressiveSquircle()
        ExpressiveShapeStyle.STADIUM_PILL -> RoundedPolygon(
            numVertices = 4,
            radius = 1.2f,
            rounding = CornerRounding(radius = 0.55f, smoothing = 0.8f)
        ).transformed { x, y ->
            val angle = Math.PI / 4.0
            val cos = kotlin.math.cos(angle).toFloat()
            val sin = kotlin.math.sin(angle).toFloat()
            androidx.graphics.shapes.TransformResult(x * cos - y * sin, x * sin + y * cos)
        }
    }

    fun buildScaledAndroidPath(polygon: RoundedPolygon, width: Float, height: Float): AndroidPath {
        val bounds = FloatArray(4)
        polygon.calculateBounds(bounds, approximate = false)
        val polyWidth = (bounds[2] - bounds[0]).coerceAtLeast(0.001f)
        val polyHeight = (bounds[3] - bounds[1]).coerceAtLeast(0.001f)
        val path = polygon.toPath()
        val matrix = Matrix().apply {
            postTranslate(-bounds[0], -bounds[1])
            postScale(width / polyWidth, height / polyHeight)
        }
        path.transform(matrix)
        return path
    }
}

class RoundedPolygonShape(private val polygon: RoundedPolygon) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val androidPath = ExpressivePolygons.buildScaledAndroidPath(polygon, size.width, size.height)
        return Outline.Generic(androidPath.asComposePath())
    }
}
