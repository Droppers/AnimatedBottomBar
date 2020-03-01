package nl.joery.demo.animatedbottombar

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import nl.joery.animatedbottombar.AnimatedBottomBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottom_bar.setOnTabSelectListener(object : AnimatedBottomBar.TabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                Log.d("TAB_SELECTED", "Selected index: $newIndex, title: ${newTab.title}")
            }
        })

//        for (i in 1..4) {
//            bottomBar.addTab(
//                AnimatedBottomBar.Tab(
//                    ContextCompat.getDrawable(
//                        applicationContext,
//                        nl.joery.animatedbottombar.R.drawable.alarm
//                    ), "Tab $i"
//                )
//            )
//        }

        select.setOnClickListener {
            bottom_bar.addTabAt(
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
            if (bottom_bar.tabCount > 0) {
                val tab = bottom_bar.tabs.last()
                bottom_bar.removeTab(tab)
            }
        }

        select_first.setOnClickListener {
            bottom_bar.setSelectedIndex(0)
        }

        select_last.setOnClickListener {
            bottom_bar.setSelectedIndex(bottom_bar.tabCount - 1)
        }
    }
}