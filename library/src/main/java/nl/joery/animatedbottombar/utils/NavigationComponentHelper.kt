package nl.joery.animatedbottombar.utils

import android.os.Bundle
import android.view.Menu
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.NavigationUI
import nl.joery.animatedbottombar.AnimatedBottomBar
import nl.joery.animatedbottombar.AnimatedBottomBar.OnTabSelectListener
import java.lang.ref.WeakReference

/**
 * Created by Mayokun Adeniyi on 24/04/2020.
 *
 * Adapted to work with the AnimatedBottomBar library.
 * All credit goes to https://github.com/ibrahimsn98/SmoothBottomBar
 */
internal object NavigationComponentHelper {
    fun setupWithNavController(
        bottomBar: AnimatedBottomBar,
        menu: Menu,
        navController: NavController
    ) {
        bottomBar.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                NavigationUI.onNavDestinationSelected(menu.getItem(newIndex), navController)
            }
        })

        val weakReference = WeakReference(bottomBar)
        navController.addOnDestinationChangedListener(object :
            NavController.OnDestinationChangedListener {

            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                val view = weakReference.get()

                if (view == null) {
                    navController.removeOnDestinationChangedListener(this)
                    return
                }

                for (h in 0 until menu.size()) {
                    val menuItem = menu.getItem(h)
                    if (matchDestination(destination, menuItem.itemId)) {
                        menuItem.isChecked = true
                        bottomBar.selectTabAt(h)
                    }
                }
            }
        })
    }

    private fun matchDestination(
        destination: NavDestination,
        @IdRes destId: Int
    ): Boolean {
        var currentDestination: NavDestination? = destination
        while (currentDestination!!.id != destId && currentDestination.parent != null) {
            currentDestination = currentDestination.parent
        }
        return currentDestination.id == destId
    }
}