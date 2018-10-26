package net.capellari.julien.ho11oscope.poly

import android.util.Log
import java.lang.RuntimeException

class MtlLibrary {
    // Classes
    data class Material(val name: String) {
        var diffuseColor = arrayOf(1f, 1f, 1f, 1f)

        fun print() {
            Log.d("MtlLibrary", "Material $name:")
            Log.d("MtlLibrary", "Kd: %.3f %.3f %.3f %.3f".format(diffuseColor[0],diffuseColor[1],diffuseColor[2],diffuseColor[3]))
        }
    }

    class MtlParseException : Exception {
        constructor(msg: String) : super(msg)
        constructor(msg: String, cause: Exception) : super(msg, cause)
    }

    // Attributs
    private val materials = mutableMapOf<String,Material>()

    // Méthodes
    fun parse(file: String) {
        var material: Material? = null
        val lines = file.split("\n")

        for (i in 0 until lines.size) {
            try {
                val line = lines[i].trim()

                val space = line.indexOf(' ')
                val verb = if (space >= 0) line.substring(0, space).trim() else line
                val args = if (space >= 0) line.substring(space).trim() else ""

                when (verb) {
                    "newmtl" -> {
                        material?.print()

                        material = Material(args)
                        materials[material.name] = material
                    }
                    "Kd" -> {
                        material?.let {
                            val parts = args.split(" +".toRegex())
                            if (parts.size < 3) throw Exception("Kd directive had fewer than 3 components: $args")

                            it.diffuseColor[0] = parts[0].toFloat()
                            it.diffuseColor[1] = parts[1].toFloat()
                            it.diffuseColor[2] = parts[2].toFloat()
                        } ?: throw Exception("Kd directive must come after newmtl")
                    }
                    else -> {}
                }
                material?.print()

            } catch (err: Exception) {
                throw MtlParseException("Failed to parse MTL, line #${i+1}", err)
            }
        }
    }

    operator fun get(name: String): Material = materials[name] ?: throw RuntimeException("Material not found: '$name'")
}