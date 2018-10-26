package net.capellari.julien.ho11oscope.poly

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.poly_activity.*
import net.capellari.julien.ho11oscope.R
import org.jetbrains.anko.doAsync

class PolyActivity : AppCompatActivity() {
    // Companion
    companion object {
        // Constantes
        const val TAG = "PolyActivity"
        const val ASSET = "assets/3yiIERrKNQr"
        const val ASSET_DISPLAY_SIZE = 5
    }

    // Attributs
    private lateinit var asset: Asset

    // Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View
        setContentView(R.layout.poly_activity)

        btn.setOnClickListener {
            doAsync {
                val boundsCenter = asset.geometry.boundsCenter
                val boundsSize   = asset.geometry.boundsSize
                val maxSize = maxOf(boundsSize.x, boundsSize.y, boundsSize.z)

                val scale = ASSET_DISPLAY_SIZE / maxSize
                val translation = boundsCenter * -1f

                Log.d(TAG, "Will apply translation: $translation, and scale: $scale")

                asset.convertObjAndMtl(translation, scale)
                poly_surface.renderer.asset = asset
            }
        }

        // Download
        asset = Asset(ASSET)

        doAsync {
            asset.download(this@PolyActivity)
        }
    }

    // Méthodes
}