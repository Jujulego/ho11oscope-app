package net.capellari.julien.ho11oscope.poly

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.poly_fragment.*
import kotlinx.android.synthetic.main.poly_search_result.view.*
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.ho11oscope.inflate
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class PolyFragment : Fragment() {
    // Companion
    companion object {
        // Constantes
        const val TAG = "PolyFragment"
        const val ASSET_DISPLAY_SIZE = 5
    }

    // Attributs
    private var polyAdapter = PolyAdapter()

    private var asset: Asset? = null
        set(asset) {
            field = asset

            asset?.run {
                listener = object : Asset.OnAssetReadyListener {
                    override fun onReady() {
                        doAsync {
                            val boundsCenter = asset.geometry.boundsCenter
                            val boundsSize   = asset.geometry.boundsSize
                            val maxSize = maxOf(boundsSize.x, boundsSize.y, boundsSize.z)

                            val scale = ASSET_DISPLAY_SIZE / maxSize
                            val translation = boundsCenter * -1f

                            Log.d(TAG, "Will apply translation: $translation, and scale: $scale")

                            asset.convertObjAndMtl(translation, scale)
                            poly_surface.renderer.asset = asset

                            uiThread {
                                progress.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }

    // Events
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.poly_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Recycler view
        results.apply {
            layoutManager = when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> GridLayoutManager(context, 2)
                else -> LinearLayoutManager(context)
            }
            adapter = polyAdapter
            itemAnimator = DefaultItemAnimator()
        }

        // Download
        PolyAPI.assets {
            polyAdapter.add(it)
        }
    }

    // Classes
    inner class PolyAdapter : RecyclerView.Adapter<PolyAdapter.ViewHolder>() {
        // Attributs
        private val liste = arrayListOf<PolyAPI.AssetData>()

        // Méthodes
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent.inflate(R.layout.poly_search_result, false))
        }

        override fun getItemCount(): Int = liste.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.data = liste[position]
        }

        fun add(data: PolyAPI.AssetData) {
            liste.size.let {
                liste.add(data)
                notifyItemInserted(it)
            }
        }

        fun clear() {
            liste.size.let {
                liste.clear()
                notifyItemRangeRemoved(0, it)
            }
        }

        // Classes
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            // Attributs
            private var view: View = itemView
            var data: PolyAPI.AssetData? = null
                set(data) {
                    field = data

                    // update ui
                    data?.let {
                        view.name.text = it.name
                    }
                }

            // Constructeur
            init {
                view.setOnClickListener { _ ->
                    data?.also {
                        asset = Asset(it.id)
                        progress.visibility = View.VISIBLE

                        doAsync {
                            asset?.download(requireContext())
                        }
                    }
                }
            }
        }
    }
}