package nl.joery.animatedbottombar

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager

internal class TabAdapter(
    private val bottomBar: AnimatedBottomBar,
    private val recycler: RecyclerView
) :
    RecyclerView.Adapter<TabAdapter.TabHolder>() {
    var onTabSelected: ((lastIndex: Int, lastTab: AnimatedBottomBar.Tab?, newIndex: Int, newTab: AnimatedBottomBar.Tab, animated: Boolean) -> Unit)? =
        null
    var onTabReselected: ((newIndex: Int, newTab: AnimatedBottomBar.Tab) -> Unit)? = null
    var onTabIntercepted: ((lastIndex: Int, lastTab: AnimatedBottomBar.Tab?, newIndex: Int, newTab: AnimatedBottomBar.Tab) -> Boolean)? =
        null

    val tabs = ArrayList<AnimatedBottomBar.Tab>()
    var selectedTab: AnimatedBottomBar.Tab? = null
        private set

    var selectedIndex: Int = RecyclerView.NO_POSITION
        private set

    override fun getItemCount(): Int {
        return tabs.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabHolder {
        val v = TabView(parent.context).apply {
            layoutParams = FlexboxLayoutManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            clipChildren = false
            clipToPadding = false
        }
        v.applyStyle(bottomBar.tabStyle)
        return TabHolder(v)
    }

    override fun onBindViewHolder(holder: TabHolder, position: Int) {
        holder.bind(tabs[position])
    }

    override fun onBindViewHolder(holder: TabHolder, position: Int, payloads: List<Any>) {
        when {
            payloads.isEmpty() -> holder.bind(tabs[position])
            else -> {
                val payload = payloads[0] as Payload
                when (payload.type) {
                    PAYLOAD_APPLY_STYLE ->
                        holder.applyStyle(
                            payload.value as BottomBarStyle.StyleUpdateType
                        )
                    PAYLOAD_UPDATE_BADGE ->
                        holder.applyBadge(payload.value as AnimatedBottomBar.Badge?)
                    PAYLOAD_SELECT ->
                        holder.select(payload.value as Boolean)
                    PAYLOAD_DESELECT ->
                        holder.deselect(payload.value as Boolean)
                    PAYLOAD_APPLY_ICON_SIZE ->
                        holder.applyIconSize(payload.value as Int)
                }
            }
        }
    }

    fun addTab(tab: AnimatedBottomBar.Tab, tabIndex: Int = -1) {
        val addedIndex: Int
        if (tabIndex == -1) {
            addedIndex = tabs.size
            tabs.add(tab)
        } else {
            addedIndex = tabIndex
            tabs.add(tabIndex, tab)
        }

        notifyItemInserted(addedIndex)
    }

    fun addTabs(values: Array<out AnimatedBottomBar.Tab>, tabIndex: Int = -1) {
        addTabs(NoCopyArrayList(values), tabIndex)
    }

    fun addTabs(values: Collection<AnimatedBottomBar.Tab>, tabIndex: Int = -1) {
        val startIndex: Int
        if(tabIndex == -1) {
            startIndex = tabs.size
            tabs.addAll(values)
        } else {
            startIndex = tabIndex
            tabs.addAll(startIndex, values)
        }

        notifyItemRangeChanged(startIndex, values.size)
    }

    fun removeTab(tab: AnimatedBottomBar.Tab) {
        val index = tabs.indexOf(tab)
        if(index < 0) {
            return
        }
        removeTabAt(index)
    }

    fun removeTabAt(index: Int) {
        tabs.removeAt(index)
        notifyItemRemoved(index)

        if (tabs.size == 0) {
            selectedTab = null
            selectedIndex = RecyclerView.NO_POSITION
        }
    }

    fun selectTab(tab: AnimatedBottomBar.Tab, animate: Boolean) {
        val index = tabs.indexOf(tab)
        if(index >= 0) {
            selectTabAt(index, animate)
        }
    }

    fun selectTabAt(tabIndex: Int, animate: Boolean) {
        val tab = tabs[tabIndex]
        if (tabIndex == selectedIndex) {
            onTabReselected?.invoke(tabIndex, tab)
            return
        }

        val lastIndex = selectedIndex
        val lastTab = selectedTab

        if (!canSelectTab(lastIndex, lastTab, tabIndex, tab)) {
            return
        }

        if (lastIndex >= 0) {
            notifyItemChanged(lastIndex, Payload(PAYLOAD_DESELECT, animate))
        }
        notifyItemChanged(tabIndex, Payload(PAYLOAD_SELECT, animate))

        selectedTab = tab
        selectedIndex = tabIndex

        onTabSelected?.invoke(
            lastIndex, lastTab,
            tabIndex, tab,
            animate
        )
    }

    fun clearSelection(animate: Boolean) {
        if (selectedTab == null) {
            return
        }

        notifyItemChanged(selectedIndex, Payload(PAYLOAD_DESELECT, animate))

        selectedTab = null
        selectedIndex = RecyclerView.NO_POSITION
    }

    fun applyTabStyle(type: BottomBarStyle.StyleUpdateType) {
        notifyItemRangeChanged(
            0, tabs.size,
            Payload(PAYLOAD_APPLY_STYLE, type)
        )
    }

    fun applyTabBadge(tab: AnimatedBottomBar.Tab, badge: AnimatedBottomBar.Badge?) {
        val index = tabs.indexOf(tab)
        if(index >= 0) {
            applyTabBadgeAt(index, badge)
        }
    }

    fun applyTabBadgeAt(tabIndex: Int, badge: AnimatedBottomBar.Badge?) {
        notifyItemChanged(tabIndex, Payload(PAYLOAD_UPDATE_BADGE, badge))
    }

    fun notifyTabChanged(tab: AnimatedBottomBar.Tab) {
        val index = tabs.indexOf(tab)
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    fun notifyTabChangedAt(index: Int) {
        notifyItemChanged(index)
    }

    fun applyIconSize(tabIndex: Int, iconSize: Int) {
        notifyItemChanged(tabIndex, Payload(PAYLOAD_APPLY_ICON_SIZE, iconSize))
    }

    fun applyIconSize(tab: AnimatedBottomBar.Tab, iconSize: Int) {
        val index = tabs.indexOf(tab)
        if(index >= 0) {
            applyIconSize(index, iconSize)
        }
    }

    private fun canSelectTab(
        lastIndex: Int,
        lastTab: AnimatedBottomBar.Tab?,
        newIndex: Int,
        newTab: AnimatedBottomBar.Tab
    ): Boolean {
        return onTabIntercepted?.invoke(
            lastIndex,
            lastTab,
            newIndex,
            newTab
        ) ?: true
    }

    inner class TabHolder(private val view: TabView) : RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener {
                selectTabAt(bindingAdapterPosition, true)
            }
        }

        fun applyStyle(type: BottomBarStyle.StyleUpdateType) {
            view.applyStyle(type, bottomBar.tabStyle)
        }

        fun applyBadge(badge: AnimatedBottomBar.Badge?) {
            view.badge = badge
        }

        fun select(animate: Boolean) {
            view.select(animate)
        }

        fun deselect(animate: Boolean) {
            view.deselect(animate)
        }

        fun applyIconSize(iconSize: Int) {
            view.iconSize = iconSize
        }

        fun bind(tab: AnimatedBottomBar.Tab) {
            if (tab == selectedTab) {
                select(false)
            } else {
                deselect(false)
            }

            view.title = tab.title
            view.icon = tab.icon
            view.iconSize = tab.iconSize
            view.badge = tab.badge
            view.isEnabled = tab.enabled
        }
    }

    private data class Payload(val type: Int, val value: Any?)

    companion object {
        private const val PAYLOAD_APPLY_STYLE = 0
        private const val PAYLOAD_UPDATE_BADGE = 1
        private const val PAYLOAD_SELECT = 2
        private const val PAYLOAD_DESELECT = 3
        private const val PAYLOAD_APPLY_ICON_SIZE = 4
    }
}