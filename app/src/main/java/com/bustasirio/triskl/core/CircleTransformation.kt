package com.bustasirio.triskl.core

import android.graphics.*
import com.squareup.picasso.Transformation

/**
 * Created by codezjx on 2016/5/4.
 * https://gist.github.com/codezjx/b8a99374385a0210edb9192bced516a3
 */

/**
 * Kotlin version alexmaryin
 */

class CircleTransformation : Transformation {

    private val key = "circleTransformation"

    override fun transform(source: Bitmap?): Bitmap {
        val minEdge = source!!.width.coerceAtMost(source.height)
        val dx = (source.width - minEdge) / 2
        val dy = (source.height - minEdge) / 2
        val shader: Shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val matrix = Matrix()
        matrix.setTranslate(-dx.toFloat(), -dy.toFloat())
        shader.setLocalMatrix(matrix)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = shader
        val output = Bitmap.createBitmap(minEdge, minEdge, source.config)
        val canvas = Canvas(output)
        canvas.drawOval(RectF(0f, 0f, minEdge.toFloat(), minEdge.toFloat()), paint)
        source.recycle()
        return output
    }

    override fun key(): String {
        return key
    }
}