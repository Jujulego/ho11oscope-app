package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.utils.DiffItem
import net.capellari.julien.utils.DiffItemCallback

class PolyObject(val id: String, val name: String, val description: String? = null, val imageUrl: String? = null)
        : DiffItem<PolyObject> {

    // Companion
    companion object {
        val DIFF_CALLBACK = DiffItemCallback<PolyObject>()
    }

    // Méthodes
    override fun isSameItem(other: PolyObject)
            = (other.id == id)

    override fun hasSameContent(other: PolyObject)
            = (other.name == name) && (other.imageUrl == imageUrl) && (other.description == description)
}