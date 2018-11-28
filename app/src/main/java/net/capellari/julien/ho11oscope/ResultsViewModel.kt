package net.capellari.julien.ho11oscope

import android.app.Application
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.result_item.view.*

class ResultsViewModel(app: Application) : AndroidViewModel(app) {
    // Attributs
    val adapter = ResultAdapter()
    val listeners = mutableSetOf<OnResultsListener>()
    val requestManager = RequestManager.getInstance(app)

    private val liveRefreshing = MutableLiveData<Boolean>()

    // Méthodes
    fun isRefreshing(): LiveData<Boolean> = liveRefreshing
    fun setRefreshing(r: Boolean) {
        liveRefreshing.postValue(r)
    }

    fun addOnResultsListener(listener: OnResultsListener) {
        listeners.add(listener)
    }

    fun removeOnResultsListener(listener: OnResultsListener) {
        listeners.remove(listener)
    }

    fun add(result: Result) {
        val s = adapter.liste.size

        adapter.liste.add(result)
        adapter.notifyItemInserted(s)
    }

    fun clear() {
        val s = adapter.liste.size

        adapter.liste.clear()
        adapter.notifyItemRangeRemoved(0, s)
    }

    // Classes
    interface OnResultsListener {
        fun onRefresh()
        fun onItemClick(res: Result)
    }

    data class Result(val name: String,
            val description: String? = null,
            val date: String? = null,
            val imageUrl: String? = null,
            val obj: Any? = null
    )

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
            view.date.text = result.date ?: ""
            view.description.text = result.description ?: ""

            result.imageUrl?.let {
                view.image.setImageUrl(it, requestManager.imageLoader)
            }
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