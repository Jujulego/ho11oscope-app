package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import kotlinx.android.synthetic.main.poly_fragment.*
import kotlinx.android.synthetic.main.poly_fragment.view.*
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.ho11oscope.ResultsFragment
import net.capellari.julien.ho11oscope.ResultsViewModel

class PolyFragment : Fragment(), ResultsViewModel.OnResultsListener {
    // Companion
    companion object {
        // Constantes
        const val TAG = "PolyFragment"
        const val ASSET_DISPLAY_SIZE = 5
    }

    // Attributs
    private lateinit var polies: PolyViewModel
    private lateinit var results: ResultsViewModel

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        // ViewModels
        polies = ViewModelProviders.of(activity!!)[PolyViewModel::class.java]
        polies.getAsset().observe(this, Observer<Asset> {
            progress.visibility = View.GONE
            poly_surface.renderer.asset = it
        })

        results = ViewModelProviders.of(activity!!)[ResultsViewModel::class.java]
        results.addOnResultsListener(this)
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
                R.id.poly_liste    -> { view.pager.currentItem = 0; true }
                R.id.poly_settings -> { view.pager.currentItem = 1; true }
                else -> false
            }
        }

        view.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                view.bottomNav.selectedItemId = when (position) {
                    0    -> R.id.poly_liste
                    1    -> R.id.poly_settings
                    else -> R.id.poly_liste
                }
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Download list
        refresh()
    }

    override fun onDetach() {
        super.onDetach()

        // Results
        results.removeOnResultsListener(this)
    }

    override fun onRefresh() {
        refresh()
    }

    override fun onItemClick(res: ResultsViewModel.Result) {
        (res.obj as? PolyAPI.AssetData)?.let {
            progress.visibility = View.VISIBLE
            polies.setAsset(Asset(it.id))
        }
    }

    // Méthodes
    private fun refresh() {
        results.setRefreshing(true)

        PolyAPI.assets {
            results.add(ResultsViewModel.Result(
                    it.name, it.description,
                    imageUrl = it.imageUrl,
                    obj = it
            ))
            results.setRefreshing(false)
        }
    }

    // Classe
    inner class Pager(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int = 2

        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0 -> ResultsFragment.Companion.Builder()
                        .apply {
                            setColumnNumber(1)
                        }.build()

                1    -> PolySettingsFragment()
                else -> null
            }
        }
    }
}