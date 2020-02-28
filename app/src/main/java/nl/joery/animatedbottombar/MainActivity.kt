package nl.joery.animatedbottombar

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import kotlinx.android.synthetic.main.activity_main.*
import nl.joery.library.animatedbottombar.AnimatedBottomBar
import nl.joery.library.animatedbottombar.BottomBarStyle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        select.setOnClickListener {
            bottomBar.addTab(
                AnimatedBottomBar.Tab(
                    ContextCompat.getDrawable(applicationContext, R.drawable.alarm),
                    "Added Tab"
                )
            )
        }

        deselect.setOnClickListener {
            val tab = bottomBar.tabs.last()
            bottomBar.removeTab(tab)
        }
    }
}