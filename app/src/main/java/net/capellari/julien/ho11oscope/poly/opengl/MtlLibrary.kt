package net.capellari.julien.ho11oscope.poly.opengl

import android.util.Log
import java.lang.RuntimeException

class MtlLibrary {
    // Classes
    data class Material(val name: String) {
        var ambientColor = arrayOf(1f, 1f, 1f)
        var diffuseColor = arrayOf(1f, 1f, 1f)
        var specularColor = arrayOf(1f, 1f, 1f)

        var specularExp = 1f
        var opacity     = 1f

        fun print() {
            Log.d("MtlLibrary", "Material $name:")
            Log.d("MtlLibrary", "   Ka: %.3f %.3f %.3f".format(ambientColor[0],ambientColor[1],ambientColor[2]))
            Log.d("MtlLibrary", "   Kd: %.3f %.3f %.3f".format(diffuseColor[0],diffuseColor[1],diffuseColor[2]))
            Log.d("MtlLibrary", "   Ks: %.3f %.3f %.3f".format(specularColor[0],specularColor[1],specularColor[2]))
            Log.d("MtlLibrary", "   Ns: %.3f".format(specularExp))
            Log.d("MtlLibrary", "   d:  %.3f".format(opacity))
        }
    }

    class MtlParseException : Exception {
        constructor(msg: String) : super(msg)
        constructor(msg: String, cause: Exception) : super(msg, cause)
    }

    // Attributs
    private val materials = mutableMapOf<String, Material>()

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
                    "Ka" -> {
                        material?.let {
                            val parts = args.split(" +".toRegex())
                            if (parts.size < 3) throw Exception("Ka directive had fewer than 3 components: $args")

                            it.ambientColor[0] = parts[0].toFloat()
                            it.ambientColor[1] = parts[1].toFloat()
                            it.ambientColor[2] = parts[2].toFloat()
                        } ?: throw Exception("Ka directive must come after newmtl")
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
                    "Ks" -> {
                        material?.let {
                            val parts = args.split(" +".toRegex())
                            if (parts.size < 3) throw Exception("Ks directive had fewer than 3 components: $args")

                            it.specularColor[0] = parts[0].toFloat()
                            it.specularColor[1] = parts[1].toFloat()
                            it.specularColor[2] = parts[2].toFloat()
                        } ?: throw Exception("Ks directive must come after newmtl")
                    }
                    "Ns" -> {
                        material?.let {
                            val parts = args.split(" +".toRegex())
                            if (parts.isEmpty()) throw Exception("Ns directive had fewer than 1 components: $args")

                            it.specularExp = parts[0].toFloat()
                        } ?: throw Exception("Ns directive must come after newmtl")
                    }
                    "d" -> {
                        material?.let {
                            val parts = args.split(" +".toRegex())
                            if (parts.isEmpty()) throw Exception("d directive had fewer than 1 components: $args")

                            it.opacity = parts[0].toFloat()
                        } ?: throw Exception("d directive must come after newmtl")
                    }
                    "Tr" -> {
                        material?.let {
                            val parts = args.split(" +".toRegex())
                            if (parts.isEmpty()) throw Exception("Tr directive had fewer than 1 components: $args")

                            it.opacity = 1-parts[0].toFloat()
                        } ?: throw Exception("Tr directive must come after newmtl")
                    }
                    else -> {}
                }

            } catch (err: Exception) {
                throw MtlParseException("Failed to parse MTL, line #${i + 1}", err)
            }
        }

        material?.print()
    }

    operator fun get(name: String): Material = materials[name] ?: throw RuntimeException("Material not found: '$name'")
}