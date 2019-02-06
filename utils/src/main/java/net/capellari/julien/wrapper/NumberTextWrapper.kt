package net.capellari.julien.wrapper

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import kotlin.math.max
import kotlin.math.min

class NumberTextWrapper(val editText: EditText) : BaseInputWrapper<Int>(), NumberWrapper<Int> {
    // Attributs
    override var max: Int = Int.MAX_VALUE
    override var min: Int = Int.MIN_VALUE

    private var user: Boolean = false

    // Initialisation
    init {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                synchronized(this) {
                    if (!user) {
                        user = true
                        emit(extract(s))
                        user = false
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) value = max(min(value, max), min)
        }
    }

    // Constructeurs
    constructor(editText: EditText, max: Int, min: Int): this(editText) {
        this.max = max
        this.min = min
    }

    // Méthodes
    private fun extract(txt: Editable): Int {
        var v = txt.toString().toIntOrNull() ?: 0
        v = min(v, max)
        v = max(v, min)

        return v
    }

    // Propriétés
    override var value: Int
        get() = extract(editText.text)
        set(value) {
            if (!user) {
                editText.setText(value.toString())
            }
        }
}