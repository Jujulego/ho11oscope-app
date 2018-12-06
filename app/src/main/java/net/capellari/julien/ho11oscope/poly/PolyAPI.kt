package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpDownload
import com.github.kittinunf.fuel.httpGet
import net.capellari.julien.utils.getSHA1Cert
import org.json.JSONObject
import java.io.File

object PolyAPI {
    // Constantes
    const val TAG = "PolyAPI"

    const val API_KEY = "AIzaSyBtAtm2vpdYXrHxhoS3i8zc0N9VJuO2GxI"
    const val BASE_URL = "https://poly.googleapis.com/v1"

    // Attributs
    val isloading = MutableLiveData<Boolean>()

    // Méthodes
    fun assets(context: Context, vararg args : Pair<String,Any?>, handler : (List<PolyObject>, String?) -> Unit) {
        val listURL = "$BASE_URL/assets"

        isloading.postValue(true)
        listURL.httpGet(listOf("key" to API_KEY, *args))
                .header(
                        "X-Android-Package" to context.packageName,
                        "X-Android-Cert" to context.getSHA1Cert()!!
                )
                .responseJson { req, _, result ->
                    Log.d(TAG, "Response for GET request on : ${req.url}")

                    result.fold({
                        // Assets
                        val json = it.obj()
                        val assets = it.obj().getJSONArray("assets")
                        val liste = mutableListOf<PolyObject>()

                        for (i in 0 until assets.length()) {
                            // Get infos
                            val obj = assets.getJSONObject(i)

                            Log.d(TAG, "(ID: ${obj.getString("name")} -- ${obj.getString("displayName")}")
                            liste.add(PolyObject(
                                    id          = obj.getString("name"),
                                    name        = obj.getString("displayName"),
                                    description = if (obj.has("descricption")) obj.getString("description") else null,
                                    imageUrl    = if (obj.has("thumbnail")) obj.getJSONObject("thumbnail").getString("url") else null
                            ))
                        }

                        handler(liste, if (json.has("nextPageToken")) json.getString("nextPageToken") else null)
                        isloading.postValue(false)
                    }, {
                        Log.e(TAG, "Error while parsing JSON", it)
                        isloading.postValue(false)
                    })
                }
    }

    fun asset(context: Context, id: String, handler: (AssetData) -> Unit) {
        val assetURL = "$BASE_URL/$id"

        assetURL.httpGet(listOf("key" to API_KEY))
                .header(
                        "X-Android-Package" to context.packageName,
                        "X-Android-Cert" to context.getSHA1Cert()!!
                )
                .responseJson { _, _, result ->
                    result.fold({
                        // Assets
                        val formats = it.obj().getJSONArray("formats")

                        for (i in 0 until formats.length()) {
                            val format = formats.getJSONObject(i)

                            // Obj format
                            if (format.getString("formatType") == "OBJ") {
                                val files = AssetData()

                                // .obj file
                                files.root = AssetFile(format.getJSONObject("root"))

                                // ressources (.mtl + textures)
                                val res = format.getJSONArray("resources")
                                for (j in 0 until res.length()) {
                                    files.ressources.add(AssetFile(res.getJSONObject(j)))
                                }

                                // fini !
                                handler(files)
                                break
                            }
                        }
                    }, {
                        Log.e(TAG, "Error while parsing JSON", it)
                    })
                }
    }

    // Classes
    class AssetData {
        // Attributs
        var root: AssetFile? = null
        var ressources = mutableListOf<AssetFile>()

        // Méthodes
        fun download(context: Context, done: () -> Unit, failed: (text : String) -> Unit = { Log.w(TAG, it) }) {
            var nbDwl = ressources.size + 1
            var fail = false

            val dwlDone : (File) -> Unit = {
                Log.d(TAG, "${it.name} downloaded !")
                if (!fail) {
                    if (nbDwl == 1) {
                        done()
                    } else {
                        nbDwl--
                    }
                }
            }
            val dwlFailed : (String) -> Unit = {
                if (!fail) {
                    fail = true
                    failed(it)
                }
            }

            root?.let { root ->
                root.download(context, dwlDone, dwlFailed)

                for (res in ressources) {
                    res.download(context, dwlDone, dwlFailed)
                }
            } ?: failed("No root file !")
        }
    }

    data class AssetFile(val name : String, val url : String, val type : String) {
        // Constructeurs
        constructor(obj : JSONObject)
                : this(obj.getString("relativePath"), obj.getString("url"), obj.getString("contentType"))

        // Méthodes
        fun getFile(context: Context) : File {
            return File(context.filesDir, name)
        }

        fun downloaded(context: Context) : Boolean {
            return getFile(context).exists()
        }

        fun download(context: Context,
                     done : (file : File) -> Unit,
                     failed : (text : String) -> Unit = { Log.w(TAG, it) }) {
            val file = getFile(context)

            url.httpDownload()
                    .destination { _, _ -> file }
                    .response { _, _, result ->
                        result.fold(
                            { done(file) },
                            { failed("Unable to download $name !") }
                        )
                    }
        }
    }
}