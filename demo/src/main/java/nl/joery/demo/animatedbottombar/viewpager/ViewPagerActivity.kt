package nl.joery.demo.animatedbottombar.viewpager

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.android.synthetic.main.activity_view_pager.*
import nl.joery.demo.animatedbottombar.R


class ViewPagerActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager)

        initToolbar()

        view_pager.adapter =
            ViewPager2Adapter(
                supportFragmentManager,
                lifecycle
            )
        bottom_bar.setupWithViewPager2(view_pager)
    }

    private fun initToolbar() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.setOnApplyWindowInsetsListener { _, insets ->
            toolbar.setPadding(
                toolbar.paddingLeft,
                toolbar.paddingTop + insets.systemWindowInsetTop,
                toolbar.paddingRight,
                toolbar.paddingBottom
            )
            insets.consumeSystemWindowInsets()
        }
    }

    class ViewPager2Adapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle
    ) :
        FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            return SampleFragment.newInstance(position)
        }
    }
}