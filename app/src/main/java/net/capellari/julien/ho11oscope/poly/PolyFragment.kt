package net.capellari.julien.ho11oscope.poly

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.poly_fragment.*
import kotlinx.android.synthetic.main.poly_fragment.view.*
import kotlinx.android.synthetic.main.result_item.view.*
import net.capellari.julien.fragments.RefreshListFragment
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.ho11oscope.youtube.YoutubeSearchProvider
import net.capellari.julien.utils.inflate
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class PolyFragment : Fragment() {
    // Companion
    companion object {
        // Constantes
        const val TAG = "PolyFragment"
    }

    // Attributs
    private var searchMenuItem: MenuItem? = null

    private lateinit var polyModel: PolyViewModel

    // Propriétés
    private val navController by lazy { Navigation.findNavController(this.requireActivity(), R.id.navHostFragment) }

    private val searchRecentSuggestions
        get() = SearchRecentSuggestions(context, YoutubeSearchProvider.AUTHORITY, YoutubeSearchProvider.MODE)

    private val searchView: SearchView?
        get() = searchMenuItem?.actionView as? SearchView

    // Méthodes
    override fun onAttach(context: Context) {
        super.onAttach(context)

        // ViewModels
        polyModel = ViewModelProviders.of(activity!!)[PolyViewModel::class.java]
        polyModel.getAsset().observe(this, Observer<Asset> {
            progress.visibility = View.VISIBLE

            doAsync {
                // Add listener : it will be called even if is already ready
                it.addOnReadyListener(object : Asset.OnAssetReadyListener {
                    override fun onReady() {
                        poly_surface.renderer.asset = it

                        this@doAsync.uiThread {
                            progress.visibility = View.GONE
                        }
                    }
                })

                // Lance le téléchargement
                if (!it.ready) {
                    it.download(context)
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate menu
        inflater.inflate(R.menu.toolbar_recherche, menu)
        inflater.inflate(R.menu.poly_toolbar, menu)

        // SearchItem
        searchMenuItem = menu.findItem(R.id.tool_search)
        setupSearchView()
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

    // Méthodes
    fun search(query: String?) {
        // Sauvegarde
        searchRecentSuggestions.saveRecentQuery(query, null)

        // Start searching
        query?.let {
            polyModel.query = query
            polyModel.invalidate()
        }
    }

    fun setupSearchView() {
        // Check activity and searchView
        activity?.also { activity ->
            searchView?.apply {
                // Setup
                val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
                setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
                setIconifiedByDefault(true)

                // Restore state
                polyModel.query?.also { query ->
                    searchMenuItem?.expandActionView()
                    setQuery(query, false)
                    clearFocus() // with no focus
                }

                // Listeners
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextChange(newText: String?): Boolean = false
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        search(query)

                        return true
                    }
                })

                setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                    override fun onSuggestionSelect(position: Int): Boolean = false
                    override fun onSuggestionClick(position: Int): Boolean {
                        val cursor = suggestionsAdapter.cursor

                        // Get suggestion selected
                        if (cursor.moveToPosition(position)) {
                            val query = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))

                            // Start searching + update UI
                            search(query)
                            this@apply.setQuery(query, false)

                            return true
                        }

                        return false
                    }
                })
            }
        }
    }

    // Classes
    class Pager(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int = 2

        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0    -> PolyResultsFragment()
                1    -> PolySettingsFragment()
                else -> null
            }
        }
    }

    class PolyResultsFragment : RefreshListFragment() {
        // Attributs
        private lateinit var polyModel: PolyViewModel
        private lateinit var adapter: PolyAdapter

        // Méthodes
        override fun onAttach(context: Context?) {
            super.onAttach(context)

            // ViewModel
            polyModel = ViewModelProviders.of(activity!!)[PolyViewModel::class.java]

            adapter = PolyAdapter()
            polyModel.assets.observe(this, Observer(adapter::submitList))
        }

        override fun onSwipeRefreshLayoutCreated(layout: SwipeRefreshLayout) {
            super.onSwipeRefreshLayoutCreated(layout)

            layout.setColorSchemeResources(R.color.colorPrimary)
            PolyAPI.isloading.observe(this, Observer {
                layout.isRefreshing = it
            })
        }

        override fun onRecyclerViewCreated(view: RecyclerView) {
            view.adapter = adapter
            view.itemAnimator = DefaultItemAnimator()
            view.layoutManager = LinearLayoutManager(context)
        }

        override fun onRefresh() {
            swipeRefreshLayout?.isRefreshing = true
            polyModel.invalidate()
        }

        // Classes
        inner class PolyAdapter: PagedListAdapter<PolyObject, PolyHolder>(PolyObject.DIFF_CALLBACK) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PolyHolder {
                return PolyHolder(parent.inflate(R.layout.result_item, false))
            }

            override fun onBindViewHolder(holder: PolyHolder, position: Int) {
                holder.bind(getItem(position))
            }
        }

        inner class PolyHolder(val view : View) : RecyclerView.ViewHolder(view), View.OnClickListener {
            // Attributs
            var polyObject: PolyObject? = null

            // Initialisation
            init {
                view.setOnClickListener(this)
            }

            // Méthodes
            fun bind(obj : PolyObject?) {
                polyObject = obj

                view.name.text        = obj?.name ?: "Loading ..."
                view.description.text = obj?.description ?: ""

                obj?.imageUrl?.let {
                    view.image.setImageUrl(it, polyModel.requestManager.imageLoader)
                }
            }

            override fun onClick(v: View?) {
                polyObject?.let {
                    polyModel.setAsset(Asset(it.id))
                }
            }
        }
    }
}