package net.capellari.julien.ho11oscope.poly

import androidx.recyclerview.widget.DiffUtil

class PolyObject(val id: String,
                 val name: String,
                 val description: String?,
                 val imageUrl: String?) {
    // Companion
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PolyObject>() {
            override fun areItemsTheSame(oldItem: PolyObject, newItem: PolyObject): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PolyObject, newItem: PolyObject): Boolean {
                return oldItem.name == newItem.name &&
                        oldItem.description == newItem.description &&
                        oldItem.imageUrl == newItem.imageUrl
            }
        }
    }

    // Constructeur
    constructor(id : String, name: String) : this(id, name, null, null)
    constructor(id : String, name: String, description: String) : this(id, name, description, null)
}