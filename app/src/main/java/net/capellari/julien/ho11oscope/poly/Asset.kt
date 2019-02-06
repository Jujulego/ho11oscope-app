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
            Log.d(PolyFragment.TAG, "Downloading asset's files ...")
            files.download(context,
                    {
                        Log.d(TAG, "Asset downloaded !")

                        doAsync {
                            ready(context, files)
                        }
                    },
                    {
                        Log.d(TAG, "Asset download failed !")
                    }
            )
        }
    }

    fun ready(context: Context, files: PolyAPI.AssetData) {
        // Assimp ;)
        model = Model(files.root!!.getFile(context).absolutePath)

        // Listener
        ready = true

        for (listener in listeners) {
            listener.onReady()
        }
    }
}