package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.util.Log
import com.github.kittinunf.fuel.httpDownload
import net.capellari.julien.ho11oscope.poly.opengl.GLUtils
import net.capellari.julien.ho11oscope.poly.opengl.MtlLibrary
import net.capellari.julien.ho11oscope.poly.opengl.ObjGeometry
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Asset(val id: String) {
    // Companion
    companion object {
        // Constantes
        const val TAG = "Asset"
    }

    // Objet
    object Files {
        var objFileURL:  String? = null
        var mtlFileURL:  String? = null
        var mtlFileName: String? = null
    }

    // Attributs
    lateinit var positions: FloatBuffer
    lateinit var colors:    FloatBuffer
    lateinit var normals:   FloatBuffer
    lateinit var indices:   ShortBuffer

    val geometry = ObjGeometry()
    val materials = MtlLibrary()

    var vertexCount = 0
    var indexCount = 0

    // Méthodes
    fun getObjFile(context: Context): File {
        return File(context.filesDir, "asset.obj")
    }
    fun getMtlFile(context: Context): File {
        return File(context.filesDir, "asset.mtl")
    }

    fun download(context: Context) {
        // Recup infos
        PolyAPI.asset(id) { files ->
            // Download files
            Log.d(PolyActivity.TAG, "Downloading ...")

            // .obj file
            files.objFileURL!!.httpDownload().destination { _, _ ->
                getObjFile(context)
            }.response { _, _, result ->
                result.fold({
                    Log.d(TAG, ".obj file downloaded to ${context.filesDir}")
                    geometry.parse(getObjFile(context).readText())
                    Log.d(TAG, ".obj file parsed")
                }, {
                    Log.e(TAG, "Error while downloading file", it)
                })
            }

            // .mtl file
            files.mtlFileURL!!.httpDownload().destination { _, _ ->
                getMtlFile(context)
            }.response { _, _, result ->
                result.fold({
                    Log.d(TAG, ".mtl file downloaded to ${context.filesDir}")
                    materials.parse(getMtlFile(context).readText())
                    Log.d(TAG, ".mtl file parsed")
                }, {
                    Log.e(TAG, "Error while downloading file", it)
                })
            }
        }
    }

    fun convertObjAndMtl(translation: ObjGeometry.Vec3, scale: Float) {
        // Count entries
        vertexCount = 0
        indexCount = 0

        for (i in 0 until geometry.faceCount) {
            val n = geometry.getFace(i).vertices.size

            if (n >= 3) {
                vertexCount += n
                indexCount += 3 * (n - 2)
            }
        }

        // Allocations
        positions = ByteBuffer.allocateDirect(GLUtils.FLOAT_SIZE * GLUtils.COORDS_PER_VERTEX * vertexCount)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()

        colors = ByteBuffer.allocateDirect(GLUtils.FLOAT_SIZE * GLUtils.NUM_COLOR_COMPONENTS * vertexCount)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()

        normals = ByteBuffer.allocateDirect(GLUtils.FLOAT_SIZE * GLUtils.COORDS_PER_VERTEX * vertexCount)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()

        indices = ByteBuffer.allocateDirect(GLUtils.SHORT_SIZE * indexCount)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()

        // Start at 0
        positions.position(0)
        colors.position(0)
        normals.position(0)
        indices.position(0)

        // Converting
        var currentVertexIndex: Short = 0

        for (i in 0 until geometry.faceCount) {
            // Gather data
            val face = geometry.getFace(i)

            val faceColor = materials[face.material].diffuseColor
            val numVerticesInFace = face.vertices.size
            val startVertexIndex = currentVertexIndex

            // Ignore faces with less than 3 vertices
            if (numVerticesInFace < 3) continue

            // Each vertex
            for (vertex in face.vertices) {
                // Get position and normal
                val pos = ObjGeometry.Vec3(geometry.getVertex(vertex.index))
                val normal = if (vertex.normalIndex != ObjGeometry.MISSING) {
                    geometry.getNormal(vertex.normalIndex)
                } else { // Missing normal.
                    // TODO: recompute.
                    ObjGeometry.Vec3(0f, 0f, 1f)
                }

                // Apply transformations
                pos += translation
                pos *= scale

                // Add to buffers
                positions.put(pos)
                normals.put(normal)
                colors.put(faceColor)

                ++currentVertexIndex
            }

            // Triangulation of faces
            for (j in 0 until numVerticesInFace-2) {
                indices.put(startVertexIndex)
                    .put((startVertexIndex + j + 1).toShort())
                    .put((startVertexIndex + j + 2).toShort())
            }
        }
    }
}