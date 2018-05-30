package com.vlntdds.bottombar.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.support.annotation.AttrRes
import android.support.annotation.ColorInt
import android.support.annotation.Dimension
import android.support.annotation.DrawableRes
import android.support.annotation.Px
import android.support.annotation.StyleRes
import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.TextView

import android.support.annotation.Dimension.DP

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

internal object MiscUtils {

    fun getTypedValue(context: Context, @AttrRes resId: Int): TypedValue {
        val tv = TypedValue()
        context.theme.resolveAttribute(resId, tv, true)
        return tv
    }

    @ColorInt
    fun getColor(context: Context, @AttrRes color: Int): Int {
        return getTypedValue(context, color).data
    }

    @DrawableRes
    fun getDrawableRes(context: Context, @AttrRes drawable: Int): Int {
        return getTypedValue(context, drawable).resourceId
    }

    /**
     * Converts dps to pixels nicely.
     *
     * @param context the Context for getting the resources
     * @param dp      dimension in dps
     * @return dimension in pixels
     */
    fun dpToPixel(context: Context, @Dimension(unit = DP) dp: Float): Int {
        val resources = context.resources
        val metrics = resources.displayMetrics

        try {
            return (dp * metrics.density).toInt()
        } catch (ignored: NoSuchFieldError) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics).toInt()
        }

    }

    /**
     * Converts pixels to dps just as well.
     *
     * @param context the Context for getting the resources
     * @param px      dimension in pixels
     * @return dimension in dps
     */
    fun pixelToDp(context: Context, @Px px: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return Math.round(px / displayMetrics.density)
    }

    /**
     * Returns screen width.
     *
     * @param context Context to get resources and device specific display metrics
     * @return screen width
     */
    fun getScreenWidth(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return (displayMetrics.widthPixels / displayMetrics.density).toInt()
    }

    /**
     * A convenience method for setting text appearance.
     *
     * @param textView a TextView which textAppearance to modify.
     * @param resId    a style resource for the text appearance.
     */
    fun setTextAppearance(textView: TextView, @StyleRes resId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setTextAppearance(resId)
        } else {
            @Suppress("DEPRECATION")
            textView.setTextAppearance(textView.context, resId)
        }
    }

    /**
     * Determine if the current UI Mode is Night Mode.
     *
     * @param context Context to get the configuration.
     * @return true if the night mode is enabled, otherwise false.
     */
    fun isNightMode(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}
