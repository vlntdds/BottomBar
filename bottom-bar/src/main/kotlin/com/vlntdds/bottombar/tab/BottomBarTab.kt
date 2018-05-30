package com.vlntdds.bottombar.tab

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.ColorInt
import android.support.annotation.VisibleForTesting
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatImageView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.vlntdds.bottombar.R
import com.vlntdds.bottombar.badge.BottomBarBadge
import com.vlntdds.bottombar.util.MiscUtils

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

class BottomBarTab internal constructor(context: Context) : LinearLayout(context) {

    private val sixDps: Int = MiscUtils.dpToPixel(context, 6f)
    private val eightDps: Int = MiscUtils.dpToPixel(context, 8f)
    private val sixteenDps: Int = MiscUtils.dpToPixel(context, 16f)

    @VisibleForTesting
    internal var badge: BottomBarBadge? = null

    var type = Type.FIXED

    var isTitleless: Boolean = false
        set(setTitleless) {
            if (isTitleless && iconResId == 0) {
                throw IllegalStateException("This tab is supposed to be " +
                        "icon only, yet it has no icon specified. Index in " +
                        "container: " + indexInTabContainer)
            }

            field = isTitleless
        }

    internal var iconResId: Int = 0

    var title: String? = null
        set(title) {
            field = title
            updateTitle()
        }

    var inActiveAlpha: Float = 0.toFloat()
        set(inActiveAlpha) {
            field = inActiveAlpha

            if (!isActive) {
                setAlphas(inActiveAlpha)
            }
        }

    var activeAlpha: Float = 0.toFloat()
        set(activeAlpha) {
            field = activeAlpha

            if (isActive) {
                setAlphas(activeAlpha)
            }
        }

    var inActiveColor: Int = 0
        set(inActiveColor) {
            field = inActiveColor

            if (!isActive) {
                setColors(inActiveColor)
            }
        }

    var activeColor: Int = 0
        set(activeIconColor) {
            field = activeIconColor

            if (isActive) {
                setColors(this.activeColor)
            }
        }

    var barColorWhenSelected: Int = 0

    var badgeBackgroundColor: Int = 0
        set(badgeBackgroundColor) {
            field = badgeBackgroundColor

            if (badge != null) {
                badge!!.setColoredCircleBackground(badgeBackgroundColor)
            }
        }

    var badgeHidesWhenActive: Boolean = false

    internal var iconView: AppCompatImageView? = null

    internal var titleView: TextView? = null
        private set

    internal var isActive: Boolean = false
        private set

    internal var indexInTabContainer: Int = 0

    var titleTextAppearance: Int = 0
        internal set(resId) {
            field = resId
            updateCustomTextAppearance()
        }

    var titleTypeFace: Typeface? = null
        private set

    val layoutResource: Int
        @VisibleForTesting
        get() {
            return when (type) {
                Type.FIXED -> R.layout.bb_bottom_bar_item_fixed
                Type.SHIFTING -> R.layout.bb_bottom_bar_item_shifting
                Type.TABLET -> R.layout.bb_bottom_bar_item_fixed_tablet
            }
        }

    val outerView: ViewGroup
        get() = parent as ViewGroup

    fun setConfig(config: Config) {
        inActiveAlpha = config.inActiveTabAlpha
        activeAlpha = config.activeTabAlpha
        inActiveColor = config.inActiveTabColor
        activeColor = config.activeTabColor
        barColorWhenSelected = config.barColorWhenSelected
        badgeBackgroundColor = config.badgeBackgroundColor
        badgeHidesWhenActive = config.badgeHidesWhenSelected
        titleTextAppearance = config.titleTextAppearance
        setTitleTypeface(config.titleTypeFace)
    }

