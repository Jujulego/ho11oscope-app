package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.viewpager.widget.ViewPager
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import kotlinx.android.synthetic.main.poly_fragment.*
import kotlinx.android.synthetic.main.poly_fragment.view.*
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.ho11oscope.ResultsFragment
import net.capellari.julien.ho11oscope.ResultsViewModel

class PolyFragment : Fragment(), MenuItem.OnActionExpandListener, ResultsViewModel.OnResultsListener {
    // Companion
    companion object {
        // Constantes
        const val TAG = "PolyFragment"
    }

    // Attributs
    private var searchMenuItem: MenuItem? = null

    private lateinit var polies: PolyViewModel
    private lateinit var results: ResultsViewModel

    // Propriétés
    private val navController by lazy { Navigation.findNavController(this.requireActivity(), R.id.navHostFragment) }

    // Méthodes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate menu
        inflater.inflate(R.menu.poly_toolbar, menu)
        inflater.inflate(R.menu.toolbar_recherche, menu)

        // SearchItem
        searchMenuItem = menu.findItem(R.id.tool_search)
                ?.setOnActionExpandListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.poly_play -> {
                navController.navigate(R.id.action_play_poly)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRefresh() {
        refresh()
    }

    override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
        return false
    }

    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
        return true
    }

    override fun onItemClick(res: ResultsViewModel.Result) {
        (res.obj as? PolyAPI.AssetData)?.let {
            progress.visibility = View.VISIBLE
            polies.setAsset(Asset(it.id))
        }
    }

    override fun onDetach() {
        super.onDetach()

        // Results
        results.clear()
        results.removeOnResultsListener(this)
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