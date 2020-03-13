package nl.joery.demo.animatedbottombar

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import kotlinx.android.synthetic.main.activity_view_pager.*

class ViewPagerActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager)

        view_pager.adapter = ViewPagerAdapter(supportFragmentManager)
        bottom_bar.setupWithViewPager(view_pager)
    }

    class ViewPagerAdapter
    constructor(fm: FragmentManager?) :
        FragmentStatePagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return SampleFragment.newInstance("This is Fragment #" + (position + 1))
        }

        override fun getCount(): Int {
            return 5
        }
    }
}