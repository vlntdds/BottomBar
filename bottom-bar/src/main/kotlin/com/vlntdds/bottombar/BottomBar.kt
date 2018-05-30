package com.vlntdds.bottombar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.ColorInt
import android.support.annotation.IdRes
import android.support.annotation.RequiresApi
import android.support.annotation.VisibleForTesting
import android.support.annotation.XmlRes
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.LinearLayout
import android.widget.Toast
import com.vlntdds.bottombar.badge.BadgeContainer
import com.vlntdds.bottombar.behavior.BottomNavigationBehavior
import com.vlntdds.bottombar.tab.BottomBarTab
import com.vlntdds.bottombar.tab.listeners.OnTabReselectListener
import com.vlntdds.bottombar.tab.listeners.OnTabSelectListener
import com.vlntdds.bottombar.tab.listeners.TabSelectionInterceptor
import com.vlntdds.bottombar.util.BatchTabPropertyApplier
import com.vlntdds.bottombar.util.MiscUtils
import com.vlntdds.bottombar.util.NavbarUtils

import java.util.ArrayList

/*
 * BottomBar library for Android
 * Copyright (c) 2016 Iiro Krankka (http://github.com/roughike).
 * Copyright (c) 2018 Eduardo Calazans Júnior (http://github.com/vlntdds).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class BottomBar : LinearLayout, View.OnClickListener, View.OnLongClickListener {

    private var batchPropertyApplier: BatchTabPropertyApplier? = null
    private var primaryColor: Int = 0
    private var screenWidth: Int = 0
    private var tenDp: Int = 0
    private var maxFixedItemWidth: Int = 0

    // XML Attributes
    private var tabXmlResource: Int = 0
    private var isTabletMode: Boolean = false
    private var behaviors: Int = 0
    private var inActiveTabAlpha: Float = 0.toFloat()
    private var activeTabAlpha: Float = 0.toFloat()
    private var inActiveTabColor: Int = 0
    private var activeTabColor: Int = 0
    private var badgeBackgroundColor: Int = 0
    private var lastTabSelected = 0
    private var hideBadgeWhenActive: Boolean = false
    private var longPressHintsEnabled: Boolean = false
    private var titleTextAppearance: Int = 0
    private var titleTypeFace: Typeface? = null
    private var showShadow: Boolean = false
    private var shadowElevation: Float = 0.toFloat()
    private var shadowView: View? = null

    private var backgroundOverlay: View? = null
    private var outerContainer: ViewGroup? = null
    private var tabContainer: ViewGroup? = null

    private var defaultBackgroundColor = Color.WHITE
    private var currentBackgroundColor: Int = 0

    /**
     * Get the currently selected tab position.
     */
    var currentTabPosition: Int = 0
        private set

    private var inActiveShiftingItemWidth: Int = 0
    private var activeShiftingItemWidth: Int = 0
    private var tabSelectionInterceptor: TabSelectionInterceptor? = null
    private var onTabSelectListener: OnTabSelectListener? = null
    private var onTabReselectListener: OnTabReselectListener? = null
    private var isComingFromRestoredState: Boolean = false
    private var ignoreTabReselectionListener: Boolean = false
    private var shySettings: ShySettings? = null
    internal var isShyHeightAlreadyCalculated: Boolean = false
    private var navBarAccountedHeightCalculated: Boolean = false
    private var currentTabs: Array<BottomBarTab?>? = null
    private val isShiftingMode: Boolean
        get() = !isTabletMode && hasBehavior(BEHAVIOR_SHIFTING)
    internal val isShy: Boolean
        get() = !isTabletMode && hasBehavior(BEHAVIOR_SHY)
    private val isIconsOnlyMode: Boolean
        get() = !isTabletMode && hasBehavior(BEHAVIOR_ICONS_ONLY)
    private val tabConfig: BottomBarTab.Config
        get() = BottomBarTab.Config.Builder()
                .inActiveTabAlpha(inActiveTabAlpha)
                .activeTabAlpha(activeTabAlpha)
                .inActiveTabColor(inActiveTabColor)
                .activeTabColor(activeTabColor)
                .barColorWhenSelected(defaultBackgroundColor)
                .badgeBackgroundColor(badgeBackgroundColor)
                .hideBadgeWhenSelected(hideBadgeWhenActive)
                .titleTextAppearance(titleTextAppearance)
                .titleTypeFace(titleTypeFace)
                .build()

    val tabCount: Int
        get() = tabContainer!!.childCount

    /**
     * Get the currently selected tab.
     */
    val currentTab: BottomBarTab?
        get() = getTabAtPosition(currentTabPosition)

    /**
     * Get the resource id for the currently selected tab.
     */
    val currentTabId: Int
        @IdRes
        get() = currentTab!!.id

    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, 0)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        batchPropertyApplier = BatchTabPropertyApplier(this)

        populateAttributes(context, attrs, defStyleAttr, defStyleRes)
        initializeViews()
        determineInitialBackgroundColor()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            init21(context)
        }

        if (tabXmlResource != 0) {
            setItems(tabXmlResource)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // This is so that in Pre-Lollipop devices there is a shadow BUT without pushing the content
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && showShadow && shadowView != null) {
            shadowView!!.visibility = View.VISIBLE
            val params = layoutParams
            if (params is ViewGroup.MarginLayoutParams) {
                val shadowHeight = resources.getDimensionPixelSize(R.dimen.bb_fake_shadow_height)

                params.setMargins(params.leftMargin,
                        params.topMargin - shadowHeight,
                        params.rightMargin,
                        params.bottomMargin)
                layoutParams = params
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init21(context: Context) {
        if (showShadow) {
            shadowElevation = elevation
            shadowElevation = if (shadowElevation > 0)
                shadowElevation
            else
                resources.getDimensionPixelSize(R.dimen.bb_default_elevation).toFloat()
            elevation = MiscUtils.dpToPixel(context, shadowElevation).toFloat()
            outlineProvider = ViewOutlineProvider.BOUNDS
        }
    }

    private fun populateAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        primaryColor = MiscUtils.getColor(getContext(), R.attr.colorPrimary)
        screenWidth = MiscUtils.getScreenWidth(getContext())
        tenDp = MiscUtils.dpToPixel(getContext(), 10f)
        maxFixedItemWidth = MiscUtils.dpToPixel(getContext(), 168f)

        val ta = context.theme
                .obtainStyledAttributes(attrs, R.styleable.BottomBar, defStyleAttr, defStyleRes)

        try {
            tabXmlResource = ta.getResourceId(R.styleable.BottomBar_bb_tabXmlResource, 0)
            isTabletMode = ta.getBoolean(R.styleable.BottomBar_bb_tabletMode, false)
            behaviors = ta.getInteger(R.styleable.BottomBar_bb_behavior, BEHAVIOR_NONE)
            inActiveTabAlpha = ta.getFloat(R.styleable.BottomBar_bb_inActiveTabAlpha,
                    if (isShiftingMode) DEFAULT_INACTIVE_SHIFTING_TAB_ALPHA else 1.0f)
            activeTabAlpha = ta.getFloat(R.styleable.BottomBar_bb_activeTabAlpha, 1f)

            @ColorInt
            val defaultInActiveColor = if (isShiftingMode)
                Color.WHITE
            else
                ContextCompat.getColor(context, R.color.bb_inActiveBottomBarItemColor)
            val defaultActiveColor = if (isShiftingMode) Color.WHITE else primaryColor

            longPressHintsEnabled = ta.getBoolean(R.styleable.BottomBar_bb_longPressHintsEnabled, true)
            inActiveTabColor = ta.getColor(R.styleable.BottomBar_bb_inActiveTabColor, defaultInActiveColor)
            activeTabColor = ta.getColor(R.styleable.BottomBar_bb_activeTabColor, defaultActiveColor)
            badgeBackgroundColor = ta.getColor(R.styleable.BottomBar_bb_badgeBackgroundColor, Color.RED)
            hideBadgeWhenActive = ta.getBoolean(R.styleable.BottomBar_bb_badgesHideWhenActive, true)
            titleTextAppearance = ta.getResourceId(R.styleable.BottomBar_bb_titleTextAppearance, 0)
            titleTypeFace = getTypeFaceFromAsset(ta.getString(R.styleable.BottomBar_bb_titleTypeFace))
            showShadow = ta.getBoolean(R.styleable.BottomBar_bb_showShadow, true)
        } finally {
            ta.recycle()
        }
    }

    private fun drawUnderNav(): Boolean {
        return (!isTabletMode
                && hasBehavior(BEHAVIOR_DRAW_UNDER_NAV)
                && NavbarUtils.shouldDrawBehindNavbar(context))
    }

    private fun hasBehavior(behavior: Int): Boolean {
        return behaviors or behavior == behaviors
    }

    private fun getTypeFaceFromAsset(fontPath: String?): Typeface? {
        return if (fontPath != null) {
            Typeface.createFromAsset(
                    context.assets, fontPath)
        } else null

    }

    private fun initializeViews() {
        val width = if (isTabletMode) LinearLayout.LayoutParams.WRAP_CONTENT else LinearLayout.LayoutParams.MATCH_PARENT
        val height = if (isTabletMode) LinearLayout.LayoutParams.MATCH_PARENT else LinearLayout.LayoutParams.WRAP_CONTENT
        val params = LinearLayout.LayoutParams(width, height)

        layoutParams = params
        orientation = if (isTabletMode) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL

        val rootView = View.inflate(context,
                if (isTabletMode) R.layout.bb_bottom_bar_item_container_tablet else R.layout.bb_bottom_bar_item_container, this)
        rootView.layoutParams = params

        backgroundOverlay = rootView.findViewById(R.id.bb_bottom_bar_background_overlay)
        outerContainer = rootView.findViewById<View>(R.id.bb_bottom_bar_outer_container) as ViewGroup
        tabContainer = rootView.findViewById<View>(R.id.bb_bottom_bar_item_container) as ViewGroup
        shadowView = findViewById(R.id.bb_bottom_bar_shadow)
    }

    private fun determineInitialBackgroundColor() {
        if (isShiftingMode) {
            defaultBackgroundColor = primaryColor
        }

        val userDefinedBackground = background

        val userHasDefinedBackgroundColor = userDefinedBackground != null && userDefinedBackground is ColorDrawable

        if (userHasDefinedBackgroundColor) {
            defaultBackgroundColor = (userDefinedBackground as ColorDrawable).color
            setBackgroundColor(Color.TRANSPARENT)
        }
    }

    /**
     * Set the item for the BottomBar from XML Resource with a default configuration
     * for each tab.
     */
    @JvmOverloads
    fun setItems(@XmlRes xmlRes: Int, defaultTabConfig: BottomBarTab.Config? = null) {
        if (xmlRes == 0) {
            throw RuntimeException("No items specified for the BottomBar!")
        }

        val tabConfigParam = defaultTabConfig ?: tabConfig
        val parser = TabParser(context, tabConfigParam, xmlRes)
        updateItems(parser.parseTabs())
    }

    private fun updateItems(bottomBarItems: List<BottomBarTab>) {
        tabContainer!!.removeAllViews()

        var biggestWidth = 0

        val viewsToAdd = arrayOfNulls<BottomBarTab>(bottomBarItems.size)

        for ((index, bottomBarTab) in bottomBarItems.withIndex()) {
            val type: BottomBarTab.Type = when {
                isShiftingMode -> BottomBarTab.Type.SHIFTING
                isTabletMode -> BottomBarTab.Type.TABLET
                else -> BottomBarTab.Type.FIXED
            }

            if (isIconsOnlyMode) {
                bottomBarTab.isTitleless = true
            }

            bottomBarTab.type = type
            bottomBarTab.prepareLayout()

            if (index == currentTabPosition) {
                bottomBarTab.select(false)

                handleBackgroundColorChange(bottomBarTab, false)
            } else {
                bottomBarTab.deselect(false)
            }

            if (!isTabletMode) {
                if (bottomBarTab.width > biggestWidth) {
                    biggestWidth = bottomBarTab.width
                }

                viewsToAdd[index] = bottomBarTab
            } else {
                tabContainer!!.addView(bottomBarTab)
            }

            bottomBarTab.setOnClickListener(this)
            bottomBarTab.setOnLongClickListener(this)
        }

        currentTabs = viewsToAdd

        if (!isTabletMode) {
            addViewsToBottomBar(viewsToAdd)
        }
    }

    private fun addViewsToBottomBar(tabsToAdd: Array<BottomBarTab?>) {
        for (tabView in tabsToAdd) {
            if (tabView?.parent == null) {
                tabContainer!!.addView(tabView)
            }
        }
    }

    /**
     * Resize the bottombar after changing visibility of some tab.
     */
    fun resizeTabsToCorrectSizes() {
        val visibleViewPos = ArrayList<Int>()
        var viewWidth = MiscUtils.pixelToDp(context, width)

        if (viewWidth <= 0 || viewWidth > screenWidth) {
            viewWidth = screenWidth
        }

        val containerView = findViewById<View>(R.id.bb_bottom_bar_item_container) as LinearLayout
        for (i in 0..containerView.childCount) {
            if (containerView.getChildAt(i) is BottomBarTab && containerView.getChildAt(i).visibility == View.VISIBLE) {
                visibleViewPos.add(i)
            }
        }

        if (visibleViewPos.size == 0) {
            return
        }

        val proposedItemWidth = Math.min(
                MiscUtils.dpToPixel(context, (viewWidth / visibleViewPos.size).toFloat()),
                maxFixedItemWidth
        )

        inActiveShiftingItemWidth = (proposedItemWidth * 0.9).toInt()
        activeShiftingItemWidth = (proposedItemWidth + proposedItemWidth * ((visibleViewPos.size - 1) * 0.1)).toInt()
        val height = Math.round(context.resources
                .getDimension(R.dimen.bb_height))

        for (i in visibleViewPos) {
            val view = containerView.getChildAt(i) as BottomBarTab
            val params = view.layoutParams
            params.height = height

            if (isShiftingMode) {
                if (view.isActive) {
                    params.width = activeShiftingItemWidth
                } else {
                    params.width = inActiveShiftingItemWidth
                }
            } else {
                params.width = proposedItemWidth
            }

            if (view.parent == null) {
                tabContainer!!.addView(view)
            }
            view.layoutParams = params
        }
    }

    /**
     * Returns the settings specific for a shy BottomBar.
     *
     * @throws UnsupportedOperationException, if this BottomBar is not shy.
     */
    fun getShySettings(): ShySettings {
        if (!isShy) {
            Log.e("BottomBar", "Tried to get shy settings for a BottomBar " + "that is not shy.")
        }

        if (shySettings == null) {
            shySettings = ShySettings(this)
        }

        return shySettings as ShySettings
    }

    /**
     * Set a listener that gets fired when the selected [BottomBarTab] is about to change.
     *
     * @param interceptor a listener for potentially interrupting changes in tab selection.
     */
    fun setTabSelectionInterceptor(interceptor: TabSelectionInterceptor) {
        tabSelectionInterceptor = interceptor
    }

    /**
     * Removes the current [TabSelectionInterceptor] listener
     */
    fun removeOverrideTabSelectionListener() {
        tabSelectionInterceptor = null
    }

    /**
     * Set a listener that gets fired when the selected [BottomBarTab] changes.
     *
     *
     * If `shouldFireInitially` is set to false, this listener isn't fired straight away
     * it's set, but you'll get all events normally for consecutive tab selection changes.
     *
     * @param listener            a listener for monitoring changes in tab selection.
     * @param shouldFireInitially whether the listener should be fired the first time it's set.
     */
    @JvmOverloads
    fun setOnTabSelectListener(listener: OnTabSelectListener, shouldFireInitially: Boolean = true) {
        onTabSelectListener = listener

        if (shouldFireInitially && tabCount > 0) {
            listener.onTabSelected(currentTabId)
        }
    }

    /**
     * Removes the current [OnTabSelectListener] listener
     */
    fun removeOnTabSelectListener() {
        onTabSelectListener = null
    }

    /**
     * Set a listener that gets fired when a currently selected [BottomBarTab] is clicked.
     *
     * @param listener a listener for handling tab reselections.
     */
    fun setOnTabReselectListener(listener: OnTabReselectListener) {
        onTabReselectListener = listener
    }

    /**
     * Removes the current [OnTabReselectListener] listener
     */
    fun removeOnTabReselectListener() {
        onTabReselectListener = null
    }

    /**
     * Set the default selected to be the tab with the corresponding tab id.
     * By default, the first tab in the container is the default tab.
     */
    fun setDefaultTab(@IdRes defaultTabId: Int) {
        val defaultTabPosition = findPositionForTabWithId(defaultTabId)
        setDefaultTabPosition(defaultTabPosition)
    }

    /**
     * Sets the default tab for this BottomBar that is shown until the user changes
     * the selection.
     *
     * @param defaultTabPosition the default tab position.
     */
    fun setDefaultTabPosition(defaultTabPosition: Int) {
        if (isComingFromRestoredState) return

        selectTabAtPosition(defaultTabPosition)
    }

    /**
     * Reselect the previously selected tab.
     */
    fun goToLastSelectedTab() {
        if (lastTabSelected != 0) {
            selectTabWithId(lastTabSelected)
        }
    }

    /**
     * Select the tab with the corresponding id.
     */
    fun selectTabWithId(@IdRes tabResId: Int) {
        val tabPosition = findPositionForTabWithId(tabResId)
        selectTabAtPosition(tabPosition)
    }

    /**
     * Select a tab at the specified position.
     *
     * @param position the position to select.
     * @param animate  should the tab change be animated or not.
     */
    @JvmOverloads
    fun selectTabAtPosition(position: Int, animate: Boolean = false) {
        if (position > tabCount - 1 || position < 0) {
            throw IndexOutOfBoundsException("Can't select tab at position " +
                    position + ". This BottomBar has no items at that position.")
        }

        val oldTab = currentTab
        val newTab = getTabAtPosition(position)

        oldTab!!.deselect(animate)
        newTab!!.select(animate)

        updateSelectedTab(position)
        shiftingMagic(oldTab, newTab, animate)
        handleBackgroundColorChange(newTab, animate)
    }

    /**
     * Get the tab at the specified position.
     */
    fun getTabAtPosition(position: Int): BottomBarTab? {
        val child = tabContainer!!.getChildAt(position)

        return if (child is BadgeContainer) {
            findTabInLayout(child)
        } else child as BottomBarTab

    }

    /**
     * Find the tabs' position in the container by id.
     */
    fun findPositionForTabWithId(@IdRes tabId: Int): Int {
        return getTabWithId(tabId).indexInTabContainer
    }

    /**
     * Find a BottomBarTab with the corresponding id.
     */
    fun getTabWithId(@IdRes tabId: Int): BottomBarTab {
        return tabContainer!!.findViewById<View>(tabId) as BottomBarTab
    }

    /**
     * Controls whether the long pressed tab title should be displayed in
     * a helpful Toast if the title is not currently visible.
     *
     * @param enabled true if toasts should be shown to indicate the title
     * of a long pressed tab, false otherwise.
     */
    fun setLongPressHintsEnabled(enabled: Boolean) {
        longPressHintsEnabled = enabled
    }

    /**
     * Set alpha value used for inactive BottomBarTabs.
     */
    fun setInActiveTabAlpha(alpha: Float) {
        inActiveTabAlpha = alpha
        batchPropertyApplier!!.applyToAllTabs { tab -> tab.inActiveAlpha = inActiveTabAlpha }
    }

    /**
     * Set alpha value used for active BottomBarTabs.
     */
    fun setActiveTabAlpha(alpha: Float) {
        activeTabAlpha = alpha
        batchPropertyApplier!!.applyToAllTabs { tab -> tab.activeAlpha = activeTabAlpha }
    }

    fun setInActiveTabColor(@ColorInt color: Int) {
        inActiveTabColor = color
        batchPropertyApplier!!.applyToAllTabs { tab -> tab.inActiveColor = inActiveTabColor }
    }

    /**
     * Set active color used for selected BottomBarTabs.
     */
    fun setActiveTabColor(@ColorInt color: Int) {
        activeTabColor = color
        batchPropertyApplier!!.applyToAllTabs { tab -> tab.activeColor = activeTabColor }
    }

    /**
     * Set background color for the badge.
     */
    fun setBadgeBackgroundColor(@ColorInt color: Int) {
        badgeBackgroundColor = color
        batchPropertyApplier!!.applyToAllTabs { tab -> tab.badgeBackgroundColor = badgeBackgroundColor }
    }

    /**
     * Controls whether the badge (if any) for active tabs
     * should be hidden or not.
     */
    fun setBadgesHideWhenActive(hideWhenSelected: Boolean) {
        hideBadgeWhenActive = hideWhenSelected
        batchPropertyApplier!!.applyToAllTabs { tab -> tab.badgeHidesWhenActive = hideBadgeWhenActive }
    }

    /**
     * Set custom text apperance for all BottomBarTabs.
     */
    fun setTabTitleTextAppearance(textAppearance: Int) {
        titleTextAppearance = textAppearance
        batchPropertyApplier!!.applyToAllTabs { tab -> tab.titleTextAppearance = titleTextAppearance }
    }

    /**
     * Set a custom typeface for all tab's titles.
     *
     * @param fontPath path for your custom font file, such as fonts/MySuperDuperFont.ttf.
     * In that case your font path would look like src/main/assets/fonts/MySuperDuperFont.ttf,
     * but you only need to provide fonts/MySuperDuperFont.ttf, as the asset folder
     * will be auto-filled for you.
     */
    fun setTabTitleTypeface(fontPath: String) {
        val actualTypeface = getTypeFaceFromAsset(fontPath)
        setTabTitleTypeface(actualTypeface)
    }

    /**
     * Set a custom typeface for all tab's titles.
     */
    fun setTabTitleTypeface(typeface: Typeface?) {
        titleTypeFace = typeface

        batchPropertyApplier!!.applyToAllTabs { tab -> tab.setTitleTypeface(titleTypeFace) }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (changed) {
            if (!isTabletMode) {
                resizeTabsToCorrectSizes()
            }

            updateTitleBottomPadding()

            if (isShy) {
                initializeShyBehavior()
            }

            if (drawUnderNav()) {
                resizeForDrawingUnderNavbar()
            }
        }
    }

    private fun updateTitleBottomPadding() {
        if (isIconsOnlyMode) {
            return
        }

        val tabCount = tabCount

        if (tabContainer == null || tabCount == 0 || !isShiftingMode) {
            return
        }

        for (i in 0 until tabCount) {
            val tab = getTabAtPosition(i)
            val title = tab!!.titleView ?: continue

            val baseline = title.baseline
            val height = title.height
            val paddingInsideTitle = height - baseline
            val missingPadding = tenDp - paddingInsideTitle

            if (missingPadding > 0) {
                title.setPadding(title.paddingLeft, title.paddingTop,
                        title.paddingRight, missingPadding + title.paddingBottom)
            }
        }
    }

    private fun initializeShyBehavior() {
        val parent = parent

        val hasAbusiveParent = parent != null && parent is CoordinatorLayout

        if (!hasAbusiveParent) {
            throw RuntimeException("In order to have shy behavior, the " + "BottomBar must be a direct child of a CoordinatorLayout.")
        }

        if (!isShyHeightAlreadyCalculated) {
            val height = height

            if (height != 0) {
                updateShyHeight(height)
                getShySettings().shyHeightCalculated()
                isShyHeightAlreadyCalculated = true
            }
        }
    }

    private fun updateShyHeight(height: Int) {
        (layoutParams as CoordinatorLayout.LayoutParams).behavior = BottomNavigationBehavior<View>(height, 0, false)
    }

    private fun resizeForDrawingUnderNavbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val currentHeight = height

            if (currentHeight != 0 && !navBarAccountedHeightCalculated) {
                navBarAccountedHeightCalculated = true
                tabContainer!!.layoutParams.height = currentHeight

                val navbarHeight = NavbarUtils.getNavbarHeight(context)
                val finalHeight = currentHeight + navbarHeight
                layoutParams.height = finalHeight

                if (isShy) {
                    updateShyHeight(finalHeight)
                }
            }
        }
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val bundle = saveState()
        bundle.putParcelable("superstate", super.onSaveInstanceState())
        return bundle
    }

    @VisibleForTesting
    private fun saveState(): Bundle {
        val outState = Bundle()
        outState.putInt(STATE_CURRENT_SELECTED_TAB, currentTabPosition)

        return outState
    }

    public override fun onRestoreInstanceState(state: Parcelable?) {
        var savedState = state
        if (savedState is Bundle) {
            val bundle = savedState as Bundle?
            restoreState(bundle)

            savedState = bundle!!.getParcelable("superstate")
        }
        super.onRestoreInstanceState(savedState)
    }

    @VisibleForTesting
    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            isComingFromRestoredState = true
            ignoreTabReselectionListener = true

            val restoredPosition = savedInstanceState.getInt(STATE_CURRENT_SELECTED_TAB, currentTabPosition)
            selectTabAtPosition(restoredPosition, false)
        }
    }

    override fun onClick(target: View) {
        if (target !is BottomBarTab) return
        handleClick(target)
    }

    override fun onLongClick(target: View): Boolean {
        return target !is BottomBarTab || handleLongClick(target)
    }

    private fun findTabInLayout(child: ViewGroup): BottomBarTab? {
        for (i in 0 until child.childCount) {
            val candidate = child.getChildAt(i)

            if (candidate is BottomBarTab) {
                return candidate
            }
        }

        return null
    }

    private fun handleClick(newTab: BottomBarTab) {
        val oldTab = currentTab

        if (tabSelectionInterceptor != null && tabSelectionInterceptor!!.shouldInterceptTabSelection(oldTab!!.id, newTab.id)) {
            return
        }

        oldTab!!.deselect(true)
        newTab.select(true)

        shiftingMagic(oldTab, newTab, true)
        handleBackgroundColorChange(newTab, true)
        updateSelectedTab(newTab.indexInTabContainer)
    }

    private fun handleLongClick(longClickedTab: BottomBarTab): Boolean {
        val areInactiveTitlesHidden = isShiftingMode || isTabletMode
        val isClickedTitleHidden = !longClickedTab.isActive
        val shouldShowHint = (areInactiveTitlesHidden
                && isClickedTitleHidden
                && longPressHintsEnabled)

        if (shouldShowHint) {
            Toast.makeText(context, longClickedTab.title, Toast.LENGTH_SHORT)
                    .show()
        }

        return true
    }

    private fun updateSelectedTab(newPosition: Int) {
        val newTabId = getTabAtPosition(newPosition)!!.id
        lastTabSelected = getTabAtPosition(currentTabPosition)!!.id

        if (newPosition != currentTabPosition) {
            if (onTabSelectListener != null) {
                onTabSelectListener!!.onTabSelected(newTabId)
            }
        } else if (onTabReselectListener != null && !ignoreTabReselectionListener) {
            onTabReselectListener!!.onTabReSelected(newTabId)
        }

        currentTabPosition = newPosition

        if (ignoreTabReselectionListener) {
            ignoreTabReselectionListener = false
        }
    }

    private fun shiftingMagic(oldTab: BottomBarTab, newTab: BottomBarTab, animate: Boolean) {
        if (isShiftingMode) {
            oldTab.updateWidth(inActiveShiftingItemWidth.toFloat(), animate)
            newTab.updateWidth(activeShiftingItemWidth.toFloat(), animate)
        }
    }

    private fun handleBackgroundColorChange(tab: BottomBarTab, animate: Boolean) {
        val newColor = tab.barColorWhenSelected

        if (currentBackgroundColor == newColor) {
            return
        }

        if (!animate) {
            outerContainer!!.setBackgroundColor(newColor)
            return
        }

        var clickedView: View = tab

        if (tab.hasActiveBadge()) {
            clickedView = tab.outerView
        }

        animateBGColorChange(clickedView, newColor)
        currentBackgroundColor = newColor
    }

    private fun animateBGColorChange(clickedView: View, newColor: Int) {
        prepareForBackgroundColorAnimation(newColor)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!outerContainer!!.isAttachedToWindow) {
                return
            }

            backgroundCircularRevealAnimation(clickedView, newColor)
        } else {
            backgroundCrossfadeAnimation(newColor)
        }
    }

    private fun prepareForBackgroundColorAnimation(newColor: Int) {
        outerContainer!!.clearAnimation()
        backgroundOverlay!!.clearAnimation()

        backgroundOverlay!!.setBackgroundColor(newColor)
        backgroundOverlay!!.visibility = View.VISIBLE
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun backgroundCircularRevealAnimation(clickedView: View, newColor: Int) {
        val centerX = (clickedView.x + clickedView.measuredWidth / 2).toInt()
        val yOffset = if (isTabletMode) clickedView.y.toInt() else 0
        val centerY = yOffset + clickedView.measuredHeight / 2
        val startRadius = 0
        val finalRadius = if (isTabletMode) outerContainer!!.height else outerContainer!!.width

        val animator = ViewAnimationUtils.createCircularReveal(
                backgroundOverlay,
                centerX,
                centerY,
                startRadius.toFloat(),
                finalRadius.toFloat()
        )

        if (isTabletMode) {
            animator.duration = 500
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onEnd()
            }

            override fun onAnimationCancel(animation: Animator) {
                onEnd()
            }

            private fun onEnd() {
                outerContainer!!.setBackgroundColor(newColor)
                backgroundOverlay!!.visibility = View.INVISIBLE
                backgroundOverlay!!.alpha = 1f
            }
        })

        animator.start()
    }

    private fun backgroundCrossfadeAnimation(newColor: Int) {
        backgroundOverlay!!.alpha = 0f
        ViewCompat.animate(backgroundOverlay)
                .alpha(1f)
                .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                    override fun onAnimationEnd(view: View?) {
                        onEnd()
                    }

                    override fun onAnimationCancel(view: View?) {
                        onEnd()
                    }

                    private fun onEnd() {
                        outerContainer!!.setBackgroundColor(newColor)
                        backgroundOverlay!!.visibility = View.INVISIBLE
                        backgroundOverlay!!.alpha = 1f
                    }
                })
                .start()
    }

    companion object {
        // Behaviors
        private const val BEHAVIOR_NONE = 0
        private const val BEHAVIOR_SHIFTING = 1
        private const val BEHAVIOR_SHY = 2
        private const val BEHAVIOR_DRAW_UNDER_NAV = 4
        private const val BEHAVIOR_ICONS_ONLY = 8

        private const val STATE_CURRENT_SELECTED_TAB = "STATE_CURRENT_SELECTED_TAB"
        private const val DEFAULT_INACTIVE_SHIFTING_TAB_ALPHA = 0.6f
    }
}