package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.util.Log
import com.github.kittinunf.fuel.httpDownload
import net.capellari.julien.opengl.mtl.MtlLibrary
import net.capellari.julien.opengl.obj.ObjGeometry
import net.capellari.julien.opengl.Vec3
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

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

    // Listeners
    interface OnAssetReadyListener {
        fun onReady()
    }

    // Attributs
    var positions = arrayListOf<Vec3>()
    var normals   = arrayListOf<Vec3>()
    var indices   = arrayListOf<Int>()

    var ambientColors  = arrayListOf<Vec3>()
    var diffuseColors  = arrayListOf<Vec3>()
    var specularColors = arrayListOf<Vec3>()
    var specularExps   = arrayListOf<Float>()
    var opacities      = arrayListOf<Float>()

    val geometry = ObjGeometry()
    val materials = MtlLibrary()

    var vertexCount = 0
    var indexCount = 0

    private var objReady = false
    private var mtlReady = false
    var ready = false
        private set

    private val listeners = mutableSetOf<OnAssetReadyListener>()

    // Méthodes
    fun getObjFile(context: Context): File {
        return File(context.filesDir, "asset.obj")
    }
    fun getMtlFile(context: Context): File {
        return File(context.filesDir, "asset.mtl")
    }
    
    fun addOnReadyListener(listener: OnAssetReadyListener) {
        if (listeners.add(listener) && ready) {
            listener.onReady()
        }
    }
    fun removeOnReadyListener(listener: OnAssetReadyListener) {
        listeners.remove(listener)
    }

    fun download(context: Context) {
        // Recup infos
        PolyAPI.asset(id) { files ->
            // Download files
            // .obj file
            Log.d(PolyFragment.TAG, "Downloading .obj file ...")

            files.objFileURL!!.httpDownload().destination { _, _ ->
                getObjFile(context)
            }.response { _, _, result ->
                result.fold({
                    doAsync {
                        Log.d(TAG, ".obj file downloaded to ${context.filesDir}")
                        geometry.parse(getObjFile(context).readText())
                        Log.d(TAG, ".obj file parsed")

                        synchronized(this@Asset) {
                            objReady = true
                            if (mtlReady) ready()
                        }
                    }
                }, {
                    Log.e(TAG, "Error while downloading file", it)
                })
            }

            // .mtl file
            Log.d(PolyFragment.TAG, "Downloading .mtl file ...")

            files.mtlFileURL!!.httpDownload().destination { _, _ ->
                getMtlFile(context)
            }.response { _, _, result ->
                result.fold({
                    doAsync {
                        Log.d(TAG, ".mtl file downloaded to ${context.filesDir}")
                        materials.parse(getMtlFile(context).readText())
                        Log.d(TAG, ".mtl file parsed")

                        synchronized(this@Asset) {
                            mtlReady = true
                            if (objReady) ready()
                        }
                    }
                }, {
                    Log.e(TAG, "Error while downloading file", it)
                })
            }
        }
    }

    fun ready() {
        val boundsCenter = geometry.boundsCenter
        val boundsSize   = geometry.boundsSize
        val maxSize = maxOf(boundsSize.x, boundsSize.y, boundsSize.z)

        val scale = PolyFragment.ASSET_DISPLAY_SIZE / maxSize
        val translation = boundsCenter * -1f

        Log.d(PolyFragment.TAG, "Will apply translation: $translation, and scale: $scale")

        convertObjAndMtl(translation, scale)

        // Listener
        ready = true

        for (listener in listeners) {
            listener.onReady()
        }
    }

    fun convertObjAndMtl(translation: Vec3, scale: Float) {
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

        // Clear lists
        positions.clear()
        normals.clear()
        indices.clear()
        ambientColors.clear()
        diffuseColors.clear()
        specularColors.clear()
        specularExps.clear()
        opacities.clear()

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
                // Get positions and normals
                var pos = Vec3(geometry.getVertex(vertex.index))
                val normal = if (vertex.normalIndex != ObjGeometry.MISSING) {
                    geometry.getNormal(vertex.normalIndex)
                } else { // Missing normals.
                    // TODO: recompute.
                    Vec3(0f, 0f, 1f)
                }

                // Apply transformations
                pos += translation
                pos *= scale

                // Add to buffers
                positions.add(pos)
                normals.add(normal)

                ambientColors.add(faceAmbientColor)
                diffuseColors.add(faceDiffuseColor)
                specularColors.add(faceSpecularColor)
                specularExps.add(faceSpecularExp)
                opacities.add(opacity)

                ++currentVertexIndex
            }

            // Triangulation of faces
            for (j in 0 until numVerticesInFace-2) {
                indices.apply {
                    add(startVertexIndex)
                    add(startVertexIndex + j + 1)
                    add(startVertexIndex + j + 2)
                }
            }
        }
    }
}