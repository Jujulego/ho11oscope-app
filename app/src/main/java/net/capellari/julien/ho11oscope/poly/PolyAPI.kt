package net.capellari.julien.ho11oscope.poly

import android.util.Log
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet

object PolyAPI {
    // Constantes
    const val TAG = "PolyAPI"

    const val API_KEY = "AIzaSyBtAtm2vpdYXrHxhoS3i8zc0N9VJuO2GxI"
    const val BASE_URL = "https://poly.googleapis.com/v1"

    // Classes
    data class AssetData(val id: String, val name: String)

    // Méthodes
    fun assets(handler : (AssetData) -> Unit) {
        val listURL = "$BASE_URL/assets"

        listURL.httpGet(listOf(
                "key" to API_KEY,
                "category" to "animals",
                "format" to "OBJ"
        )).responseJson { _, _, result ->
            result.fold({
                // Assets
                val assets = it.obj().getJSONArray("assets")

                for (i in 0 until assets.length()) {
                    // Get infos
                    val id   = assets.getJSONObject(i).getString("name")
                    val name = assets.getJSONObject(i).getString("displayName")

                    //Log.d(TAG, "(ID: $id) -- $name")
                    handler(AssetData(id, name))
                }
            }, {
                Log.e(TAG, "Error while parsing JSON", it)
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