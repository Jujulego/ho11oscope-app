package net.capellari.julien.data.wrappers

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import net.capellari.julien.data.Noeud
import net.capellari.julien.data.Source
import net.capellari.julien.data.base.NoeudImpl
import net.capellari.julien.data.base.SourceImpl

class EditTextWrapper(val edittext: EditText) : NoeudImpl<String>() {
    // Propriétés
    private var editing: Boolean = false
    private var newdata: String? = null

    override val data: String get() = edittext.text.toString()

    // Initialisation
    init {
        edittext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!editing) {
                    editing = true
                    emitData(s.toString())
                    editing = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        edittext.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && newdata != null) {
                edittext.setText(newdata)
                newdata = null
            }
        }
    }

    // Méthodes
    override fun updateData(data: String, origin: Source<String>) {
        if (editing) {
            newdata = data
        } else {
            edittext.setText(data)
        }
    }
}