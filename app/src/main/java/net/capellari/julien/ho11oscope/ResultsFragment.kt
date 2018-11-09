package net.capellari.julien.ho11oscope

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.result_item.view.*
import kotlinx.android.synthetic.main.results_fragment.view.*

class ResultsFragment : Fragment() {
    // Attributs
    private lateinit var results: ResultsViewModel

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        // ViewModel
        results = ViewModelProviders.of(activity!!)[ResultsViewModel::class.java]
        results.isRefreshing().observe(this, Observer<Boolean> {
            (view as? SwipeRefreshLayout)?.isRefreshing = it
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.results_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Swipe to refresh
        (view as SwipeRefreshLayout).let {
            it.setColorSchemeResources(R.color.colorPrimary)
            it.setOnRefreshListener {
                for (l in results.listeners) l.onRefresh()
            }
        }

        // Recycler view
        view.results.let {
            it.adapter = results.adapter
            it.layoutManager = LinearLayoutManager(context)
        }
    }
}