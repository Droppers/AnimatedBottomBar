package nl.joery.animatedbottombar

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnimatedBottomBarTest {
    private fun setupBottomBar(): AnimatedBottomBar {
        val context = InstrumentationRegistry.getInstrumentation().context
        val bottomBar = AnimatedBottomBar(context)
        bottomBar.addTab(bottomBar.createTab(R.drawable.alarm, "Tab 1", 1))
        bottomBar.addTab(bottomBar.createTab(R.drawable.alarm, "Tab 2", 2))
        bottomBar.addTab(bottomBar.createTab(R.drawable.alarm, "Tab 3", 3))
        bottomBar.addTab(bottomBar.createTab(R.drawable.alarm, "Tab 4", R.id.tab_with_id))
        return bottomBar
    }

    private fun setupEmptyBottomBar(): AnimatedBottomBar {
        val context = InstrumentationRegistry.getInstrumentation().context
        return AnimatedBottomBar(context)
    }

    @Test
    fun createBottomBar() {
        setupBottomBar()
    }

    @Test
    fun addTab() {
        val bottomBar = setupBottomBar()
        bottomBar.addTab(bottomBar.createTab(R.drawable.alarm, "Tab 5", 5))

        assertEquals(5, bottomBar.tabCount)
        assertEquals(5, bottomBar.tabs.last().id)
    }

    @Test
    fun addTabAt() {
        val bottomBar = setupBottomBar()
        bottomBar.addTabAt(1, bottomBar.createTab(R.drawable.alarm, "Tab At", 999))

        assertEquals(5, bottomBar.tabCount)
        assertEquals(999, bottomBar.tabs[1].id)
        assertEquals(1, bottomBar.tabs[0].id)
        assertEquals(2, bottomBar.tabs[2].id)
    }

    @Test
    fun removeTabAt() {
        val bottomBar = setupBottomBar()
        bottomBar.removeTabAt(1)

        assertEquals(3, bottomBar.tabCount)
        for (tab in bottomBar.tabs) {
            assertNotEquals(2, tab.id)
        }
    }

    @Test
    fun removeTabWithId() {
        val bottomBar = setupBottomBar()
        bottomBar.removeTabById(R.id.tab_with_id)

        assertEquals(3, bottomBar.tabCount)
        for (tab in bottomBar.tabs) {
            assertNotEquals(R.id.tab_with_id, tab.id)
        }
    }

    @Test
    fun removeTabAtEmpty() {
        try {
            val bottomBar = setupEmptyBottomBar()
            bottomBar.removeTabAt(0)
        } catch (e: Exception) {
            assertEquals("Tab index is out of bounds.", e.message)
        }
    }

    @Test
    fun removeTab() {
        val bottomBar = setupBottomBar()
        val removedTab = bottomBar.tabs[3]
        bottomBar.removeTab(removedTab)

        assertEquals(3, bottomBar.tabCount)
        for (tab in bottomBar.tabs) {
            assertNotEquals(removedTab.id, tab.id)
        }
    }

    @Test
    fun selectTabAt() {
        val bottomBar = setupBottomBar()
        bottomBar.selectTabAt(1, false)
        assertEquals(1, bottomBar.selectedIndex)
        bottomBar.selectTabAt(3, false)
        assertEquals(4, bottomBar.selectedTab?.id)
    }

    @Test
    fun selectTabById() {
        val bottomBar = setupBottomBar()
        bottomBar.selectTabById(1, false)
        assertEquals(1, bottomBar.selectedIndex)
        bottomBar.selectTabById(R.id.tab_with_id, false)
        assertEquals(R.id.tab_with_id, bottomBar.selectedTab?.id)
    }

    @Test
    fun selectTabAtIndexOutOfBounds() {
        try {
            val bottomBar = setupBottomBar()
            bottomBar.selectTabAt(100)
        } catch (e: Exception) {
            assertEquals("Tab index is out of bounds.", e.message)
        }
    }

    @Test
    fun selectTabByTab() {
        val bottomBar = setupBottomBar()
        bottomBar.selectTab(bottomBar.tabs[1], false)
        assertEquals(1, bottomBar.selectedIndex)
        bottomBar.selectTab(bottomBar.tabs[3], false)
        assertEquals(4, bottomBar.selectedTab?.id)
    }
}