package com.example.jumptest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.collection.FloatList
import androidx.collection.emptyFloatList
import kotlin.math.ceil
import kotlin.math.floor

class XYChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val coordinatesPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val coordinatesTicksPaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val coordinatesTextPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 3f
        style = Paint.Style.STROKE
        textAlign = Paint.Align.CENTER
        textSize = 20F
    }

    private val curvePaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        color = Color.MAGENTA
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private var x: FloatList = emptyFloatList()
    private var y: List<FloatList> = emptyList<FloatList>()
    private var xRange: Pair<Float, Float> = Pair<Float, Float>(0F, 0F)
    private var yRange: Pair<Float, Float> = Pair<Float, Float>(0F, 0F)
    private var scales: Pair<Float, Float> = Pair<Float, Float>(0F, 0F)
    private val curveColors: List<Int> = listOf(Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.MAGENTA, Color.YELLOW)
    private val textOffset = 50
    private val xTickNumber = 10F
    private val yTickNumber = 10F

    fun changeData(xIn: FloatList, yIn: List<FloatList>) {
        x = xIn
        y = yIn
        invalidate() // This will call onDraw()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if(x.isEmpty() || y.isEmpty() || y[0].size != x.size)
            return

        for(i in y.indices){
            if(i > 0 && y[i].size != y[i-1].size)
                return
        }

        calculateRangesAndScales()

        drawCoorinates(canvas)
        drawCurves(canvas)
        //drawBorder(canvas)
    }

    private fun drawBorder(canvas: Canvas) {
        canvas.drawLine(0F, 0F, 0F, height.toFloat(), borderPaint)
        canvas.drawLine(0F, 0F, width.toFloat(), 0F, borderPaint)
        canvas.drawLine(0F, height.toFloat(), width.toFloat(), height.toFloat(), borderPaint)
        canvas.drawLine(width.toFloat(), 0F, width.toFloat(), height.toFloat(), borderPaint)

    }

    private fun drawCoorinates(canvas: Canvas) {
        val x0 = xViewportCoordinate(0F)
        val y0 = yViewportCoordinate(0F)
        val xn = getChartWidth().toFloat() + textOffset
        val xTickStep = (xRange.second - xRange.first)/xTickNumber
        var x = xRange.first
        val yTickStep = (yRange.second - yRange.first)/yTickNumber
        var y = yRange.first

        while(x <= xRange.second)
        {
            val xv = xViewportCoordinate(x)
            canvas.drawLine(xv, 0F, xv, getChartHeight().toFloat(), coordinatesTicksPaint)
            if(xv >= width) {
                coordinatesTextPaint.textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("${x.toInt()}",
                xv,
                (getChartHeight()+textOffset/2).toFloat(), coordinatesTextPaint)
            x += xTickStep
        }
        coordinatesTextPaint.textAlign = Paint.Align.CENTER
        while(y <= yRange.second)
        {
            var yv = yViewportCoordinate(y)
            canvas.drawLine(x0, yv, xn, yv, coordinatesTicksPaint)
            if(yv < coordinatesTextPaint.textSize) {
                yv = coordinatesTextPaint.textSize
            }
            canvas.drawText("${yv.toInt()}",
                (textOffset/2).toFloat(), yv, coordinatesTextPaint)
            y += yTickStep
        }

        canvas.drawLine(x0, 0F, x0, getChartHeight().toFloat(), coordinatesPaint)
        canvas.drawLine(x0, y0, xn, y0, coordinatesPaint)
    }

    private fun drawCurves(canvas: Canvas)
    {
        var x1 = 0F
        var y1 = 0F
        for (i in x.indices) {
            x1 = xViewportCoordinate(x[i])
            for(j in y.indices) {
                curvePaint.color = curveColors[j % curveColors.size]
                y1 = yViewportCoordinate(y[j][i])
                canvas.drawCircle(x1, y1, 5F, curvePaint)
                if (i > 0) {
                    canvas.drawLine(
                        x1,
                        y1,
                        xViewportCoordinate(x[i - 1]),
                        yViewportCoordinate(y[j][i - 1]),
                        curvePaint
                    )
                }
            }
        }

        curvePaint.color = Color.BLUE
    }

    private fun calculateRangesAndScales()
    {
        val xMin = floor(x[0])
        val xMax = ceil(x[x.size-1])
        var yMin = Float.POSITIVE_INFINITY
        var yMax = Float.NEGATIVE_INFINITY

        for(i in y.indices){
            for(j in y[i].indices)
            {
                if(yMin > y[i][j])
                    yMin = y[i][j]
                if(yMax < y[i][j])
                    yMax = y[i][j]
            }
        }

        yMin = floor(yMin - 1F)
        yMax = ceil(yMax + 1F)

        xRange = Pair<Float, Float>(xMin, xMax)
        yRange = Pair<Float, Float>(yMin, yMax)
        scales = Pair<Float, Float>(getChartWidth()/(xMax- xMin), getChartHeight()/(yMax- yMin))
    }

    private fun xViewportCoordinate(x: Float): Float {
        return  scales.first*(x-xRange.first) + textOffset
    }

    private fun yViewportCoordinate(y: Float): Float {
        return getChartHeight() - scales.second*(y-yRange.first)
    }

    private fun getChartWidth(): Int {
        return width - textOffset
    }

    private fun getChartHeight(): Int {
        return height - textOffset
    }
}