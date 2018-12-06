package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.util.Log
import com.github.kittinunf.fuel.httpDownload
import net.capellari.julien.opengl.jni.Model
import org.jetbrains.anko.doAsync
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
    lateinit var model: Model

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
        PolyAPI.asset(context, id) { files ->
            // Download files
            // .obj file
            Log.d(PolyFragment.TAG, "Downloading .obj file ...")

            files.objFileURL!!.httpDownload().destination { _, _ ->
                getObjFile(context)
            }.response { _, _, result ->
                result.fold({
                    doAsync {
                        Log.d(TAG, ".obj file downloaded to ${context.filesDir}")

                        synchronized(this@Asset) {
                            objReady = true
                            if (mtlReady) ready(context)
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

                        synchronized(this@Asset) {
                            mtlReady = true
                            if (objReady) ready(context)
                        }
                    }
                }, {
                    Log.e(TAG, "Error while downloading file", it)
                })
            }
        }
    }

    fun ready(context: Context) {
        // Assimp ;)
        model = Model(getObjFile(context).absolutePath)

        // Listener
        ready = true

        for (listener in listeners) {
            listener.onReady()
        }
    }
}