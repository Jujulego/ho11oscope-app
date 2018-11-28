package net.capellari.julien.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.refresh_list_fragment.view.*
import net.capellari.julien.R

abstract class RefreshListFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    // Attributs
    protected var recyclerView: RecyclerView? = null
    protected var swipeRefreshLayout: SwipeRefreshLayout? = null

    // Méthodes
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.refresh_list_fragment, container, false)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Swipe to refresh
        (view as SwipeRefreshLayout).let {
            swipeRefreshLayout = it
            onSwipeRefreshLayoutCreated(it)
        }

        view.results.let {
            recyclerView = it
            onRecyclerViewCreated(it)
        }
    }

    @CallSuper
    open fun onSwipeRefreshLayoutCreated(layout: SwipeRefreshLayout) {
        layout.setOnRefreshListener(this)
    }

    abstract fun onRecyclerViewCreated(view: RecyclerView)
}