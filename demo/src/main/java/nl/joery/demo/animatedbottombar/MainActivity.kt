package nl.joery.demo.animatedbottombar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import nl.joery.animatedbottombar.AnimatedBottomBar


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomBars = arrayOf(bottom_bar, bottom_bar2, bottom_bar3, bottom_bar4)

        bottom_bar.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                Log.d("TAB_SELECTED", "Selected index: $newIndex, title: ${newTab.title}")
            }
        })

        select.setOnClickListener {
            for (bottomBar in bottomBars) {
                bottomBar.addTabAt(
                    0,
                    bottom_bar.createTab(R.drawable.alarm, R.string.app_name)
                )
            }
        }

        deselect.setOnClickListener {
            for (bottomBar in bottomBars) {
                if (bottomBar.tabCount > 0) {
                    val tab = bottomBar.tabs.last()
                    bottomBar.removeTab(tab)
                }
            }
        }

        select_first.setOnClickListener {
            for (bottomBar in bottomBars) {
                bottomBar.selectTabAt(0)
            }
        }

        select_last.setOnClickListener {
            for (bottomBar in bottomBars) {
                bottomBar.selectTabAt(bottom_bar.tabCount - 1)
            }
            bottom_bar.tabColor = Color.RED
        }

        open_view_pager.setOnClickListener {
            startActivity(Intent(this, ViewPagerActivity::class.java))
        }
    }
}