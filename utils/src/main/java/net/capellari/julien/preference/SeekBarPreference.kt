package net.capellari.julien.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import net.capellari.julien.R
import net.capellari.julien.utils.parcelableCreator
import net.capellari.julien.wrapper.*

class SeekBarPreference : Preference {
    // Companion
    companion object {
        // Constantes
        const val TAG = "SeekBarPreference"
    }

    // Attributs
    private val linker = NumberLinker(0)

    private val showValue: Boolean
    private val format: String?

    private val listener = object : ValueListener<Int> {
        override fun onNewValue(value: Int, from: InputWrapper<Int>) {
            if (callChangeListener(value)) {
                persistInt(value)
            }
        }
    }

    // Constructeurs
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes) {

        // Get attributes
        val a = context.obtainStyledAttributes(attrs, androidx.preference.R.styleable.SeekBarPreference, defStyleAttr, defStyleRes)

        linker.min = a.getInt(androidx.preference.R.styleable.SeekBarPreference_min, 0)
        linker.max = a.getInt(androidx.preference.R.styleable.SeekBarPreference_android_max, 100)
        // TODO: androidx.preference.R.styleable.SeekBarPreference_seekBarIncrement
        // TODO: androidx.preference.R.styleable.SeekBarPreference_adjustable

        showValue = a.getBoolean(androidx.preference.R.styleable.SeekBarPreference_showSeekBarValue, true)
        format    = a.getString(R.styleable.SeekBarPreference_format)

        a.recycle()
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int)
            : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?)
            : this(context, attrs, R.attr.seekBarPreferenceStyle)

    constructor(context: Context) : this(context, null)

    // Events
    override fun onBindViewHolder(view: PreferenceViewHolder) {
        super.onBindViewHolder(view)

        // Setup seekbar
        val seekbar = view.findViewById(androidx.preference.R.id.seekbar) as? SeekBar
        if (seekbar == null) {
            Log.e(TAG, "SeekBar view is null in onBindViewHolder.")
            return
        }

        // Setup textview
        val textview = view.findViewById(androidx.preference.R.id.seekbar_value) as TextView
        textview.visibility = if (showValue) View.VISIBLE else View.GONE

        // Link !
        linker.addValueListener(listener)
        linker.add(SeekBarWrapper(seekbar))

        if (showValue) {
            linker.add(Printer(textview, 0, format))
        }
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        value = getPersistedInt((defaultValue ?: 0) as Int)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int) = a.getInt(index, 0)

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (isPersistent) return superState

        // Save !
        val state = SavedState(superState)
        state.value = linker.value
        state.min = linker.min
        state.max = linker.max

        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state.javaClass != SavedState::class.java) {
            super.onRestoreInstanceState(state)
            return
        }

        val s = state as SavedState
        super.onRestoreInstanceState(s.superState)

        linker.value = s.value
        linker.min = s.min
        linker.max = s.max

        notifyChanged()
    }

    // Méthodes
    private fun setValueInternal(v: Int, notify: Boolean) {
        if (linker.value != v) {
            linker.value = v

            persistInt(v)
            if (notify) notifyChanged()
        }
    }

    // Propriétés
    var min: Int get() = linker.min
        set(v) {
            if (linker.min != v) {
                linker.min = v
                notifyChanged()
            }
        }

    var max: Int get() = linker.max
        set(v) {
            if (linker.max != v) {
                linker.max = v
                notifyChanged()
            }
        }

    var value: Int get() = linker.value
        set(v) { setValueInternal(v, true) }

    // Classe
    class SavedState : BaseSavedState {
        // Companion
        companion object {
            @JvmField val CREATOR = parcelableCreator(::SavedState)
        }

        // Attributs
        var value: Int = 0
        var min: Int = 0
        var max: Int = 0

        // Constructeurs
        constructor(source: Parcel): super(source) {
            value = source.readInt()
            min = source.readInt()
            max = source.readInt()
        }

        constructor(superState: Parcelable): super(superState)

        // Méthodes
        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)

            // Save !
            dest.writeInt(value)
            dest.writeInt(min)
            dest.writeInt(max)
        }
    }
}