    internal fun prepareLayout() {
        View.inflate(context, layoutResource, this)
        orientation = LinearLayout.VERTICAL
        gravity = if (this.isTitleless) Gravity.CENTER else Gravity.CENTER_HORIZONTAL
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        setBackgroundResource(MiscUtils.getDrawableRes(context, R.attr.selectableItemBackgroundBorderless))

        iconView = findViewById<View>(R.id.bb_bottom_bar_icon) as AppCompatImageView
        iconView!!.setImageResource(iconResId)

        if (type != Type.TABLET && !this.isTitleless) {
            titleView = findViewById<View>(R.id.bb_bottom_bar_title) as TextView
            titleView!!.visibility = View.VISIBLE

            if (type == Type.SHIFTING) {
                findViewById<View>(R.id.spacer).visibility = View.VISIBLE
            }

            updateTitle()
        }

        updateCustomTextAppearance()
        updateCustomTypeface()
    }

    private fun updateTitle() {
        if (titleView != null) {
            titleView!!.text = this.title
        }
    }

    private fun updateCustomTextAppearance() {
        if (titleView == null || titleTextAppearance == 0) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            titleView!!.setTextAppearance(titleTextAppearance)
        } else {
            @Suppress("DEPRECATION")
            titleView!!.setTextAppearance(context, titleTextAppearance)
        }

