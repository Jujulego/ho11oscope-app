package net.capellari.julien.ho11oscope

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.result_item.view.*
import kotlinx.android.synthetic.main.results_fragment.view.*

class ResultsFragment : Fragment() {
    // Attributs
    private val resultAdapter = ResultAdapter()
    private val listeners = mutableSetOf<OnResultsListener>()
    private lateinit var requestManager: RequestManager

    var isRefreshing: Boolean = false
        set(value) {
            field = value

            (view as? SwipeRefreshLayout)?.let {
                it.isRefreshing = value
            }
        }

    // Events
    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Get requestManager
        requestManager = RequestManager.getInstance(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.results_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Swipe to refresh
        (view as SwipeRefreshLayout).let {
            it.isRefreshing = isRefreshing
            it.setColorSchemeResources(R.color.colorPrimary)

            it.setOnRefreshListener {
                for (l in listeners) l.onRefresh()
            }
        }

        // Recycler view
        view.results.let {
            it.adapter = resultAdapter
            it.layoutManager = LinearLayoutManager(context)
        }
    }

    // Méthodes
    fun addListener(listener: OnResultsListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: OnResultsListener) {
        listeners.remove(listener)
    }

    fun add(result: Result) {
        val s = resultAdapter.liste.size

        resultAdapter.liste.add(result)
        resultAdapter.notifyItemInserted(s)
    }

    fun clear() {
        val s = resultAdapter.liste.size

        resultAdapter.liste.clear()
        resultAdapter.notifyItemRangeRemoved(0, s)
    }

    // Classes
    interface OnResultsListener {
        fun onRefresh()
        fun onItemClick(res: Result)
    }

    data class Result(val name: String, val description: String? = null, val date: String? = null, val imageUrl: String? = null, val obj: Any? = null)

    inner class ResultHolder(val view: View) : RecyclerView.ViewHolder(view) {
        // Attributs
        var result: Result? = null

        // Constructeur
        init {
            view.setOnClickListener { for (l in listeners) l.onItemClick(result!!) }
        }

        // Méthodes
        fun bind(result: Result) {
            this.result = result

            // View
            view.name.text = result.name
            result.date?.let { view.date.text = it }
            result.description?.let { view.description.text = it }
            result.imageUrl?.let { view.image.setImageUrl(it, requestManager.imageLoader) }
        }
    }

    inner class ResultAdapter : RecyclerView.Adapter<ResultHolder>() {
        // Attributs
        var liste: ArrayList<Result> = ArrayList()

        // Méthode
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultHolder {
            return ResultHolder(parent.inflate(R.layout.result_item, false))
        }

        override fun getItemCount(): Int = liste.size

        override fun onBindViewHolder(holder: ResultHolder, position: Int) {
            holder.bind(liste[position])
        }
    }
}