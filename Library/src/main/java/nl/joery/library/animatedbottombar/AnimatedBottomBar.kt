package nl.joery.library.animatedbottombar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
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
    private val indicatorStyle: BottomBarStyle.Indicator by lazy { BottomBarStyle.Indicator() }

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: TabAdapter

    init {
        initRecyclerView()
        initAdapter()
        initAttributes(attrs)
    }

    private fun initAttributes(
        attributeSet: AttributeSet?
    ) {
        tabColorSelected = context.getColorResCompat(android.R.attr.colorPrimary)
        tabColor = context.getColorResCompat(android.R.attr.textColorPrimary)

        val attr: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.AnimatedBottomBar, 0, 0)
        try {
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
            // TODO: animationInterpolator = (is this possible with xml?)

            // Colors
            tabColorSelected = attr.getColor(
                R.styleable.AnimatedBottomBar_abb_tabColorSelected,
                tabStyle.tabColorSelected
            )
            tabColor =
                attr.getColor(R.styleable.AnimatedBottomBar_abb_tabColor, tabStyle.tabColor)
        } finally {
            attr.recycle()
        }
    }

    private fun initRecyclerView() {
        recycler = RecyclerView(context)
        recycler.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        val flexLayoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.NOWRAP)
        recycler.layoutManager = flexLayoutManager
        addView(recycler)
    }

    private fun initAdapter() {
        adapter = TabAdapter(this)
        adapter.addTab(Tab(ContextCompat.getDrawable(context, R.drawable.alarm), "hello"))
        adapter.addTab(Tab(ContextCompat.getDrawable(context, R.drawable.alarm), "two"))
        adapter.addTab(Tab(ContextCompat.getDrawable(context, R.drawable.alarm), "three"))
        adapter.addTab(Tab(ContextCompat.getDrawable(context, R.drawable.alarm), "last one"))
        recycler.adapter = adapter
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
        if (tabIndex <= 0 || tabIndex >= adapter.tabs.size) {
            throw IllegalArgumentException("Tab index is out of bounds.")
        }

        val tab = adapter.tabs.get(tabIndex)
        adapter.removeTab(tab)
    }

    private fun applyTabStyle(type: BottomBarStyle.StyleUpdateType) {
        adapter.applyTabStyle(type)
    }

    val tabs
        get() = ArrayList(adapter.tabs)

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
            ?: Color.BLACK // Todo: Return actual default value (android.R.color.textColorPrimary)
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


    data class Tab(val icon: Drawable?, val name: String, val tag: Any? = null)

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
}