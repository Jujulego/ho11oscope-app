package net.capellari.julien.ho11oscope.poly

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import org.jetbrains.anko.doAsync

class PolyDataSource : PageKeyedDataSource<String,PolyObject>() {
    // Classes
    class Factory : DataSource.Factory<String,PolyObject>() {
        // Attributs
        val sourceLiveData = MutableLiveData<PolyDataSource>()

        // Méthodes
        override fun create(): DataSource<String, PolyObject> {
            val source = PolyDataSource()
            sourceLiveData.postValue(source)
            return source
        }
    }

    // Méthodes
    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, PolyObject>) {
        doAsync {
            PolyAPI.assets(
                    "category" to "animals",
                    "format"   to "OBJ",
                    "pageSize" to params.requestedLoadSize) { liste, token ->
                callback.onResult(liste, null, token)
            }
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, PolyObject>) {
        doAsync {
            PolyAPI.assets(
                    "pageToken" to params.key,
                    "pageSize"  to params.requestedLoadSize) { liste, token ->
                callback.onResult(liste, token)
            }
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, PolyObject>) {
        doAsync {
            PolyAPI.assets(
                    "pageToken" to params.key,
                    "pageSize"  to params.requestedLoadSize) { liste, token ->
                callback.onResult(liste, token)
            }
        }
    }
}