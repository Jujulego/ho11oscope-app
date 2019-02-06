package net.capellari.julien.opengl

import android.content.Context
import android.graphics.Color.*
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import net.capellari.julien.opengl.base.BaseVec

class Color() : BaseVec<Color>(3, Color::class) {
    companion object {
        val WHITE = Color(0xffffff)
        val BLACK = Color(0x000000)

        val RED   = Color(0xff0000)
        val GREEN = Color(0x00ff00)
        val BLUE  = Color(0x0000ff)

        val YELLOW  = RED + GREEN
        val CYAN    = GREEN + BLUE
        val MAGENTA = BLUE + RED
    }

    // Propriétés
    var r: Float get() = data[0]
        set(v) { data[0] = v }

    var g: Float get() = data[1]
        set(v) { data[1] = v }

    var b: Float get() = data[2]
        set(v) { data[2] = v }

    // Constructeurs
    constructor(o: Color) : this(o.r, o.g, o.b)
    constructor(o: ColorAlpha) : this(o.r, o.g, o.b)
    constructor(r: Float, v: Float, b: Float): this() {
        data[0] = r; data[1] = v; data[2] = b
    }

    constructor(@ColorInt color: Int): this() {
        data[0] = red(color)   / 255f // R
        data[1] = green(color) / 255f // G
        data[2] = blue(color)  / 255f // B
    }
    constructor(context: Context, @ColorRes color: Int): this(context.getColor(color))

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(color: android.graphics.Color): this(color.toArgb())
}