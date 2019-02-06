package net.capellari.julien.opengl

import android.content.Context
import android.graphics.Color.*
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import net.capellari.julien.opengl.base.BaseVec

class ColorAlpha() : BaseVec<ColorAlpha>(4, ColorAlpha::class) {
    // Propriétés
    var r: Float get() = data[0]
        set(v) { data[0] = v }

    var g: Float get() = data[1]
        set(v) { data[1] = v }

    var b: Float get() = data[2]
        set(v) { data[2] = v }

    var a: Float get() = data[3]
        set(v) { data[3] = v }

    val rgb: Color get() = Color(this)

    // Constructeurs
    constructor(c: Color) : this(c.r, c.g, c.b, 1f)
    constructor(o: ColorAlpha) : this(o.r, o.g, o.b, o.a)
    constructor(r: Float, v: Float, b: Float, a: Float): this() {
        data[0] = r; data[1] = v; data[2] = b; data[3] = a
    }

    constructor(@ColorInt color: Int): this() {
        data[0] = red(color)   / 255f // R
        data[1] = green(color) / 255f // G
        data[2] = blue(color)  / 255f // B
        data[3] = alpha(color) / 255f // A
    }
    constructor(context: Context, @ColorRes color: Int): this(context.getColor(color))

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(color: android.graphics.Color): this(color.toArgb())
}