package net.capellari.julien.opengl

class TexCoord(var u: Float, var v: Float) {
    // Constructeurs
    constructor(): this(0f, 0f)
    constructor(o: TexCoord): this(o.u, o.v)

    // Opérateurs
    operator fun plus(o: Vec2)  = TexCoord(u + o.x, v + o.y)
    operator fun minus(o: Vec2) = TexCoord(u - o.x, v - o.y)

    // - logic
    override operator fun equals(other: Any?) : Boolean {
        if (other is TexCoord) {
            return (u == other.u) && (v == other.v)
        }

        return false
    }

    // Méthodes
    override fun hashCode(): Int {
        var result = u.hashCode()
        result = 31 * result + v.hashCode()
        return result
    }
}