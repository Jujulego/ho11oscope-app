package net.capellari.julien.ho11oscope

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.capellari.julien.ho11oscope.youtube.YoutubePlayerFragment

class PlayerFragment : Fragment() {
    // Companion (equiv to static)
    companion object {
        // Attributs
        const val TAG = "PlayerFragment"

        const val ARGS_TYPE   = "type"
        const val ARGS_VALEUR = "valeur"
    }

    // Enumération
    interface IType {
        fun fragment(value: String): Fragment
    }

    enum class Type: IType {
        YOUTUBE {
            override fun fragment(value: String): Fragment = YoutubePlayerFragment.newInstance(value)
        }
    }

    // Propriétés
    private val type: String?
        get() = arguments?.getString(ARGS_TYPE)

    private val valeur: String?
        get() = arguments?.getString(ARGS_VALEUR)

    // Events
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.player_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Setup !
        setupPlayer()
    }

    private fun setupPlayer() {
        type?.let {
            val type = Type.valueOf(it)

            valeur?.let { valeur ->
                val frag = type.fragment(valeur)

                // Add fragment
                childFragmentManager.beginTransaction()
                        .apply {
                            replace(R.id.fragment_placeholder, frag)
                        }.commit()
            }
        }
    }
}