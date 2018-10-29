package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.util.Log
import com.github.kittinunf.fuel.httpDownload
import net.capellari.julien.ho11oscope.poly.opengl.GLUtils
import net.capellari.julien.ho11oscope.poly.opengl.MtlLibrary
import net.capellari.julien.ho11oscope.poly.opengl.ObjGeometry
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.nio.*

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

    interface OnAssetReadyListener {
        fun onReady() {}
    }

    // Attributs
    lateinit var positions:     FloatBuffer
    lateinit var normals:       FloatBuffer
    lateinit var indices:       IntBuffer

    lateinit var ambientColors:  FloatBuffer
    lateinit var diffuseColors:  FloatBuffer
    lateinit var specularColors: FloatBuffer
    lateinit var specularExps:   FloatBuffer
    lateinit var opacities:        FloatBuffer

    val geometry = ObjGeometry()
    val materials = MtlLibrary()

    var vertexCount = 0
    var indexCount = 0

    private var objReady = false
    private var mtlReady = false
    var listener: OnAssetReadyListener? = null

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
            // .obj file
            Log.d(PolyActivity.TAG, "Downloading .obj file ...")

            files.objFileURL!!.httpDownload().destination { _, _ ->
                getObjFile(context)
            }.response { _, _, result ->
                result.fold({
                    doAsync {
                        Log.d(TAG, ".obj file downloaded to ${context.filesDir}")
                        geometry.parse(getObjFile(context).readText())
                        Log.d(TAG, ".obj file parsed")

                        uiThread { _ ->
                            objReady = true
                            if (mtlReady) listener?.onReady()
                        }
                    }
                }, {
                    Log.e(TAG, "Error while downloading file", it)
                })
            }

            // .mtl file
            Log.d(PolyActivity.TAG, "Downloading .mtl file ...")

            files.mtlFileURL!!.httpDownload().destination { _, _ ->
                getMtlFile(context)
            }.response { _, _, result ->
                result.fold({
                    doAsync {
                        Log.d(TAG, ".mtl file downloaded to ${context.filesDir}")
                        materials.parse(getMtlFile(context).readText())
                        Log.d(TAG, ".mtl file parsed")

                        uiThread { _ ->
                            mtlReady = true
                            if (objReady) listener?.onReady()
                        }
                    }
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

        Log.d(TAG, "$vertexCount vertices, $indexCount indices")

        // Allocations
        positions = ByteBuffer.allocateDirect(GLUtils.FLOAT_SIZE * GLUtils.COORDS_PER_VERTEX * vertexCount)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()

        normals = ByteBuffer.allocateDirect(GLUtils.FLOAT_SIZE * GLUtils.COORDS_PER_VERTEX * vertexCount)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()

        indices = ByteBuffer.allocateDirect(GLUtils.INT_SIZE * indexCount)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer()

        ambientColors = ByteBuffer.allocateDirect(GLUtils.FLOAT_SIZE * GLUtils.NUM_COLOR_COMPONENTS * vertexCount)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()

        diffuseColors = ByteBuffer.allocateDirect(GLUtils.FLOAT_SIZE * GLUtils.NUM_COLOR_COMPONENTS * vertexCount)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()

        specularColors = ByteBuffer.allocateDirect(GLUtils.FLOAT_SIZE * GLUtils.NUM_COLOR_COMPONENTS * vertexCount)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()

        specularExps = ByteBuffer.allocateDirect(GLUtils.FLOAT_SIZE * vertexCount)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()

        opacities = ByteBuffer.allocateDirect(GLUtils.FLOAT_SIZE * vertexCount)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()

        // Start at 0
        positions.position(0)
        normals.position(0)
        indices.position(0)
        ambientColors.position(0)
        diffuseColors.position(0)
        specularColors.position(0)
        specularExps.position(0)
        opacities.position(0)

        // Converting
        var currentVertexIndex = 0

        for (i in 0 until geometry.faceCount) {
            // Gather data
            val face = geometry.getFace(i)

            val faceAmbientColor = materials[face.material].ambientColor
            val faceDiffuseColor = materials[face.material].diffuseColor
            val faceSpecularColor = materials[face.material].specularColor
            val faceSpecularExp = materials[face.material].specularExp
            val opacity = materials[face.material].opacity
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

                ambientColors.put(faceAmbientColor)
                diffuseColors.put(faceDiffuseColor)
                specularColors.put(faceSpecularColor)
                specularExps.put(faceSpecularExp)
                opacities.put(opacity)

                ++currentVertexIndex
            }

            // Triangulation of faces
            for (j in 0 until numVerticesInFace-2) {
                indices.put(startVertexIndex)
                    .put(startVertexIndex + j + 1)
                    .put(startVertexIndex + j + 2)
            }
        }
    }
}