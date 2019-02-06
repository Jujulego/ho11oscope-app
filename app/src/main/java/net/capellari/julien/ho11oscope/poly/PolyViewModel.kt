package net.capellari.julien.ho11oscope.poly

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import net.capellari.julien.ho11oscope.RequestManager
import net.capellari.julien.opengl.PointLight

class PolyViewModel(app: Application) : AndroidViewModel(app) {
    // Attributs
    private val dataSourceFactory = PolyDataSource.Factory(this)
    private val liveAsset = MutableLiveData<Asset>()

    val requestManager = RequestManager.getInstance(app)
    val assets: LiveData<PagedList<PolyObject>> = LivePagedListBuilder(dataSourceFactory, 20).build()
    val lights = MutableLiveData<ArrayList<PointLight>>()

    var query: String? = null

    // Méthodes
    fun getAsset(): LiveData<Asset> = liveAsset
    fun setAsset(asset: Asset) {
        // Gardiens
        if (asset.id == liveAsset.value?.id) {
            return
        }

        liveAsset.postValue(asset)
    }

    fun invalidate() {
        assets.value?.dataSource?.invalidate()
    }
}