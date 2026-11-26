package app.iris

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView

/** The few sizes and colours of the test build (Consora's: background, card, accent; 12 margins). */
object Ui {
    val BG = Color.parseColor("#E8F2FC")
    val CARD = Color.WHITE
    val FG = Color.parseColor("#2D3039")
    val MUTED = Color.parseColor("#676A74")
    val ACCENT = Color.parseColor("#057394")

    fun dp(c: Context, v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), c.resources.displayMetrics).toInt()

    fun round(c: Context, color: Int, radiusDp: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(c, radiusDp).toFloat()
    }

    fun text(c: Context, s: String, sizeSp: Float = 14f, color: Int = FG, bold: Boolean = false) = TextView(c).apply {
        text = s
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        setTextColor(color)
        if (bold) typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        gravity = Gravity.CENTER_VERTICAL
    }
}
