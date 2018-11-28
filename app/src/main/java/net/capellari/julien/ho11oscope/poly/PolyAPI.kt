package net.capellari.julien.ho11oscope.poly

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet

object PolyAPI {
    // Constantes
    const val TAG = "PolyAPI"

    const val API_KEY = "AIzaSyBtAtm2vpdYXrHxhoS3i8zc0N9VJuO2GxI"
    const val BASE_URL = "https://poly.googleapis.com/v1"

    val isloading = MutableLiveData<Boolean>()

    // Méthodes
    fun assets(vararg args : Pair<String,Any?>, handler : (PolyObject) -> Unit) {
        val listURL = "$BASE_URL/assets"

        isloading.postValue(true)
        listURL.httpGet(listOf("key" to API_KEY, *args)).responseJson { _, _, result ->
            result.fold({
                // Assets
                val assets = it.obj().getJSONArray("assets")

                for (i in 0 until assets.length()) {
                    // Get infos
                    val obj = assets.getJSONObject(i)

                    Log.d(TAG, "(ID: ${obj.getString("name")} -- ${obj.getString("displayName")}")
                    handler(PolyObject(
                            id          = obj.getString("name"),
                            name        = obj.getString("displayName"),
                            description = if (obj.has("descricption")) obj.getString("description") else null,
                            imageUrl    = if (obj.has("thumbnail")) obj.getJSONObject("thumbnail").getString("url") else null
                    ))
                }
                isloading.postValue(false)
            }, {
                Log.e(TAG, "Error while parsing JSON", it)
                isloading.postValue(false)
            })
        }
    }
    fun assets(vararg args : Pair<String,Any?>, handler : (List<PolyObject>, String) -> Unit) {
        val listURL = "$BASE_URL/assets"

        isloading.postValue(true)
        listURL.httpGet(listOf("key" to API_KEY, *args)).responseJson { req, _, result ->
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

                handler(liste, json.getString("nextPageToken"))
                isloading.postValue(false)
            }, {
                Log.e(TAG, "Error while parsing JSON", it)
                isloading.postValue(false)
            })
        }
    }

    fun asset(id: String, handler: (Asset.Files) -> Unit) {
        val assetURL = "$BASE_URL/$id"

        assetURL.httpGet(listOf(
                "key" to API_KEY
        )).responseJson { _, _, result ->
            result.fold({
                // Assets
                val formats = it.obj().getJSONArray("formats")

                for (i in 0 until formats.length()) {
                    val format = formats.getJSONObject(i)

                    // Obj format
                    if (format.getString("formatType") == "OBJ") {
                        val files = Asset.Files

                        // .obj file
                        files.objFileURL = format.getJSONObject("root")
                                                .getString("url")

                        // .mtl file
                        format.getJSONArray("resources").getJSONObject(0)
                                .apply {
                                    files.mtlFileURL = getString("url")
                                    files.mtlFileName = getString("relativePath")
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
}