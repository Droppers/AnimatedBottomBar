package nl.joery.animatedbottombar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.Dimension
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager


class AnimatedBottomBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    internal val tabStyle: BottomBarStyle.Tab by lazy { BottomBarStyle.Tab() }
    internal val indicatorStyle: BottomBarStyle.Indicator by lazy { BottomBarStyle.Indicator() }

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: TabAdapter
    private lateinit var tabIndicator: TabIndicator

    init {
        initRecyclerView()
        initAdapter()
        initTabIndicator()
        initAttributes(attrs)
    }

    private fun initAttributes(
        attributeSet: AttributeSet?
    ) {
        tabColorSelected = context.getColorResCompat(android.R.attr.colorPrimary)
        tabColor = context.getColorResCompat(android.R.attr.textColorPrimary)
        indicatorColor = context.getColorResCompat(android.R.attr.colorPrimary)

        val attr: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.AnimatedBottomBar, 0, 0)
        try {
            autoSelectTabs = attr.getBoolean(
                R.styleable.AnimatedBottomBar_abb_autoSelectTabs,
                autoSelectTabs
            )

            // Type
            selectedTabType = TabType.fromId(
                attr.getInt(
                    R.styleable.AnimatedBottomBar_abb_selectedTabType,
                    tabStyle.selectedTabType.id
                )
            ) ?: tabStyle.selectedTabType

            // Animations
            animationTypeSelected = TabAnimationType.fromId(
                attr.getInt(
                    R.styleable.AnimatedBottomBar_abb_animationTypeSelected,
                    tabStyle.animationTypeSelected.id
                )
            ) ?: tabStyle.animationTypeSelected
            animationType = TabAnimationType.fromId(
                attr.getInt(
                    R.styleable.AnimatedBottomBar_abb_animationType,
                    tabStyle.animationType.id
                )
            ) ?: tabStyle.animationType
            animationDuration = attr.getInt(
                R.styleable.AnimatedBottomBar_abb_animationDuration,
                tabStyle.animationDuration.toInt()
            ).toLong()

            // Colors
            tabColorSelected = attr.getColor(
                R.styleable.AnimatedBottomBar_abb_tabColorSelected,
                tabStyle.tabColorSelected
            )
            tabColor =
                attr.getColor(R.styleable.AnimatedBottomBar_abb_tabColor, tabStyle.tabColor)

            // Indicator
            indicatorHeight =
                attr.getInt(
                    R.styleable.AnimatedBottomBar_abb_indicatorHeight,
                    indicatorStyle.indicatorHeight
                )
            indicatorMargin =
                attr.getInt(
                    R.styleable.AnimatedBottomBar_abb_indicatorMargin,
                    indicatorStyle.indicatorMargin
                )
            indicatorColor =
                attr.getColor(
                    R.styleable.AnimatedBottomBar_abb_indicatorColor,
                    indicatorStyle.indicatorColor
                )
            indicatorAppearance = IndicatorAppearance.fromId(
                attr.getInt(
                    R.styleable.AnimatedBottomBar_abb_indicatorAppearance,
                    indicatorStyle.indicatorAppearance.id
                )
            ) ?: indicatorStyle.indicatorAppearance
            indicatorLocation = IndicatorLocation.fromId(
                attr.getInt(
                    R.styleable.AnimatedBottomBar_abb_indicatorLocation,
                    indicatorStyle.indicatorLocation.id
                )
            ) ?: indicatorStyle.indicatorLocation
            indicatorAnimation = attr.getBoolean(
                R.styleable.AnimatedBottomBar_abb_indicatorAnimation,
                indicatorStyle.indicatorAnimation
            )

            // Initials tabs
            val tabsResId = attr.getResourceId(R.styleable.AnimatedBottomBar_abb_tabs, -1);
            val initialIndex = attr.getInt(R.styleable.AnimatedBottomBar_abb_selectedIndex, -1);
            initInitialTabs(tabsResId, initialIndex)
        } finally {
            attr.recycle()
        }
    }

    private fun initRecyclerView() {
        recycler = RecyclerView(context)
        recycler.itemAnimator = null
        recycler.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        val flexLayoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.NOWRAP)
        recycler.layoutManager = flexLayoutManager
        addView(recycler)
    }

    private fun initAdapter() {
        adapter = TabAdapter(this)
        adapter.onTabSelected = { lastIndex: Int, newIndex: Int, animated: Boolean, _: Tab ->
            tabIndicator.setSelectedIndex(lastIndex, newIndex, animated)
        }
        recycler.adapter = adapter
    }

    private fun initTabIndicator() {
        tabIndicator = TabIndicator(this, recycler, adapter)
        recycler.addItemDecoration(tabIndicator)
    }

    private fun initInitialTabs(tabsResId: Int, initialIndex: Int) {
        if (tabsResId == -1) {
            return
        }

        val tabs = MenuParser.parse(context, tabsResId, !isInEditMode)
        for (tab in tabs) {
            addTab(tab)
        }

        if (initialIndex != -1 && !isInEditMode) {
            if (initialIndex < 0 || initialIndex > adapter.tabs.size - 1) {
                throw IndexOutOfBoundsException("attribute 'selectedIndex' is out of bounds.")
            } else {
                setSelectedIndex(initialIndex, false)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Draws the tab indicator again
        recycler.postInvalidate()
    }

    fun addTab(tab: Tab) {
        adapter.addTab(tab)
    }

    fun addTabAt(tabIndex: Int, tab: Tab) {
        adapter.addTab(tab, tabIndex)
    }

    fun removeTab(tab: Tab) {
        adapter.removeTab(tab)
    }

    fun removeTabAt(tabIndex: Int) {
        if (tabIndex < 0 || tabIndex >= adapter.tabs.size) {
            throw IndexOutOfBoundsException("Tab index is out of bounds.")
        }

        val tab = adapter.tabs[tabIndex]
        adapter.removeTab(tab)
    }

    fun setSelectedIndex(tabIndex: Int, animate: Boolean = true) {
        if (tabIndex < 0 || tabIndex >= adapter.tabs.size) {
            throw IndexOutOfBoundsException("Tab index is out of bounds.")
        }

        val tab = adapter.tabs[tabIndex]
        setSelectedTab(tab, animate)
    }

    fun setSelectedTab(tab: Tab, animate: Boolean = true) {
        adapter.selectTab(tab, animate)
    }

    private fun applyTabStyle(type: BottomBarStyle.StyleUpdateType) {
        adapter.applyTabStyle(type)
    }

    private fun applyIndicatorStyle() {
        tabIndicator.applyStyle()
    }

    val tabs
        get() = ArrayList(adapter.tabs)

    val tabCount
        get() = adapter.tabs.size

    val selectedTab
        get() = adapter.selectedTab

    val selectedIndex
        get() = adapter.selectedIndex

    var autoSelectTabs: Boolean = false

    // Item type
    var selectedTabType
        get() = tabStyle.selectedTabType
        set(value) {
            tabStyle.selectedTabType = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.TAB_TYPE)
        }

    // Animations
    var animationTypeSelected
        get() = tabStyle.animationTypeSelected
        set(value) {
            tabStyle.animationTypeSelected = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.ANIMATIONS)
        }

    var animationType
        get() = tabStyle.animationType
        set(value) {
            tabStyle.animationType = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.ANIMATIONS)
        }

    var animationDuration
        get() = tabStyle.animationDuration
        set(value) {
            tabStyle.animationDuration = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.ANIMATIONS)
        }

    var animationInterpolator
        get() = tabStyle.animationInterpolator
        set(value) {
            tabStyle.animationInterpolator = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.ANIMATIONS)
        }

    // Colors
    var tabColorSelected
        @ColorInt
        get() = tabStyle.tabColorSelected
        set(@ColorInt value) {
            tabStyle.tabColorSelected = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.COLORS)
        }

    var tabColorSelectedRes
        @Deprecated("", level = DeprecationLevel.HIDDEN)
        get() = Int.MIN_VALUE
        set(@ColorRes value) {
            tabStyle.tabColorSelected = ContextCompat.getColor(context, value)
            applyTabStyle(BottomBarStyle.StyleUpdateType.COLORS)
        }

    var tabColor
        @ColorInt
        get() = tabStyle.tabColor
        set(@ColorInt value) {
            tabStyle.tabColor = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.COLORS)
        }

    var tabColorRes
        @Deprecated("", level = DeprecationLevel.HIDDEN)
        get() = Int.MIN_VALUE
        set(@ColorRes value) {
            tabStyle.tabColor = ContextCompat.getColor(context, value)
            applyTabStyle(BottomBarStyle.StyleUpdateType.COLORS)
        }

    // Indicator
    var indicatorHeight
        @Dimension
        get() = indicatorStyle.indicatorHeight
        set(@Dimension value) {
            indicatorStyle.indicatorHeight = value
            applyIndicatorStyle()
        }

    var indicatorMargin
        @Dimension
        get() = indicatorStyle.indicatorMargin
        set(@Dimension value) {
            indicatorStyle.indicatorMargin = value
            applyIndicatorStyle()
        }

    var indicatorColor
        @ColorInt
        get() = indicatorStyle.indicatorColor
        set(@ColorInt value) {
            indicatorStyle.indicatorColor = value
            applyIndicatorStyle()
        }

    var indicatorColorRes
        @Deprecated("", level = DeprecationLevel.HIDDEN)
        get() = Int.MIN_VALUE
        set(@ColorRes value) {
            indicatorStyle.indicatorColor = ContextCompat.getColor(context, value)
            applyIndicatorStyle()
        }

    var indicatorAppearance
        get() = indicatorStyle.indicatorAppearance
        set(value) {
            indicatorStyle.indicatorAppearance = value
            applyIndicatorStyle()
        }

    var indicatorLocation
        get() = indicatorStyle.indicatorLocation
        set(value) {
            indicatorStyle.indicatorLocation = value
            applyIndicatorStyle()
        }

    var indicatorAnimation
        get() = indicatorStyle.indicatorAnimation
        set(value) {
            indicatorStyle.indicatorAnimation = value
            applyIndicatorStyle()
        }

    data class Tab(val icon: Drawable?, val title: String, val id: Int = -1)

    enum class TabType(val id: Int) {
        TEXT(0),
        ICON(1);

        companion object {
            fun fromId(id: Int): TabType? {
                for (f in TabType.values()) {
                    if (f.id == id) return f
                }
                throw IllegalArgumentException()
            }
        }
    }

    enum class TabAnimationType(val id: Int) {
        SLIDE(0),
        FADE(1),
        NONE(2);

        companion object {
            fun fromId(id: Int): TabAnimationType? {
                for (f in values()) {
                    if (f.id == id) return f
                }
                throw IllegalArgumentException()
            }
        }
    }

    enum class IndicatorLocation(val id: Int) {
        TOP(0),
        BOTTOM(1);

        companion object {
            fun fromId(id: Int): IndicatorLocation? {
                for (f in values()) {
                    if (f.id == id) return f
                }
                throw IllegalArgumentException()
            }
        }
    }

    enum class IndicatorAppearance(val id: Int) {
        SQUARE(0),
        ROUNDED(1),
        NONE(2);

        companion object {
            fun fromId(id: Int): IndicatorAppearance? {
                for (f in values()) {
                    if (f.id == id) return f
                }
                throw IllegalArgumentException()
            }
        }
    }
}