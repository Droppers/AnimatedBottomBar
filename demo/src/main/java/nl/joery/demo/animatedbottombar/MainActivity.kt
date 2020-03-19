package nl.joery.demo.animatedbottombar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import nl.joery.animatedbottombar.AnimatedBottomBar
import nl.joery.demo.animatedbottombar.playground.PlaygroundActivity
import java.lang.reflect.Type


class MainActivity : AppCompatActivity() {

    private fun setProperty(instance: Any, property: String, value: Any) {
        val methodName = "set" + property.capitalize()
        val method = instance.javaClass.methods.toList().find { it.name == methodName }
        method?.invoke(instance, value)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomBars = arrayOf(bottom_bar, bottom_bar2, bottom_bar3, bottom_bar4, bottom_bar5)

        bottom_bar.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                Log.d("TAB_SELECTED", "Selected index: $newIndex, title: ${newTab.title}")
            }

            // An optional method that will be fired whenever an already selected tab has been selected again.
            override fun onTabReselected(index: Int, tab: AnimatedBottomBar.Tab) {
                Log.d("TAB_RESELECTED", "Reselected index: $index, title: ${tab.title}")
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
        }

        open_view_pager.setOnClickListener {
            startActivity(Intent(this, ViewPagerActivity::class.java))
        }

        open_playground.setOnClickListener {
            startActivity(Intent(this, PlaygroundActivity::class.java))
        }
    }
}