package net.capellari.julien.ho11oscope

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.support.annotation.IdRes
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem

class DrawerControl(val activity: Activity, val drawerLayout: DrawerLayout, val navView: NavigationView) : NavigationView.OnNavigationItemSelectedListener {
    // Attributs
    lateinit var drawerToggle: ActionBarDrawerToggle
        private set

    // Méthodes
    fun setupDrawer(@IdRes id: Int) {
        // Setup actionbar toggle
        drawerToggle = ActionBarDrawerToggle(
                activity, drawerLayout,
                R.string.nav_open, R.string.nav_close
        )

        drawerLayout.addDrawerListener(drawerToggle)

        // Events
        navView.apply {
            setNavigationItemSelectedListener(this@DrawerControl)
            setCheckedItem(id)
        }
    }

    fun syncState() {
        drawerToggle.syncState()
    }

    // Events
    fun onConfigurationChanged(newConfig: Configuration?) {
        drawerToggle.onConfigurationChanged(newConfig)
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        return drawerToggle.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_main -> {
                activity.startActivity(Intent(activity, MainActivity::class.java))
                true
            }
            R.id.nav_settings -> {
                activity.startActivity(Intent(activity, SettingsActivity::class.java))
                true
            }
            R.id.debug_yt_video_activity -> {
                activity.startActivity(Intent(activity, YoutubeVideoActivity::class.java))
                true
            }
            R.id.debug_player_activity -> {
                activity.startActivity(Intent(activity, PlayerActivity::class.java))
                true
            }
            else -> false
        }
    }
}