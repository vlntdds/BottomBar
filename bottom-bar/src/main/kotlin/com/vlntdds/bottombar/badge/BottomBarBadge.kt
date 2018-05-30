package com.vlntdds.bottombar.badge

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatTextView
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.vlntdds.bottombar.*
import com.vlntdds.bottombar.tab.BottomBarTab
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
internal class BottomBarBadge(context: Context) : AppCompatTextView(context) {
    /**
     * Get the currently showing count for this Badge.
     *
     * @return current count for the Badge.
     */
    /**
     * Set the unread / new item / whatever count for this Badge.
     *
     */
    var count: Int = 0
        set(count) {
            field = count
            text = count.toString()
        }
    /**
     * Is this badge currently visible?
     *
     * @return true is this badge is visible, otherwise false.
     */
    var isVisible = false
        private set

    /**
     * Shows the badge with a neat little scale animation.
     */
    fun show() {
        isVisible = true
        ViewCompat.animate(this)
                .setDuration(150)
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .start()
    }

    /**
     * Hides the badge with a neat little scale animation.
     */
    fun hide() {
        isVisible = false
        ViewCompat.animate(this)
                .setDuration(150)
                .alpha(0f)
                .scaleX(0f)
                .scaleY(0f)
                .start()
    }

    fun attachToTab(tab: BottomBarTab, backgroundColor: Int) {
        val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        layoutParams = params
        gravity = Gravity.CENTER
        MiscUtils.setTextAppearance(this, R.style.BB_BottomBarBadge_Text)

        setColoredCircleBackground(backgroundColor)
        wrapTabAndBadgeInSameContainer(tab)
    }

    fun setColoredCircleBackground(circleColor: Int) {
        val innerPadding = MiscUtils.dpToPixel(context, 1f)
        val backgroundCircle = BadgeCircle.make(innerPadding * 3, circleColor)
        setPadding(innerPadding, innerPadding, innerPadding, innerPadding)
        setBackgroundCompat(backgroundCircle)
    }

    private fun wrapTabAndBadgeInSameContainer(tab: BottomBarTab) {
        val tabContainer = tab.parent as ViewGroup
        tabContainer.removeView(tab)

        val badgeContainer = BadgeContainer(context)
        badgeContainer.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        badgeContainer.addView(tab)
        badgeContainer.addView(this)

        tabContainer.addView(badgeContainer, tab.indexInTabContainer)

        badgeContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT < 16) {
                    @Suppress("DEPRECATION")
                    badgeContainer.viewTreeObserver.removeGlobalOnLayoutListener(this)
                } else {
                    badgeContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
                adjustPositionAndSize(tab)
            }
        })
    }

    fun removeFromTab(tab: BottomBarTab) {
        val badgeAndTabContainer = parent as BadgeContainer
        val originalTabContainer = badgeAndTabContainer.parent as ViewGroup

        badgeAndTabContainer.removeView(tab)
        originalTabContainer.removeView(badgeAndTabContainer)
        originalTabContainer.addView(tab, tab.indexInTabContainer)
    }

    fun adjustPositionAndSize(tab: BottomBarTab) {
        val iconView = tab.iconView
        val params = layoutParams

        val size = Math.max(width, height)
        val xOffset = (iconView?.width!! / 1.25).toFloat()

        x = iconView.x + xOffset
        translationY = 10f

        if (params.width != size || params.height != size) {
            params.width = size
            params.height = size
            layoutParams = params
        }
    }

    private fun setBackgroundCompat(background: Drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(background)
        } else {
            @Suppress("DEPRECATION")
            setBackgroundDrawable(background)
        }
    }
}
