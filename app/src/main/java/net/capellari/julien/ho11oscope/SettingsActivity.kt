package net.capellari.julien.ho11oscope

import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.MenuItem
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : AppCompatActivity() {
    // Companion (equiv to static)
    companion object {
        const val TAG = "SettingsActivity"
    }

    // Attributs
    private lateinit var drawerControl: DrawerControl

    // events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        // Setup
        setupToolbar()
        drawerControl = DrawerControl(this, drawerLayout, navView)
        drawerControl.setupDrawer(R.id.nav_settings)
    }

    override fun onStart() {
        super.onStart()

        // Sync drawerToggle
        drawerControl.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        // Pass to the drawerToggle
        drawerControl.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return drawerControl.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    // methodes
    private fun setupToolbar() {
        // Set support toolbar
        setSupportActionBar(toolbar)

        // Show home button
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    // inner class
    class SettingsFragment : PreferenceFragmentCompat() {
        // Events
        override fun onCreatePreferences(p0: Bundle?, p1: String?) {
            addPreferencesFromResource(R.xml.preferences)
        }
    }
}