package net.capellari.julien.opengl.mtl

import net.capellari.julien.opengl.Vec3
import java.lang.RuntimeException

class MtlLibrary {
    // Attributs
    private val materials = mutableMapOf<String, Material>()
    val names get() = materials.keys

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
                        //material?.print()

                        material = Material(args)
                        materials[material.name] = material
                    }
                    "Ka" -> {
                        material?.let {
                            val parts = args.split(" +".toRegex())
                            if (parts.size < 3) throw MtlParseException("Ka directive had fewer than 3 components: $args")

                            it.ambientColor = Vec3(parts[0].toFloat(), parts[1].toFloat(), parts[2].toFloat())
                        } ?: throw MtlParseException("Ka directive must come after newmtl")
                    }
                    "Kd" -> {
                        material?.let {
                            val parts = args.split(" +".toRegex())
                            if (parts.size < 3) throw MtlParseException("Kd directive had fewer than 3 components: $args")

                            it.diffuseColor = Vec3(parts[0].toFloat(), parts[1].toFloat(), parts[2].toFloat())
                        } ?: throw MtlParseException("Kd directive must come after newmtl")
                    }
                    "Ks" -> {
                        material?.let {
                            val parts = args.split(" +".toRegex())
                            if (parts.size < 3) throw MtlParseException("Ks directive had fewer than 3 components: $args")

                            it.specularColor = Vec3(parts[0].toFloat(), parts[1].toFloat(), parts[2].toFloat())
                        } ?: throw MtlParseException("Ks directive must come after newmtl")
                    }
                    "Ns" -> {
                        material?.let {
                            val parts = args.split(" +".toRegex())
                            if (parts.isEmpty()) throw MtlParseException("Ns directive had fewer than 1 components: $args")

                            it.specularExp = parts[0].toFloat()
                        } ?: throw MtlParseException("Ns directive must come after newmtl")
                    }
                    "d" -> {
                        material?.let {
                            val parts = args.split(" +".toRegex())
                            if (parts.isEmpty()) throw MtlParseException("d directive had fewer than 1 components: $args")

                            it.opacity = parts[0].toFloat()
                        } ?: throw MtlParseException("d directive must come after newmtl")
                    }
                    "Tr" -> {
                        material?.let {
                            val parts = args.split(" +".toRegex())
                            if (parts.isEmpty()) throw MtlParseException("Tr directive had fewer than 1 components: $args")

                            it.opacity = 1-parts[0].toFloat()
                        } ?: throw MtlParseException("Tr directive must come after newmtl")
                    }
                    else -> {}
                }

            } catch (err: Exception) {
                throw MtlParseException("Failed to parse MTL, line #${i + 1}", err)
            }
        }

        //material?.print()
    }

    // Opérateurs
    operator fun get(name: String): Material = materials[name] ?: throw RuntimeException("Material not found: '$name'")
}