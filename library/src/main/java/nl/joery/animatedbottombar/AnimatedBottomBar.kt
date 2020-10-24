package nl.joery.animatedbottombar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import nl.joery.animatedbottombar.utils.*


class AnimatedBottomBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var onTabSelectListener: OnTabSelectListener? = null
    private var onTabInterceptListener: OnTabInterceptListener? = null

    var onTabSelected: (Tab) -> Unit = {}
    var onTabReselected: (Tab) -> Unit = {}
    var onTabIntercepted: (Tab) -> Boolean = { true }

    internal val tabStyle: BottomBarStyle.Tab by lazy { BottomBarStyle.Tab() }
    internal val indicatorStyle: BottomBarStyle.Indicator by lazy { BottomBarStyle.Indicator() }

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: TabAdapter
    private lateinit var tabIndicator: TabIndicator

    private var viewPager: ViewPager? = null
    private var viewPager2: ViewPager2? = null

    init {
        initRecyclerView()
        initAdapter()
        initTabIndicator()
        initAttributes(attrs)
    }

    private fun initAttributes(
        attributeSet: AttributeSet?
    ) {
        tabColorDisabled = context.getTextColor(android.R.attr.textColorSecondary)
        tabColor = context.getTextColor(android.R.attr.textColorPrimary)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rippleColor = android.R.attr.selectableItemBackgroundBorderless
            tabColorSelected = context.getColorResCompat(android.R.attr.colorPrimary)
            indicatorColor = context.getColorResCompat(android.R.attr.colorPrimary)
        } else {
            tabColorSelected = context.getTextColor(android.R.attr.textColorPrimary)
            indicatorColor = context.getTextColor(android.R.attr.textColorPrimary)
        }

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
            tabAnimationSelected = TabAnimation.fromId(
                attr.getInt(
                    R.styleable.AnimatedBottomBar_abb_tabAnimationSelected,
                    tabStyle.tabAnimationSelected.id
                )
            ) ?: tabStyle.tabAnimationSelected
            tabAnimation = TabAnimation.fromId(
                attr.getInt(
                    R.styleable.AnimatedBottomBar_abb_tabAnimation,
                    tabStyle.tabAnimation.id
                )
            ) ?: tabStyle.tabAnimation
            animationDuration = attr.getInt(
                R.styleable.AnimatedBottomBar_abb_animationDuration,
                tabStyle.animationDuration
            )
            animationInterpolator = Utils.loadInterpolator(
                context, attr.getResourceId(
                    R.styleable.AnimatedBottomBar_abb_animationInterpolator,
                    -1
                ), tabStyle.animationInterpolator
            )

            // Ripple
            rippleEnabled = attr.getBoolean(
                R.styleable.AnimatedBottomBar_abb_rippleEnabled,
                tabStyle.rippleEnabled
            )
            rippleColor = attr.getColor(
                R.styleable.AnimatedBottomBar_abb_rippleColor,
                tabStyle.rippleColor
            )

            // Colors
            tabColorSelected = attr.getColor(
                R.styleable.AnimatedBottomBar_abb_tabColorSelected,
                tabStyle.tabColorSelected
            )
            tabColorDisabled = attr.getColor(
                R.styleable.AnimatedBottomBar_abb_tabColorDisabled,
                tabStyle.tabColorDisabled
            )
            tabColor =
                attr.getColor(R.styleable.AnimatedBottomBar_abb_tabColor, tabStyle.tabColor)

            // Text
            textAppearance =
                attr.getResourceId(
                    R.styleable.AnimatedBottomBar_abb_textAppearance,
                    tabStyle.textAppearance
                )
            val textStyle =
                attr.getInt(
                    R.styleable.AnimatedBottomBar_abb_textStyle,
                    typeface.style
                )
            typeface = Typeface.create(typeface, textStyle)
            textSize =
                attr.getDimensionPixelSize(
                    R.styleable.AnimatedBottomBar_abb_textSize,
                    tabStyle.textSize
                )

            // Icon
            iconSize =
                attr.getDimension(
                    R.styleable.AnimatedBottomBar_abb_iconSize,
                    tabStyle.iconSize.toFloat()
                ).toInt()

            // Indicator
            indicatorHeight =
                attr.getDimension(
                    R.styleable.AnimatedBottomBar_abb_indicatorHeight,
                    indicatorStyle.indicatorHeight.toFloat()
                ).toInt()
            indicatorMargin =
                attr.getDimension(
                    R.styleable.AnimatedBottomBar_abb_indicatorMargin,
                    indicatorStyle.indicatorMargin.toFloat()
                ).toInt()
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
            indicatorAnimation = IndicatorAnimation.fromId(
                attr.getInt(
                    R.styleable.AnimatedBottomBar_abb_indicatorAnimation,
                    indicatorStyle.indicatorAnimation.id
                )
            ) ?: indicatorStyle.indicatorAnimation

            // Badge
            badgeAnimation = BadgeAnimation.fromId(
                attr.getInt(
                    R.styleable.AnimatedBottomBar_abb_badgeAnimation,
                    tabStyle.badge.animation.id
                )
            ) ?: tabStyle.badge.animation
            badgeAnimationDuration = attr.getInt(
                R.styleable.AnimatedBottomBar_abb_badgeAnimationDuration,
                tabStyle.badge.animationDuration
            )
            badgeBackgroundColor =
                attr.getColor(
                    R.styleable.AnimatedBottomBar_abb_badgeBackgroundColor,
                    tabStyle.badge.backgroundColor
                )
            badgeTextColor =
                attr.getColor(
                    R.styleable.AnimatedBottomBar_abb_badgeTextColor,
                    tabStyle.badge.textColor
                )
            badgeTextSize =
                attr.getDimensionPixelSize(
                    R.styleable.AnimatedBottomBar_abb_badgeTextSize,
                    tabStyle.badge.textSize
                )

            // Initials tabs
            val tabsResId = attr.getResourceId(R.styleable.AnimatedBottomBar_abb_tabs, -1)
            val initialIndex = attr.getInt(R.styleable.AnimatedBottomBar_abb_selectedIndex, -1)
            val initialTabId =
                attr.getResourceId(R.styleable.AnimatedBottomBar_abb_selectedTabId, -1)
            initInitialTabs(tabsResId, initialIndex, initialTabId)
        } finally {
            attr.recycle()
        }
    }

    private fun initRecyclerView() {
        recycler = RecyclerView(context)
        recycler.itemAnimator = null
        recycler.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        recycler.overScrollMode = View.OVER_SCROLL_NEVER

        val flexLayoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.NOWRAP)
        recycler.layoutManager = flexLayoutManager
        addView(recycler)
    }

    private fun initAdapter() {
        adapter = TabAdapter(this, recycler)
        adapter.onTabSelected =
            { lastIndex: Int, lastTab: Tab?, newIndex: Int, newTab: Tab, animated: Boolean ->
                tabIndicator.setSelectedIndex(lastIndex, newIndex, animated)

                viewPager?.currentItem = newIndex
                viewPager2?.currentItem = newIndex

                onTabSelectListener?.onTabSelected(lastIndex, lastTab, newIndex, newTab)
                onTabSelected.invoke(newTab)
            }
        adapter.onTabReselected =
            { newIndex: Int, newTab: Tab ->
                onTabSelectListener?.onTabReselected(newIndex, newTab)
                onTabReselected.invoke(newTab)
            }
        adapter.onTabIntercepted = { lastIndex: Int, lastTab: Tab?, newIndex: Int, newTab: Tab ->
            onTabInterceptListener?.onTabIntercepted(lastIndex, lastTab, newIndex, newTab)
                ?: onTabIntercepted.invoke(newTab)
        }
        recycler.adapter = adapter
    }

    private fun initTabIndicator() {
        tabIndicator = TabIndicator(this, recycler, adapter)
        recycler.addItemDecoration(tabIndicator)
    }

    private fun initInitialTabs(tabsResId: Int, initialIndex: Int, initialTabId: Int) {
        if (tabsResId == -1) {
            return
        }

        val tabs = MenuParser.parse(context, tabsResId, !isInEditMode)
        for (tab in tabs) {
            addTab(tab)
        }

        if (initialIndex != -1) {
            if (initialIndex < 0 || initialIndex > adapter.tabs.size - 1) {
                throw IndexOutOfBoundsException("Attribute 'selectedIndex' ($initialIndex) is out of bounds.")
            } else {
                selectTabAt(initialIndex, false)
            }
        }

        if (initialTabId != -1) {
            val tab = findTabWithId(initialTabId)
                ?: throw IllegalArgumentException("Attribute 'selectedTabId', tab with this id does not exist.")
            selectTab(tab, false)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec,
            if (layoutParams.height == WRAP_CONTENT) MeasureSpec.makeMeasureSpec(
                64.dpPx,
                MeasureSpec.EXACTLY
            ) else heightMeasureSpec
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        recycler.postInvalidate()
    }

    fun setOnTabSelectListener(onTabSelectListener: OnTabSelectListener) {
        this.onTabSelectListener = onTabSelectListener
    }

    fun setOnTabInterceptListener(onTabInterceptListener: OnTabInterceptListener) {
        this.onTabInterceptListener = onTabInterceptListener
    }

    private fun applyTabStyle(type: BottomBarStyle.StyleUpdateType) {
        adapter.applyTabStyle(type)
    }

    private fun applyIndicatorStyle() {
        tabIndicator.applyStyle()
    }

    /**
     * Creates a new [Tab] instance with the given parameters.
     *
     * @param icon A drawable of the tab icon.
     * @param title The title of the tab.
     * @param id A unique identifier of a tab.
     */
    fun createTab(icon: Drawable?, title: String, @IdRes id: Int = -1): Tab {
        if (icon == null) {
            throw IllegalArgumentException("Icon drawable cannot be null.")
        }
        return Tab(icon, title, id)
    }

    /**
     * Creates a new [Tab] instance with the given parameters.
     *
     * @param iconRes A drawable resource of the tab icon.
     * @param title The title of the tab.
     * @param id A unique identifier of a tab.
     */
    fun createTab(@DrawableRes iconRes: Int, title: String, @IdRes id: Int = -1): Tab {
        val icon = ContextCompat.getDrawable(context, iconRes)
        return createTab(icon, title, id)
    }

    /**
     * Creates a new [Tab] instance with the given parameters.
     *
     * @param iconRes A drawable resource of the tab icon.
     * @param titleRes A string resourceRes of the tab title.
     * @param id A unique identifier of a tab.
     */
    fun createTab(@DrawableRes iconRes: Int, @StringRes titleRes: Int, @IdRes id: Int = -1): Tab {
        val title = context.getString(titleRes)
        return createTab(iconRes, title, id)
    }

    /**
     * Appends the given tab to the end of the BottomBar.
     *
     * @param tab The [Tab] to be appended.
     */
    fun addTab(tab: Tab) {
        adapter.addTab(tab)
    }

    /**
     * Adds the given tab to the specified [tabIndex].
     *
     * @param tabIndex The index the tab needs to be added at.
     * @param tab The [Tab] to be appended.
     */
    fun addTabAt(tabIndex: Int, tab: Tab) {
        adapter.addTab(tab, tabIndex)
    }

    /**
     * Remove a tab from the BottomBar by the specified [tabIndex] index.
     *
     * @param tabIndex The index of the tab to be removed.
     */
    fun removeTabAt(tabIndex: Int) {
        if (tabIndex < 0 || tabIndex >= adapter.tabs.size) {
            throw IndexOutOfBoundsException("Tab index $tabIndex is out of bounds.")
        }

        val tab = adapter.tabs[tabIndex]
        removeTab(tab)
    }

    /**
     * Remove a tab from the BottomBar by the specified tab [id].
     *
     * @param id The id of the tab to be removed.
     */
    fun removeTabById(@IdRes id: Int) {
        val tab =
            findTabWithId(id) ?: throw IllegalArgumentException("Tab with id $id does not exist.")
        removeTab(tab)
    }

    /**
     * Remove a tab from the BottomBar by [Tab] instance, use [tabs] to retrieve a list of tabs.
     *
     * @param tab The [Tab] instance to be removed.
     */
    fun removeTab(tab: Tab) {
        adapter.removeTab(tab)
    }

    /**
     * Select a tab on the BottomBar by the specified [tabIndex] index.
     *
     * @param tabIndex The index of the tab to be selected.
     */
    fun selectTabAt(tabIndex: Int, animate: Boolean = true) {
        if (tabIndex < 0 || tabIndex >= adapter.tabs.size) {
            throw IndexOutOfBoundsException("Tab index $tabIndex is out of bounds.")
        }

        val tab = adapter.tabs[tabIndex]
        selectTab(tab, animate)
    }

    /**
     * Select a tab on the BottomBar by the specified tab [id].
     *
     * @param id The id of the tab to be selected.
     */
    fun selectTabById(@IdRes id: Int, animate: Boolean = true) {
        val tab =
            findTabWithId(id) ?: throw IllegalArgumentException("Tab with id $id does not exist.")
        selectTab(tab, animate)
    }

    /**
     * Select a tab on the BottomBar by [Tab] instance, use [tabs] to retrieve a list of tabs.
     *
     * @param tab The [Tab] instance to be selected.
     */
    fun selectTab(tab: Tab, animate: Boolean = true) {
        adapter.selectTab(tab, animate)
    }

    /**
     * Enable/disabled a tab on the BottomBar by the specified [tabIndex] index.
     *
     * @param tabIndex The index of the tab to be enabled or disabled.
     * @param enabled Whether the tab state should be enabled or disabled.
     */
    fun setTabEnabledAt(tabIndex: Int, enabled: Boolean) {
        if (tabIndex < 0 || tabIndex >= adapter.tabs.size) {
            throw IndexOutOfBoundsException("Tab index $tabIndex is out of bounds.")
        }

        val tab = adapter.tabs[tabIndex]
        setTabEnabled(tab, enabled)
    }

    /**
     * Enable/disable a tab on the BottomBar by the specified tab [id].
     *
     * @param id The id of the tab to be enabled or disabled.
     * @param enabled Whether the tab state should be enabled or disabled.
     */
    fun setTabEnabledById(@IdRes id: Int, enabled: Boolean) {
        val tab =
            findTabWithId(id) ?: throw IllegalArgumentException("Tab with id $id does not exist.")
        setTabEnabled(tab, enabled)
    }

    /**
     * Enable/disable a tab on the BottomBar by [Tab] instance, use [tabs] to retrieve a list of tabs.
     *
     * @param tab The [Tab] instance to be enabled or disabled.
     * @param enabled Whether the tab state should be enabled or disabled.
     */
    fun setTabEnabled(tab: Tab, enabled: Boolean) {
        tab.enabled = enabled
        adapter.notifyTabChanged(tab)
    }

    /**
     * Add a badge to a tab on the BottomBar by the specified [tabIndex] index.
     *
     * @param tabIndex The index of the tab which the given [Badge] should be added to.
     * @param badge The badge you want to add to the tab.
     */
    fun setBadgeAtTabIndex(tabIndex: Int, badge: Badge? = null) {
        if (tabIndex < 0 || tabIndex >= adapter.tabs.size) {
            throw IndexOutOfBoundsException("Tab index $tabIndex is out of bounds.")
        }

        val tab = adapter.tabs[tabIndex]
        setBadgeAtTab(tab, badge)
    }

    /**
     * Add a badge to a tab on the BottomBar by the specified tab [id].
     *
     * @param id The id of the tab which the given [Badge] should be added to.
     * @param badge The badge you want to add to the tab.
     */
    fun setBadgeAtTabId(@IdRes id: Int, badge: Badge? = null) {
        val tab =
            findTabWithId(id) ?: throw IllegalArgumentException("Tab with id $id does not exist.")
        setBadgeAtTab(tab, badge)
    }

    /**
     * Add a badge to a BottomBar tab by [Tab] instance, use [tabs] to retrieve a list of tabs.
     *
     * @param tab The [Tab] instance which the given [Badge] should be added to.
     * @param badge The badge you want to add to the tab.
     */
    fun setBadgeAtTab(tab: Tab, badge: Badge? = null) {
        tab.badge = badge
        adapter.applyTabBadge(tab, badge ?: Badge())
    }

    /**
     * Remove a badge from a tab by the specified [tabIndex] index.
     *
     * @param tabIndex The index of the tab which the badge should be removed of.
     */
    fun clearBadgeAtTabIndex(tabIndex: Int) {
        if (tabIndex < 0 || tabIndex >= adapter.tabs.size) {
            throw IndexOutOfBoundsException("Tab index $tabIndex is out of bounds.")
        }

        val tab = adapter.tabs[tabIndex]
        clearBadgeAtTab(tab)
    }

    /**
     * Remove a badge from a tab by the specified tab [id].
     *
     * @param id The id of the tab which the badge should be removed of.
     */
    fun clearBadgeAtTabId(@IdRes id: Int) {
        val tab =
            findTabWithId(id) ?: throw IllegalArgumentException("Tab with id $id does not exist.")
        clearBadgeAtTab(tab)
    }

    /**
     * Remove a badge from a tab.
     *
     * @param tab The [Tab] instance which the badge should be removed of.
     */
    fun clearBadgeAtTab(tab: Tab) {
        tab.badge = null
        adapter.applyTabBadge(tab, null)
    }

    /**
     * This method will link the given ViewPager and this AnimatedBottomBar together so that changes in one are automatically reflected in the other. This includes scroll state changes and clicks.
     *
     * @param viewPager The ViewPager to link to, or null to clear any previous link
     */
    fun setupWithViewPager(viewPager: ViewPager?) {
        this.viewPager = viewPager

        if (viewPager != null) {
            selectTabAt(viewPager.currentItem, false)
            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                private var previousState: Int = ViewPager.SCROLL_STATE_IDLE
                private var userScrollChange = false

                override fun onPageScrollStateChanged(state: Int) {
                    // Use Scroll state to detect whether the user is sliding
                    if (previousState == ViewPager.SCROLL_STATE_DRAGGING
                        && state == ViewPager.SCROLL_STATE_SETTLING
                    ) {
                        userScrollChange = true
                    } else if (previousState == ViewPager.SCROLL_STATE_SETTLING
                        && state == ViewPager.SCROLL_STATE_IDLE
                    ) {
                        userScrollChange = false
                    }
                    previousState = state
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    if (userScrollChange) {
                        // Swap by user's touch, change adapter to new tab.
                        selectTabAt(position)
                    }
                }
            })
        }
    }

    /**
     * This method will link the given ViewPager2 and this AnimatedBottomBar together so that changes in one are automatically reflected in the other. This includes scroll state changes and clicks.
     *
     * @param viewPager2 The ViewPager2 to link to, or null to clear any previous link
     */
    fun setupWithViewPager2(viewPager2: ViewPager2?) {
        this.viewPager2 = viewPager2

        if (viewPager2 != null) {
            selectTabAt(viewPager2.currentItem, false)
            viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                private var previousState: Int = ViewPager2.SCROLL_STATE_IDLE
                private var userScrollChange = false

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    // Use Scroll state to detect whether the user is sliding
                    if (previousState == ViewPager2.SCROLL_STATE_DRAGGING
                        && state == ViewPager2.SCROLL_STATE_SETTLING
                    ) {
                        userScrollChange = true
                    } else if (previousState == ViewPager2.SCROLL_STATE_SETTLING
                        && state == ViewPager2.SCROLL_STATE_IDLE
                    ) {
                        userScrollChange = false
                    }
                    previousState = state
                }

                override fun onPageSelected(position: Int) {
                    if (userScrollChange) {
                        // Swap by user's touch, change adapter to new tab.
                        selectTabAt(position)
                    }
                }
            })
        }
    }

    /**
     * This method will link the given NavController to the AnimatedBottomBar together so that changes in one are automatically reflected in the other.
     *
     * @param menu The menu used in combination with the NavController and AnimatedBottomBar.
     * @param navController NavController The NavController the AnimatedBottomBar should be linked to.
     */
    fun setupWithNavController(menu: Menu, navController: NavController) {
        NavigationComponentHelper.setupWithNavController(this, menu, navController)
    }

    private fun findTabWithId(@IdRes id: Int): Tab? {
        for (tab in tabs) {
            if (tab.id == id) {
                return tab
            }
        }

        return null
    }

    /**
     * Retrieve a list of all tabs.
     */
    val tabs
        get() = ArrayList(adapter.tabs)

    /**
     * Get the number of tabs in the BottomBar.
     */
    val tabCount
        get() = adapter.tabs.size

    /**
     * Get the currently selected [Tab] instance.
     *
     * @return Currently selected tab, null when no tab is selected.
     */
    val selectedTab
        get() = adapter.selectedTab

    /**
     * Get the currently selected tab index.
     *
     * @return Currently selected tab index, -1 when no tab is selected.
     */
    val selectedIndex
        get() = adapter.selectedIndex


    var selectedTabType
        get() = tabStyle.selectedTabType
        set(value) {
            tabStyle.selectedTabType = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.TAB_TYPE)
        }


    var tabAnimationSelected
        get() = tabStyle.tabAnimationSelected
        set(value) {
            tabStyle.tabAnimationSelected = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.ANIMATIONS)
        }

    var tabAnimation
        get() = tabStyle.tabAnimation
        set(value) {
            tabStyle.tabAnimation = value
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

    var animationInterpolatorRes: Int
        @Deprecated("", level = DeprecationLevel.HIDDEN)
        get() = 0
        set(@AnimRes value) {
            animationInterpolator = AnimationUtils.loadInterpolator(context, value)
        }


    var rippleEnabled
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        get() = tabStyle.rippleEnabled
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        set(value) {
            tabStyle.rippleEnabled = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.RIPPLE)
        }

    var rippleColor
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        @ColorInt
        get() = tabStyle.rippleColor
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        set(@ColorInt value) {
            tabStyle.rippleColor = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.RIPPLE)
        }

    var rippleColorRes
        @Deprecated("", level = DeprecationLevel.HIDDEN)
        get() = Int.MIN_VALUE
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        set(@ColorRes value) {
            rippleColor = ContextCompat.getColor(context, value)
        }


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
            tabColorSelected = ContextCompat.getColor(context, value)
        }

    var tabColorDisabled
        @ColorInt
        get() = tabStyle.tabColorDisabled
        set(@ColorInt value) {
            tabStyle.tabColorDisabled = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.COLORS)
        }

    var tabColorDisabledRes
        @Deprecated("", level = DeprecationLevel.HIDDEN)
        get() = Int.MIN_VALUE
        set(@ColorRes value) {
            tabColorDisabled = ContextCompat.getColor(context, value)
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
            tabColor = ContextCompat.getColor(context, value)
        }

    // Text
    var textAppearance
        @StyleRes
        get() = tabStyle.textAppearance
        set(@StyleRes value) {
            tabStyle.textAppearance = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.TEXT)
        }
    var typeface
        get() = tabStyle.typeface
        set(value) {
            tabStyle.typeface = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.TEXT)
        }
    var textSize
        @Dimension
        get() = tabStyle.textSize
        set(@Dimension value) {
            tabStyle.textSize = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.TEXT)
        }

    // Icon
    var iconSize
        @Dimension
        get() = tabStyle.iconSize
        set(@Dimension value) {
            tabStyle.iconSize = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.ICON)
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
            indicatorColor = ContextCompat.getColor(context, value)
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

    // Badge
    var badgeAnimation
        get() = tabStyle.badge.animation
        set(value) {
            tabStyle.badge.animation = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.BADGE)
        }

    var badgeAnimationDuration
        get() = tabStyle.badge.animationDuration
        set(value) {
            tabStyle.badge.animationDuration = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.BADGE)
        }

    var badgeBackgroundColor
        @ColorInt
        get() = tabStyle.badge.backgroundColor
        set(@ColorInt value) {
            tabStyle.badge.backgroundColor = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.BADGE)
        }

    var badgeBackgroundColorRes
        @Deprecated("", level = DeprecationLevel.HIDDEN)
        get() = Int.MIN_VALUE
        set(@ColorRes value) {
            badgeBackgroundColor = ContextCompat.getColor(context, value)
        }

    var badgeTextColor
        @ColorInt
        get() = tabStyle.badge.textColor
        set(@ColorInt value) {
            tabStyle.badge.textColor = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.BADGE)
        }

    var badgeTextColorRes
        @Deprecated("", level = DeprecationLevel.HIDDEN)
        get() = Int.MIN_VALUE
        set(@ColorRes value) {
            badgeTextColor = ContextCompat.getColor(context, value)
        }

    var badgeTextSize
        get() = tabStyle.badge.textSize
        set(@Dimension value) {
            tabStyle.badge.textSize = value
            applyTabStyle(BottomBarStyle.StyleUpdateType.BADGE)
        }

    class Tab internal constructor(
        val icon: Drawable,
        val title: String,
        @IdRes val id: Int = -1,
        var badge: Badge? = null,
        var enabled: Boolean = true
    )

    class Badge(
        val text: String? = null,
        @ColorInt val backgroundColor: Int? = null,
        @ColorInt val textColor: Int? = null,
        @Dimension val textSize: Int? = null
    )

    enum class TabType(val id: Int) {
        TEXT(0),
        ICON(1);

        companion object {
            fun fromId(id: Int): TabType? {
                for (f in values()) {
                    if (f.id == id) return f
                }
                throw IllegalArgumentException()
            }
        }
    }

    enum class TabAnimation(val id: Int) {
        NONE(0),
        SLIDE(1),
        FADE(2);

        companion object {
            fun fromId(id: Int): TabAnimation? {
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
        INVISIBLE(0),
        SQUARE(1),
        ROUND(2);

        companion object {
            fun fromId(id: Int): IndicatorAppearance? {
                for (f in values()) {
                    if (f.id == id) return f
                }
                throw IllegalArgumentException()
            }
        }
    }

    enum class IndicatorAnimation(val id: Int) {
        NONE(0),
        SLIDE(1),
        FADE(2);

        companion object {
            fun fromId(id: Int): IndicatorAnimation? {
                for (f in values()) {
                    if (f.id == id) return f
                }
                throw IllegalArgumentException()
            }
        }
    }

    enum class BadgeAnimation(val id: Int) {
        NONE(0),
        SCALE(1),
        FADE(2);

        companion object {
            fun fromId(id: Int): BadgeAnimation? {
                for (f in values()) {
                    if (f.id == id) return f
                }
                throw IllegalArgumentException()
            }
        }
    }

    interface OnTabSelectListener {
        fun onTabSelected(lastIndex: Int, lastTab: Tab?, newIndex: Int, newTab: Tab)

        fun onTabReselected(index: Int, tab: Tab) {
        }
    }

    interface OnTabInterceptListener {
        fun onTabIntercepted(lastIndex: Int, lastTab: Tab?, newIndex: Int, newTab: Tab): Boolean
    }
}