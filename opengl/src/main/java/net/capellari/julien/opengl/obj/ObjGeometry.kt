package net.capellari.julien.opengl.obj

import net.capellari.julien.opengl.TexCoord
import net.capellari.julien.opengl.Vec3

class ObjGeometry {
    // Companion
    companion object {
        // Attributs
        const val MISSING = -1
    }

    // Classes
    class Vertex(var index: Int, var texCoordIndex: Int, var normalIndex: Int)
    class Face(var vertices: MutableList<Vertex>, var material: String)

    // Attributs
    // - tableaux (+ tailles)
    private var vertices = mutableListOf<Vec3>()
    val vertexCount: Int get() = vertices.size

    private var normals  = mutableListOf<Vec3>()
    val normalCount: Int get() = normals.size

    private var texCoords = mutableListOf<TexCoord>()
    val texCoordCount: Int get() = texCoords.size

    private var faces = mutableListOf<Face>()
    val faceCount: Int get() = faces.size

    // - limites
    var boundsMin: Vec3? = null
        private set
    var boundsMax: Vec3? = null
        private set

    val boundsCenter: Vec3
        get() = boundsMin?.let { boundsMin ->
            boundsMax?.let { boundsMax ->
                boundsMax center boundsMin
            }
        } ?: Vec3()

    val boundsSize: Vec3
        get() = boundsMin?.let { boundsMin ->
            boundsMax?.let { boundsMax ->
                boundsMax - boundsMin
            }
        } ?: Vec3()

    // Méthodes
    private fun parseVec3(s: String): Vec3 {
        val parts = s.trim().split(" +".toRegex())
        if (parts.size != 3) throw ObjParseException("Vec3 doesn't have 3 components.")

        return Vec3(parts[0].toFloat(), parts[1].toFloat(), parts[2].toFloat())
    }

    private fun parseTexCoords(s: String): TexCoord {
        val parts = s.trim().split(" +".toRegex())
        if (parts.size < 2) throw ObjParseException("Tex coords has < 2 components.")

        return TexCoord(parts[0].toFloat(), parts[1].toFloat())
    }

    private fun parseVertex(s: String): Vertex {
        val parts = s.trim().split("/")
        if (parts.isEmpty()) throw ObjParseException("Vertex must have a face index.")

        val index = parts[0].toInt()
        val tcIndex = parts.getOrNull(1)?.toIntOrNull()
        val nIndex = parts.getOrNull(2)?.toIntOrNull()

        return Vertex(
                index - 1,
                tcIndex?.let { it - 1 }
                        ?: MISSING,
                nIndex?.let { it - 1 }
                        ?: MISSING
        )
    }

    private fun parseFace(s: String, material: String): Face {
        val parts = s.trim().split(" +".toRegex())
        if (parts.size < 3) throw ObjParseException("Face must have at least 3 vertices.")

        val vertices = mutableListOf<Vertex>()
        for (p in parts) {
            vertices.add(parseVertex(p))
        }

        return Face(vertices, material)
    }

    private fun encapsulateInBounds(vertex: Vec3) {
        // Minimum
        boundsMin = boundsMin?.apply {
            x = minOf(x, vertex.x)
            y = minOf(y, vertex.y)
            z = minOf(z, vertex.z)
        } ?: Vec3(vertex)

        // Maximum
        boundsMax = boundsMax?.apply {
            x = maxOf(x, vertex.x)
            y = maxOf(y, vertex.y)
            z = maxOf(z, vertex.z)
        } ?: Vec3(vertex)
    }

    fun parse(objFile: String) {
        // Initialisation
        var currentMaterialName: String? = null
        val lines = objFile.split("\n")

        // Parcours
        for (i in 0 until lines.size) {
            try {
                val line = lines[i].trim()

                val space = line.indexOf(' ')
                val verb = if (space >= 0) line.substring(0, space).trim() else line
                val args = if (space >= 0) line.substring(space).trim() else ""

                when (verb) {
                    "v" -> { // Vertex
                        val vertex = parseVec3(args)
                        vertices.add(vertex)
                        encapsulateInBounds(vertex)
                    }
                    "vt" -> { // Texture
                        texCoords.add(parseTexCoords(args))
                    }
                    "vn" -> { // Normal
                        normals.add(parseVec3(args))
                    }
                    "f" -> { // Face
                        faces.add(parseFace(args, currentMaterialName!!))
                    }
                    "usemtl" -> { // Material
                        currentMaterialName = args
                    }
                    else -> {}
                }
            } catch (err: Exception) {
                throw ObjParseException("Failed to parse OBJ, line #${i + 1}", err)
            }
        }

        if (vertices.size <= 0){
            throw ObjParseException("Did not found any vertices in OBJ file.")
        }
    }

    // Accesseurs
    fun getVertex(index: Int): Vec3 = vertices[index]
    fun getNormal(index: Int): Vec3 = normals[index]
    fun getTexCoord(index: Int): TexCoord = texCoords[index]
    fun getFace(index: Int): Face = faces[index]
}