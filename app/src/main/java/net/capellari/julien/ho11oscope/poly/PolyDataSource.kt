package net.capellari.julien.ho11oscope.poly

import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource

class PolyDataSource(val polyModel: PolyViewModel) : PageKeyedDataSource<String,PolyObject>() {
    // Classes
    class Factory(val polyModel: PolyViewModel) : DataSource.Factory<String,PolyObject>() {
        // Méthodes
        override fun create(): DataSource<String, PolyObject> {
            return PolyDataSource(polyModel)
        }
    }

    // Méthodes
    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, PolyObject>) {
        if (polyModel.query != null) {
            PolyAPI.assets(polyModel.getApplication(),
                    "keywords" to polyModel.query,
                    "format" to "OBJ",
                    "pageSize" to params.requestedLoadSize) { liste, token ->
                callback.onResult(liste, null, token)
            }
        } else {
            PolyAPI.assets(polyModel.getApplication(),
                    "category" to "animals",
                    "format" to "OBJ",
                    "pageSize" to params.requestedLoadSize) { liste, token ->
                callback.onResult(liste, null, token)
            }
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, PolyObject>) {
        PolyAPI.assets(polyModel.getApplication(),
                "pageToken" to params.key,
                "pageSize"  to params.requestedLoadSize) { liste, token ->
            callback.onResult(liste, token)
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, PolyObject>) {
        PolyAPI.assets(polyModel.getApplication(),
                "pageToken" to params.key,
                "pageSize"  to params.requestedLoadSize) { liste, token ->
            callback.onResult(liste, token)
        }
    }
}