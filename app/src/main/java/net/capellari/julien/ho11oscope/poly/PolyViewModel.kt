package net.capellari.julien.ho11oscope.poly

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PolyViewModel(app: Application) : AndroidViewModel(app) {
    // Attributs
    private val liveAsset = MutableLiveData<Asset>()

    // Méthodes
    fun getAsset(): LiveData<Asset> = liveAsset
    fun setAsset(asset: Asset) {
        // Gardiens
        if (asset.id == liveAsset.value?.id) {
            return
        }

        // Add listener : it will be called even if is already ready
        asset.addOnReadyListener(object : Asset.OnAssetReadyListener {
            override fun onReady() = liveAsset.postValue(asset)
        })

        // Lance le téléchargement
        if (!asset.ready) {
            asset.download(getApplication() as Context)
        }
    }
}