        titleView!!.setTag(R.id.bb_bottom_bar_appearance_id, titleTextAppearance)
    }

    private fun updateCustomTypeface() {
        if (titleTypeFace != null && titleView != null) {
            titleView!!.typeface = titleTypeFace
        }
    }

    fun setBadgeCount(count: Int) {
        if (count <= 0) {
            if (badge != null) {
                badge!!.removeFromTab(this)
                badge = null
            }

            return
        }

        if (badge == null) {
            badge = BottomBarBadge(context)
            badge!!.attachToTab(this, this.badgeBackgroundColor)
        }

        badge!!.count = count

        if (isActive && badgeHidesWhenActive) {
            badge!!.hide()
        }
    }

    fun removeBadge() {
        setBadgeCount(0)
    }

    internal fun hasActiveBadge(): Boolean {
        return badge != null
    }

    internal fun setIconTint(tint: Int) {
        iconView!!.setColorFilter(tint)
    }

    fun setTitleTypeface(typeface: Typeface?) {
        this.titleTypeFace = typeface
        updateCustomTypeface()
    }

    internal fun select(animate: Boolean) {
        isActive = true

        if (animate) {
            animateIcon(this.activeAlpha, ACTIVE_SHIFTING_TITLELESS_ICON_SCALE)
            animateTitle(sixDps, ACTIVE_TITLE_SCALE, this.activeAlpha)
            animateColors(this.inActiveColor, this.activeColor)
        } else {
            setTitleScale(ACTIVE_TITLE_SCALE)
            setTopPadding(sixDps)
            setIconScale(ACTIVE_SHIFTING_TITLELESS_ICON_SCALE)
            setColors(this.activeColor)
            setAlphas(this.activeAlpha)
        }

        isSelected = true

        if (badge != null && badgeHidesWhenActive) {
            badge!!.hide()
        }
    }

    internal fun deselect(animate: Boolean) {
        isActive = false

        val isShifting = type == Type.SHIFTING

        val titleScale = if (isShifting) 0f else INACTIVE_FIXED_TITLE_SCALE
        val iconPaddingTop = if (isShifting) sixteenDps else eightDps

        if (animate) {
            animateTitle(iconPaddingTop, titleScale, this.inActiveAlpha)
            animateIcon(this.inActiveAlpha, INACTIVE_SHIFTING_TITLELESS_ICON_SCALE)
            animateColors(this.activeColor, this.inActiveColor)
        } else {
            setTitleScale(titleScale)
            setTopPadding(iconPaddingTop)
            setIconScale(INACTIVE_SHIFTING_TITLELESS_ICON_SCALE)
            setColors(this.inActiveColor)
            setAlphas(this.inActiveAlpha)
        }

        isSelected = false

        if (!isShifting && badge != null && !badge!!.isVisible) {
            badge!!.show()
        }
    }

    private fun animateColors(previousColor: Int, color: Int) {
        val anim = ValueAnimator()
        anim.setIntValues(previousColor, color)
        anim.setEvaluator(ArgbEvaluator())
        anim.addUpdateListener { valueAnimator -> setColors(valueAnimator.animatedValue as Int) }

        anim.duration = 150
        anim.start()
    }

    private fun setColors(color: Int) {
        iconView?.setColorFilter(color)
        iconView?.setTag(R.id.bb_bottom_bar_color_id, color)
        titleView?.setTextColor(color)
    }

    private fun setAlphas(alpha: Float) {
        iconView?.alpha = alpha
        titleView?.alpha = alpha
    }

    internal fun updateWidth(endWidth: Float, animated: Boolean) {
        if (!animated) {
            layoutParams.width = endWidth.toInt()

            if (!isActive && badge != null) {
                badge!!.adjustPositionAndSize(this)
                badge!!.show()
            }
            return
        }

        val start = width.toFloat()

        val animator = ValueAnimator.ofFloat(start, endWidth)
        animator.duration = 150
        animator.addUpdateListener(ValueAnimator.AnimatorUpdateListener { changedAnimator ->
            val params = layoutParams ?: return@AnimatorUpdateListener

            params.width = Math.round(changedAnimator.animatedValue as Float)
            layoutParams = params
        })

        postDelayed({
            if (!isActive && badge != null) {
                clearAnimation()
                badge!!.adjustPositionAndSize(this@BottomBarTab)
                badge!!.show()
            }
        }, animator.duration)

        animator.start()
    }

    private fun updateBadgePosition() {
        if (badge != null) {
            badge!!.adjustPositionAndSize(this)
        }
    }

    private fun setTopPaddingAnimated(start: Int, end: Int) {
        if (type == Type.TABLET || this.isTitleless) {
            return
        }

        val paddingAnimator = ValueAnimator.ofInt(start, end)
        paddingAnimator.addUpdateListener { animation ->
            iconView!!.setPadding(
                    iconView!!.paddingLeft,
                    animation.animatedValue as Int,
                    iconView!!.paddingRight,
                    iconView!!.paddingBottom
            )
        }

        paddingAnimator.duration = ANIMATION_DURATION
        paddingAnimator.start()
    }

    private fun animateTitle(padding: Int, scale: Float, alpha: Float) {
        if (type == Type.TABLET && this.isTitleless) {
            return
        }

        setTopPaddingAnimated(iconView!!.paddingTop, padding)

        val titleAnimator = ViewCompat.animate(titleView)
                .setDuration(ANIMATION_DURATION)
                .scaleX(scale)
                .scaleY(scale)
        titleAnimator.alpha(alpha)
        titleAnimator.start()
    }

    private fun animateIconScale(scale: Float) {
        ViewCompat.animate(iconView)
                .setDuration(ANIMATION_DURATION)
                .scaleX(scale)
                .scaleY(scale)
                .start()
    }

    private fun animateIcon(alpha: Float, scale: Float) {
        ViewCompat.animate(iconView)
                .setDuration(ANIMATION_DURATION)
                .alpha(alpha)
                .start()

        if (this.isTitleless && type == Type.SHIFTING) {
            animateIconScale(scale)
        }
    }

    private fun setTopPadding(topPadding: Int) {
        if (type == Type.TABLET || this.isTitleless) {
            return
        }

        iconView!!.setPadding(
                iconView!!.paddingLeft,
                topPadding,
                iconView!!.paddingRight,
                iconView!!.paddingBottom
        )
    }

    private fun setTitleScale(scale: Float) {
        if (type == Type.TABLET || this.isTitleless) {
            return
        }

        titleView!!.scaleX = scale
        titleView!!.scaleY = scale
    }

    private fun setIconScale(scale: Float) {
        if (this.isTitleless && type == Type.SHIFTING) {
            iconView!!.scaleX = scale
            iconView!!.scaleY = scale
        }
    }

    public override fun onSaveInstanceState(): Parcelable? {
        if (badge != null) {
            val bundle = saveState()
            bundle.putParcelable("superstate", super.onSaveInstanceState())

            return bundle
        }

        return super.onSaveInstanceState()
    }

    @VisibleForTesting
    private fun saveState(): Bundle {
        val outState = Bundle()
        outState.putInt(STATE_BADGE_COUNT + indexInTabContainer, badge!!.count)

        return outState
    }

    public override fun onRestoreInstanceState(state: Parcelable?) {
        var savedState = state
        if (savedState is Bundle) {
            val bundle = savedState as Bundle?
            restoreState(savedState)

            savedState = bundle!!.getParcelable("superstate")
        }

        super.onRestoreInstanceState(savedState)
    }

    @VisibleForTesting
    internal fun restoreState(savedInstanceState: Bundle) {
        val previousBadgeCount = savedInstanceState.getInt(STATE_BADGE_COUNT + indexInTabContainer)
        setBadgeCount(previousBadgeCount)
    }

    enum class Type {
        FIXED, SHIFTING, TABLET
    }

    class Config private constructor(builder: Builder) {
        val inActiveTabAlpha: Float
        val activeTabAlpha: Float
        val inActiveTabColor: Int
        val activeTabColor: Int
        val barColorWhenSelected: Int
        val badgeBackgroundColor: Int
        val titleTextAppearance: Int
        val titleTypeFace: Typeface?
        var badgeHidesWhenSelected = true

        init {
            this.inActiveTabAlpha = builder.inActiveTabAlpha
            this.activeTabAlpha = builder.activeTabAlpha
            this.inActiveTabColor = builder.inActiveTabColor
            this.activeTabColor = builder.activeTabColor
            this.barColorWhenSelected = builder.barColorWhenSelected
            this.badgeBackgroundColor = builder.badgeBackgroundColor
            this.badgeHidesWhenSelected = builder.hidesBadgeWhenSelected
            this.titleTextAppearance = builder.titleTextAppearance
            this.titleTypeFace = builder.titleTypeFace
        }

        class Builder {
            var inActiveTabAlpha: Float = 0.toFloat()
            var activeTabAlpha: Float = 0.toFloat()
            var inActiveTabColor: Int = 0
            var activeTabColor: Int = 0
            var barColorWhenSelected: Int = 0
            var badgeBackgroundColor: Int = 0
            var hidesBadgeWhenSelected = true
            var titleTextAppearance: Int = 0
            var titleTypeFace: Typeface? = null
            var isVisible = true

            fun inActiveTabAlpha(alpha: Float): Builder {
                this.inActiveTabAlpha = alpha
                return this
            }

            fun activeTabAlpha(alpha: Float): Builder {
                this.activeTabAlpha = alpha
                return this
            }

            fun inActiveTabColor(@ColorInt color: Int): Builder {
                this.inActiveTabColor = color
                return this
            }

            fun activeTabColor(@ColorInt color: Int): Builder {
                this.activeTabColor = color
                return this
            }

            fun barColorWhenSelected(@ColorInt color: Int): Builder {
                this.barColorWhenSelected = color
                return this
            }

            fun badgeBackgroundColor(@ColorInt color: Int): Builder {
                this.badgeBackgroundColor = color
                return this
            }

            fun hideBadgeWhenSelected(hide: Boolean): Builder {
                this.hidesBadgeWhenSelected = hide
                return this
            }

            fun titleTextAppearance(titleTextAppearance: Int): Builder {
                this.titleTextAppearance = titleTextAppearance
                return this
            }

            fun titleTypeFace(titleTypeFace: Typeface?): Builder {
                this.titleTypeFace = titleTypeFace
                return this
            }

            fun build(): Config {
                return Config(this)
            }
        }
    }

    companion object {
        private const val ANIMATION_DURATION: Long = 150
        private const val ACTIVE_TITLE_SCALE = 1f
        private const val INACTIVE_FIXED_TITLE_SCALE = 0.86f
        private const val ACTIVE_SHIFTING_TITLELESS_ICON_SCALE = 1.24f
        private const val INACTIVE_SHIFTING_TITLELESS_ICON_SCALE = 1f

        @VisibleForTesting
        internal val STATE_BADGE_COUNT = "STATE_BADGE_COUNT_FOR_TAB_"
    }
}
