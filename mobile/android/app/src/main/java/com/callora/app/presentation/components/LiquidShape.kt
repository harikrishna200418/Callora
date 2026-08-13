package com.callora.app.presentation.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class LiquidShape(private val stretchFactor: Float = 1f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val width = size.width
            val height = size.height
            
            moveTo(width * 0.2f, height * 0.1f)
            cubicTo(
                width * 0.8f, height * -0.1f * stretchFactor,
                width * 1.1f, height * 0.8f,
                width * 0.7f, height * 0.9f
            )
            cubicTo(
                width * 0.3f, height * 1.1f * stretchFactor,
                width * -0.1f, height * 0.6f,
                width * 0.2f, height * 0.1f
            )
            close()
        }
        return Outline.Generic(path)
    }
}
