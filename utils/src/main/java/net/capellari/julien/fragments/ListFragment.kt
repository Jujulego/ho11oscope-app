package net.capellari.julien.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import net.capellari.julien.R

abstract class ListFragment : Fragment() {
    // Attributs
    protected var recyclerView: RecyclerView? = null

    // Méthodes
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.list_fragment, container, false)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Swipe to refresh
        (view as RecyclerView).let {
            recyclerView = it
            onRecyclerViewCreated(it)
        }
    }

    abstract fun onRecyclerViewCreated(view: RecyclerView)
}