package nl.joery.demo.animatedbottombar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import nl.joery.animatedbottombar.AnimatedBottomBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        for (i in 1..4) {
            bottomBar.addTab(
                AnimatedBottomBar.Tab(
                    ContextCompat.getDrawable(
                        applicationContext,
                        nl.joery.animatedbottombar.R.drawable.alarm
                    ), "Tab $i"
                )
            )
        }

        select.setOnClickListener {
            bottomBar.addTabAt(
                0,
                AnimatedBottomBar.Tab(
                    ContextCompat.getDrawable(
                        applicationContext,
                        nl.joery.animatedbottombar.R.drawable.alarm
                    ), "Added tab"
                )
            )
        }

        deselect.setOnClickListener {
            if (bottomBar.tabCount > 0) {
                val tab = bottomBar.tabs.last()
                bottomBar.removeTab(tab)
            }
        }

        select_first.setOnClickListener {
            bottomBar.setSelectedIndex(0)
        }

        select_last.setOnClickListener {
            bottomBar.setSelectedIndex(bottomBar.tabCount - 1)
        }
    }
}