package net.capellari.julien.ho11oscope.poly

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import kotlinx.android.synthetic.main.poly_fragment.*
import kotlinx.android.synthetic.main.poly_fragment.view.*
import kotlinx.android.synthetic.main.poly_search_result.view.*
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.ho11oscope.ResultsFragment
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
    private val resultsFragment = ResultsFragment()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Results
        resultsFragment.addListener(object : ResultsFragment.OnResultsListener {
            override fun onRefresh() {
                refresh()
            }

            override fun onItemClick(res: ResultsFragment.Result) {
                (res.obj as? PolyAPI.AssetData)?.let {
                    asset = Asset(it.id)
                    progress.visibility = View.VISIBLE

                    doAsync {
                        asset?.download(requireContext())
                    }
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.poly_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View Pager
        view.pager.adapter = Pager(childFragmentManager)
        view.bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.poly_liste -> { view.pager.currentItem = 0; true }
                else -> false
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Download list
        refresh()
    }

    // Méthodes
    fun refresh() {
        resultsFragment.isRefreshing = true

        PolyAPI.assets {
            resultsFragment.add(ResultsFragment.Result(
                    it.name,
                    obj = it
            ))
            resultsFragment.isRefreshing = false
        }
    }

    // Classe
    inner class Pager(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int = 1

        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0    -> resultsFragment
                else -> null
            }
        }
    }
}