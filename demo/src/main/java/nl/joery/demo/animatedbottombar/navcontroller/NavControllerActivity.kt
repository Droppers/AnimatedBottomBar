package nl.joery.demo.animatedbottombar.navcontroller

import android.os.Bundle
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.android.synthetic.main.activity_nav_controller.*
import nl.joery.demo.animatedbottombar.R

class NavControllerActivity : AppCompatActivity(R.layout.activity_nav_controller) {
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController(R.id.main_fragment)
        setupActionBarWithNavController(navController)
        setUpBottomBar()
    }

    private fun setUpBottomBar() {
        val popUpMenu = PopupMenu(this, null)
        popUpMenu.inflate(R.menu.clock_tabs)
        bottom_bar.setupWithNavController(popUpMenu.menu, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        navController.navigateUp()
        return true
    }
}
