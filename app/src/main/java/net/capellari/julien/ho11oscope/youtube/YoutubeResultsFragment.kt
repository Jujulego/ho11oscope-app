package net.capellari.julien.ho11oscope.youtube

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.api.services.youtube.model.SearchResult
import kotlinx.android.synthetic.main.result_item.view.*
import net.capellari.julien.fragments.RefreshListFragment
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.utils.DiffItemCallback
import net.capellari.julien.utils.RecyclerHolder
import net.capellari.julien.utils.inflate
import org.jetbrains.anko.bundleOf
import java.text.SimpleDateFormat
import java.util.*

class YoutubeResultsFragment : RefreshListFragment() {
    // Companion
    companion object {
        val VIDEO_DIFF_CALLBACK = DiffItemCallback<VideoResult>()
    }

    // Attributs
    private lateinit var ytModel: YoutubeViewModel
    private var adapter = Adapter()

    private val navController by lazy { Navigation.findNavController(this.requireActivity(), R.id.navHostFragment) }

    // Méthodes
    override fun onAttach(context: Context?) {
        super.onAttach(context)

        // ViewModel
        ytModel = ViewModelProviders.of(activity!!)[YoutubeViewModel::class.java]
        ytModel.videos.observe(this, Observer(adapter::submitList))
    }

    override fun onSwipeRefreshLayoutCreated(layout: SwipeRefreshLayout) {
        super.onSwipeRefreshLayoutCreated(layout)

        layout.setColorSchemeResources(R.color.colorPrimary)

        ytModel.isLoading.observe(this, Observer {
            swipeRefreshLayout?.isRefreshing = it
        })
    }

    override fun onRecyclerViewCreated(view: RecyclerView) {
        view.adapter = adapter
        view.itemAnimator = DefaultItemAnimator()
        view.layoutManager = GridLayoutManager(context, context!!.resources.getInteger(R.integer.results_column_number))
    }

    override fun onRefresh() {
        swipeRefreshLayout!!.isRefreshing = true
        ytModel.invalidate()
    }

    // Classes
    inner class Adapter : PagedListAdapter<VideoResult,ViewHolder>(VIDEO_DIFF_CALLBACK) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent.inflate(R.layout.result_item, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    inner class ViewHolder(view: View) : RecyclerHolder<VideoResult>(view), View.OnClickListener {
        // Initialisation
        init {
            view.setOnClickListener(this)
        }

        // Méthodes
        override fun onBind(value: VideoResult?) {
            view.name.text        = value?.title ?: "Loading ..."
            view.description.text = value?.description ?: ""
            view.date.text = value?.publishedAt?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: ""
            value?.imageUrl?.let { view.image.setImageUrl(it, ytModel.requestManager.imageLoader) }
        }

        override fun onClick(v: View?) {
            value?.let {
                navController.navigate(
                        R.id.action_video_details,
                        bundleOf(
                                YoutubeVideoFragment.ARGS_VIDEO_ID          to it.id,
                                YoutubeVideoFragment.ARGS_VIDEO_TITLE       to it.title,
                                YoutubeVideoFragment.ARGS_VIDEO_DESCRIPTION to it.description,
                                YoutubeVideoFragment.ARGS_VIDEO_IMAGE_URL   to it.imageUrl
                        )
                )
            }
        }
    }
}