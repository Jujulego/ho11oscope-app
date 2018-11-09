package net.capellari.julien.ho11oscope

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.main_activity.*
import net.capellari.julien.utils.sharedPreference

class MainActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    // Companion
    companion object {
        // Constantes
        val PREFERENCE_FRAGMENTS = mapOf(
                "net.capellari.julien.ho11oscope.poly.PolySettingsFragment" to R.id.action_to_rendering_settings
        )
    }

    // Attributs
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var appBarConfiguration: AppBarConfiguration

    // Propriétés
    private var preferenceBrightness by sharedPreference("player_brightness", true)

    private val navController get() = findNavController(R.id.navHostFragment)
    private val isAtTopLevel get()  = navController.currentDestination?.run {isTopLevelDestination(id) } ?: false

    // Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Setup
        setupToolbar()
        setupDrawer()
        setupNavigation()
    }

    override fun onStart() {
        super.onStart()

        // Sync drawerToggle
        drawerToggle.syncState()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus && navController.currentDestination?.id == R.id.playerFragment) {
            hideSystemUI()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        // Pass to the drawerToggle
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController)
                || (if (isAtTopLevel) drawerToggle.onOptionsItemSelected(item) else false)
                || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        PREFERENCE_FRAGMENTS[pref.fragment]?.let {
            navController.navigate(it)
        }

        return true
    }

    // Méthodes
    private fun setupToolbar() {
        // Set support toolbar
        setSupportActionBar(toolbar)

        // Show home button
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }
    private fun setupDrawer() {
        // Setup actionbar toggle
        drawerToggle = ActionBarDrawerToggle(
                this, drawerLayout,
                R.string.nav_open, R.string.nav_close
        )

        drawerLayout.addDrawerListener(drawerToggle)

        // Events
        navigationView.setNavigationItemSelectedListener { item ->
            if (item.itemId != navController.currentDestination?.id) {
                navController.navigate(item.itemId)
            }

            true
        }
    }
    private fun setupNavigation() {
        // ActionBar
        appBarConfiguration = AppBarConfiguration.Builder(
                    R.id.youtubeFragment,
                    R.id.openglFragment,
                    R.id.polyFragment,
                    R.id.settingsFragment
                ).setDrawerLayout(drawerLayout)
                .build()

        setupActionBarWithNavController(navController, appBarConfiguration)

        // Events
        navController.addOnNavigatedListener { _, destination ->
            when (destination.id) {
                R.id.youtubeFragment  -> navigationView.setCheckedItem(R.id.action_drawer_youtube)
                R.id.openglFragment   -> navigationView.setCheckedItem(R.id.action_drawer_opengl)
                R.id.polyFragment     -> navigationView.setCheckedItem(R.id.action_drawer_poly)
                R.id.settingsFragment -> navigationView.setCheckedItem(R.id.action_drawer_settings)
            }

            // Manage SystemUI
            if (destination.id == R.id.playerFragment) {
                hideSystemUI()
            } else {
                showSystemUI()
            }

            // Manage drawer
            if (isTopLevelDestination(destination.id)) {
                drawerToggle.syncState()
                drawerLayout.closeDrawers()
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
    }

    private fun hideSystemUI() {
        // Max brightness
        if (preferenceBrightness) {
            window.attributes.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }

        // Hide toolbar
        toolbar.visibility = View.GONE

        // Immersive mode
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Move layout behind system bars
                or View.SYSTEM_UI_LAYOUT_FLAGS
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                // Hide system bars
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
    private fun showSystemUI() {
        // Default brightness
        if (preferenceBrightness) {
            window.attributes.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }

        // Show toolbar
        toolbar.visibility = View.VISIBLE

        // Normal mode
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    private fun isTopLevelDestination(@IdRes id: Int) = appBarConfiguration.topLevelDestinations.contains(id)
}
