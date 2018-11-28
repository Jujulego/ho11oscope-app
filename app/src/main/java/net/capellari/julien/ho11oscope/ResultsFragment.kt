package net.capellari.julien.ho11oscope

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.results_fragment.view.*

class ResultsFragment : Fragment() {
    // Companion
    companion object {
        // Builder
        class Builder {
            // Attributs
            private val bundle = Bundle()

            // Méthodes
            fun setColumnNumber(col: Int) : Builder {
                bundle.putInt("columnNumber", col)
                return this
            }

            fun build(): ResultsFragment = ResultsFragment()
                    .apply {
                        arguments = bundle
                    }
        }
    }

    // Attributs
    private lateinit var results: ResultsViewModel

    private val columnNumber by lazy {
        val def = context!!.resources.getInteger(R.integer.results_column_number)
        arguments?.getInt("columnNumber", def) ?: def
    }

    // Events
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
            it.itemAnimator = DefaultItemAnimator()
            it.layoutManager = GridLayoutManager(context, columnNumber)
        }
    }
}