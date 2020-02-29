package nl.joery.library.animatedbottombar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class TabAdapter(
    private val bottomBar: AnimatedBottomBar
) :
    RecyclerView.Adapter<TabAdapter.TabHolder>() {
    var onTabSelected: ((lastIndex: Int, newIndex: Int, tab: AnimatedBottomBar.Tab) -> Unit)? = null

    val tabs = ArrayList<AnimatedBottomBar.Tab>()
    var selectedTab: AnimatedBottomBar.Tab? = null

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
        if (tabs.size == 0) {
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
        } else if (selectedTab == tab) {
            // TODO: Should I even select a tab, is this expected behavior of a tab control?
            // TODO: Maybe add an option 'autoSelectTabs'?
            // Assign a new selected tab after it has been removed
            val newTabIndex = Math.max(0, index - 1)
            selectedTab = tabs.get(newTabIndex)
            notifyItemChanged(
                newTabIndex,
                Payload(PayloadType.SelectTab, false)
            )
        }
    }

    fun selectTab(tab: AnimatedBottomBar.Tab) {
        if (tab == selectedTab) {
            return
        }

        val lastIndex = tabs.indexOf(selectedTab)
        val newIndex = tabs.indexOf(tab)
        selectedTab = tab

        notifyItemChanged(
            lastIndex,
            Payload(PayloadType.DeselectTab, true)
        )
        notifyItemChanged(
            newIndex,
            Payload(PayloadType.SelectTab, true)
        )

        onTabSelected?.invoke(lastIndex, newIndex, tab)
    }

    fun getSelectedIndex(): Int {
        val tabIndex = tabs.indexOf(selectedTab)
        if (tabIndex >= 0) {
            return tabIndex
        }

        return RecyclerView.NO_POSITION
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
                selectTab(tabs[adapterPosition])
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
            view.setText(tab.name)
        }
    }

    private data class Payload(val type: PayloadType, val value: Any)
    private enum class PayloadType {
        ApplyStyle,
        SelectTab,
        DeselectTab
    }
}