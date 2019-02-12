package net.capellari.julien.data.preferences

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
import net.capellari.julien.data.Linker
import net.capellari.julien.data.R
import net.capellari.julien.data.Source
import net.capellari.julien.data.base.SinkImpl
import net.capellari.julien.data.wrappers.SeekBarWrapper
import net.capellari.julien.data.wrappers.TextViewWrapper
import net.capellari.julien.utils.parcelableCreator

class SeekBarPreference : Preference {
    // Companion
    companion object {
        // Constantes
        const val TAG = "SeekBarPreference"
    }

    // Attributs
    private val linker = Linker(0)
    private val showValue: Boolean

    private val sink = object : SinkImpl<Int>() {
        override fun updateData(data: Int, origin: Source<Int>) {
            if (callChangeListener(value)) {
                persistInt(value)
            }
        }
    }

    // Constructeurs
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes) {

        // Linker initialisation
        linker.addSink(sink)

        // Get attributes
        val a = context.obtainStyledAttributes(attrs, androidx.preference.R.styleable.SeekBarPreference, defStyleAttr, defStyleRes)

        linker["min"] = a.getInt(androidx.preference.R.styleable.SeekBarPreference_min, 0)
        linker["max"] = a.getInt(androidx.preference.R.styleable.SeekBarPreference_android_max, 100)
        // TODO: androidx.preference.R.styleable.SeekBarPreference_seekBarIncrement
        // TODO: androidx.preference.R.styleable.SeekBarPreference_adjustable

        showValue = a.getBoolean(androidx.preference.R.styleable.SeekBarPreference_showSeekBarValue, true)
        linker["format"] = a.getString(R.styleable.SeekBarPreference_format)

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

        // Setup textview
        val textview = view.findViewById(androidx.preference.R.id.seekbar_value) as TextView
        textview.visibility = if (showValue) View.VISIBLE else View.GONE

        // Setup seekbar
        val seekbar = view.findViewById(androidx.preference.R.id.seekbar) as? SeekBar
        if (seekbar == null) {
            Log.e(TAG, "SeekBar view is null in onBindViewHolder.")
            return
        }

        linker.link(SeekBarWrapper(seekbar))
        if (showValue) {
            linker.link(TextViewWrapper(textview))
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
        state.value = value
        state.min = min
        state.max = max

        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state.javaClass != SavedState::class.java) {
            super.onRestoreInstanceState(state)
            return
        }

        val s = state as SavedState
        super.onRestoreInstanceState(s.superState)

        linker.data = s.value
        linker["min"] = s.min
        linker["max"] = s.max

        notifyChanged()
    }

    // Méthodes
    private fun setValueInternal(v: Int, notify: Boolean) {
        if (value != v) {
            linker.data = v

            persistInt(v)
            if (notify) notifyChanged()
        }
    }

    // Propriétés
    var min: Int get() = linker["min"]!! as Int
        set(v) {
            if (min != v) {
                linker["min"] = v
                notifyChanged()
            }
        }

    var max: Int get() = linker["max"]!! as Int
        set(v) {
            if (max != v) {
                linker["max"] = v
                notifyChanged()
            }
        }

    var value: Int get() = linker.data
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