package net.capellari.julien.ho11oscope.poly

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import net.capellari.julien.ho11oscope.R

class PolySettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.poly_preferences)
    }
}