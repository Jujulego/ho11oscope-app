package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.result_item.view.*
import net.capellari.julien.fragments.RefreshListFragment
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.utils.RecyclerHolder
import net.capellari.julien.utils.inflate

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

    inner class PolyHolder(view : View) : RecyclerHolder<PolyObject>(view), View.OnClickListener {
        // Attributs
        var polyObject: PolyObject? = null

        // Initialisation
        init {
            view.setOnClickListener(this)
        }

        // Méthodes
        override fun onBind(value: PolyObject?) {
            polyObject = value

            view.name.text        = value?.name ?: "Loading ..."
            view.description.text = value?.description ?: ""

            value?.imageUrl?.let {
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