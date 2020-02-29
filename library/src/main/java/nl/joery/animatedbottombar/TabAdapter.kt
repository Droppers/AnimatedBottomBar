package nl.joery.animatedbottombar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


internal class TabAdapter(
    private val bottomBar: AnimatedBottomBar
) :
    RecyclerView.Adapter<TabAdapter.TabHolder>() {
    var onTabSelected: ((lastIndex: Int, newIndex: Int, animated: Boolean, tab: AnimatedBottomBar.Tab) -> Unit)? =
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
        return TabHolder(v, this)
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
                    PayloadType.ApplyStyle ->
                        holder.applyStyle(
                            payloads[0] as BottomBarStyle.StyleUpdateType
                        )
                    PayloadType.SelectTab ->
                        holder.select(payload.value as Boolean)
                    PayloadType.DeselectTab ->
                        holder.deselect(payload.value as Boolean)
                }
            }
        }
    }

    fun addTab(tab: AnimatedBottomBar.Tab, tabIndex: Int = -1) {
        // Automatically select a tab when none selected
        if (bottomBar.autoSelectTabs && tabs.size == 0) {
            selectedTab = tab
        }

        val addedIndex: Int?
        if (tabIndex == -1) {
            addedIndex = tabs.size;
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
        } else if (bottomBar.autoSelectTabs && selectedTab == tab) {
            // Automatically select a tab when none selected
            val newTabIndex = Math.max(0, index - 1)
            selectedTab = tabs[newTabIndex]
            notifyItemChanged(
                newTabIndex,
                Payload(PayloadType.SelectTab, false)
            )
        }
    }

    fun selectTab(tab: AnimatedBottomBar.Tab, animate: Boolean) {
        if (tab == selectedTab) {
            return
        }

        val lastIndex = tabs.indexOf(selectedTab)
        val newIndex = tabs.indexOf(tab)
        selectedTab = tab

        notifyItemChanged(
            lastIndex,
            Payload(PayloadType.DeselectTab, animate)
        )
        notifyItemChanged(
            newIndex,
            Payload(PayloadType.SelectTab, animate)
        )

        onTabSelected?.invoke(lastIndex, newIndex, animate, tab)
    }

    fun applyTabStyle(type: BottomBarStyle.StyleUpdateType) {
        notifyItemRangeChanged(
            0, tabs.size,
            Payload(PayloadType.ApplyStyle, type)
        )
    }

    inner class TabHolder(v: View, adapter: TabAdapter) : RecyclerView.ViewHolder(v) {
        private val parent: TabAdapter = adapter
        private val view: TabView = v as TabView

        init {
            view.setOnClickListener {
                selectTab(tabs[adapterPosition], true)
            }
        }

        fun applyStyle(type: BottomBarStyle.StyleUpdateType) {
            view.applyStyle(type, bottomBar.tabStyle)
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

            view.setIcon(tab.icon)
            view.setText(tab.title)
        }
    }

    private data class Payload(val type: PayloadType, val value: Any)
    private enum class PayloadType {
        ApplyStyle,
        SelectTab,
        DeselectTab
    }
}