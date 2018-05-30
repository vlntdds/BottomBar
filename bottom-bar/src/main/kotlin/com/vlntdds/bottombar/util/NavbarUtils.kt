package com.vlntdds.bottombar.util

import android.content.Context
import android.os.Build
import android.support.annotation.IntRange
import android.util.DisplayMetrics
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.ViewConfiguration
import android.view.WindowManager
import com.vlntdds.bottombar.R

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

internal object NavbarUtils {
    private const val RESOURCE_NOT_FOUND = 0

    @IntRange(from = 0)
    fun getNavbarHeight(context: Context): Int {
        val res = context.resources
        val navBarIdentifier = res.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (navBarIdentifier != RESOURCE_NOT_FOUND)
            res.getDimensionPixelSize(navBarIdentifier)
        else
            0
    }

    fun shouldDrawBehindNavbar(context: Context): Boolean {
        return isPortrait(context) && hasSoftKeys(context)
    }

    private fun isPortrait(context: Context): Boolean {
        val res = context.resources
        return res.getBoolean(R.bool.bb_bottom_bar_is_portrait_mode)
    }

    /**
     * http://stackoverflow.com/a/14871974
     */
    private fun hasSoftKeys(context: Context): Boolean {
        var hasSoftwareKeys = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val d = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

            val realDisplayMetrics = DisplayMetrics()
            d.getRealMetrics(realDisplayMetrics)

            val realHeight = realDisplayMetrics.heightPixels
            val realWidth = realDisplayMetrics.widthPixels

            val displayMetrics = DisplayMetrics()
            d.getMetrics(displayMetrics)

            val displayHeight = displayMetrics.heightPixels
            val displayWidth = displayMetrics.widthPixels

            hasSoftwareKeys = realWidth - displayWidth > 0 || realHeight - displayHeight > 0
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
            val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
            hasSoftwareKeys = !hasMenuKey && !hasBackKey
        }

        return hasSoftwareKeys
    }
}
