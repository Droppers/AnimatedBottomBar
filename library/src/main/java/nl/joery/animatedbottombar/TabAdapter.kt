package nl.joery.animatedbottombar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


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
    val selectedIndex: Int
        get() {
            val tabIndex = tabs.indexOf(selectedTab)
            return if (tabIndex >= 0) tabIndex else RecyclerView.NO_POSITION
        }

    override fun getItemCount(): Int {
        return tabs.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabHolder {
        val v: TabView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_tab, parent, false) as TabView
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
                    PayloadType.APPLY_STYLE ->
                        holder.applyStyle(
                            payload.value as BottomBarStyle.StyleUpdateType
                        )
                    PayloadType.UPDATE_BADGE ->
                        holder.applyBadge(payload.value as AnimatedBottomBar.Badge?)
                    PayloadType.SELECT ->
                        holder.select(payload.value as Boolean)
                    PayloadType.DESELECT ->
                        holder.deselect(payload.value as Boolean)
                }
            }
        }
    }

    fun addTab(tab: AnimatedBottomBar.Tab, tabIndex: Int = -1) {
        val addedIndex: Int?
        if (tabIndex == -1) {
            addedIndex = tabs.size
            tabs.add(tab)
        } else {
            addedIndex = tabIndex
            tabs.add(tabIndex, tab)
        }

        notifyItemInserted(addedIndex)
    }

    fun removeTab(tab: AnimatedBottomBar.Tab) {
        val index = tabs.indexOf(tab)
        if (index >= 0) {
            tabs.removeAt(index)
            notifyItemRemoved(index)
        }

        if (tabs.size == 0) {
            selectedTab = null
        }
    }

    fun selectTab(tab: AnimatedBottomBar.Tab, animate: Boolean) {
        val newIndex = tabs.indexOf(tab)
        if (tab == selectedTab) {
            onTabReselected?.invoke(newIndex, tab)
            return
        }

        val lastIndex = tabs.indexOf(selectedTab)
        if (!canSelectTab(lastIndex, selectedTab, newIndex, tab)) {
            return
        }

        if (lastIndex >= 0) {
            notifyItemChanged(lastIndex, Payload(PayloadType.DESELECT, animate))
        }
        notifyItemChanged(newIndex, Payload(PayloadType.SELECT, animate))

        selectedTab = tab

        onTabSelected?.invoke(
            lastIndex,
            if (lastIndex >= 0) tabs[lastIndex] else null,
            newIndex,
            tab,
            animate
        )
    }

    fun clearSelection(animate: Boolean) {
        if (selectedTab == null) {
            return
        }

        val lastIndex = tabs.indexOf(selectedTab)
        notifyItemChanged(lastIndex, Payload(PayloadType.DESELECT, animate))

        selectedTab = null
    }

    fun applyTabStyle(type: BottomBarStyle.StyleUpdateType) {
        notifyItemRangeChanged(
            0, tabs.size,
            Payload(PayloadType.APPLY_STYLE, type)
        )
    }

    fun applyTabBadge(tab: AnimatedBottomBar.Tab, badge: AnimatedBottomBar.Badge?) {
        notifyItemChanged(tabs.indexOf(tab), Payload(PayloadType.UPDATE_BADGE, badge))
    }

    fun notifyTabChanged(tab: AnimatedBottomBar.Tab) {
        val index = tabs.indexOf(tab)
        if (index >= 0) {
            notifyItemChanged(index)
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

    inner class TabHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val view: TabView = v as TabView

        init {
            view.setOnClickListener {
                selectTab(tabs[adapterPosition], true)
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

        fun bind(tab: AnimatedBottomBar.Tab) {
            if (tab == selectedTab) {
                select(false)
            } else {
                deselect(false)
            }

            view.title = tab.title
            view.icon = tab.icon
            view.badge = tab.badge
            view.isEnabled = tab.enabled
        }
    }

    private data class Payload(val type: PayloadType, val value: Any?)
    private enum class PayloadType {
        APPLY_STYLE,
        UPDATE_BADGE,
        SELECT,
        DESELECT
    }
}