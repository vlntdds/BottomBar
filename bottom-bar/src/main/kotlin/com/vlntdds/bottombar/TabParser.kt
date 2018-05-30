package com.vlntdds.bottombar

import android.content.Context
import android.content.res.XmlResourceParser
import android.graphics.Color
import android.support.annotation.CheckResult
import android.support.annotation.ColorInt
import android.support.annotation.IntRange
import android.support.annotation.XmlRes
import android.support.v4.content.ContextCompat
import com.vlntdds.bottombar.tab.BottomBarTab

import org.xmlpull.v1.XmlPullParserException

import java.io.IOException
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

internal class TabParser(private val context: Context, private val defaultTabConfig: BottomBarTab.Config, @XmlRes tabsXmlResId: Int) {

    private val parser: XmlResourceParser = context.resources.getXml(tabsXmlResId)

    private var tabs: MutableList<BottomBarTab>? = null

    @CheckResult
    fun parseTabs(): List<BottomBarTab> {
        if (tabs == null) {
            tabs = ArrayList(AVG_NUMBER_OF_TABS)
            try {
                var eventType: Int
                do {
                    eventType = parser.next()
                    if (eventType == XmlResourceParser.START_TAG && TAB_TAG == parser.name) {
                        val bottomBarTab = parseNewTab(parser, tabs!!.size)
                        tabs!!.add(bottomBarTab)
                    }
                } while (eventType != XmlResourceParser.END_DOCUMENT)
            } catch (e: IOException) {
                e.printStackTrace()
                throw TabParserException()
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
                throw TabParserException()
            }

        }

        return tabs as MutableList<BottomBarTab>
    }

    private fun parseNewTab(parser: XmlResourceParser, @IntRange(from = 0) containerPosition: Int): BottomBarTab {
        val workingTab = tabWithDefaults()
        workingTab.indexInTabContainer = containerPosition

        val numberOfAttributes = parser.attributeCount
        loop@ for (i in 0 until numberOfAttributes) {
            val attrName = parser.getAttributeName(i)
            when (attrName) {
                "id" -> workingTab.id = parser.getIdAttributeResourceValue(i)
                "icon" -> workingTab.iconResId = parser.getAttributeResourceValue(i, RESOURCE_NOT_FOUND)
                "title" -> workingTab.title = getTitleValue(parser, i)
                "inActiveColor" -> {
                    val inactiveColor = getColorValue(parser, i)
                    if (inactiveColor == COLOR_NOT_SET) continue@loop
                    workingTab.inActiveColor = inactiveColor
                }
                "activeColor" -> {
                    val activeColor = getColorValue(parser, i)
                    if (activeColor == COLOR_NOT_SET) continue@loop
                    workingTab.activeColor = activeColor
                }
                "barColorWhenSelected" -> {
                    val barColorWhenSelected = getColorValue(parser, i)
                    if (barColorWhenSelected == COLOR_NOT_SET) continue@loop
                    workingTab.barColorWhenSelected = barColorWhenSelected
                }
                "badgeBackgroundColor" -> {
                    val badgeBackgroundColor = getColorValue(parser, i)
                    if (badgeBackgroundColor == COLOR_NOT_SET) continue@loop
                    workingTab.badgeBackgroundColor = badgeBackgroundColor
                }
                "badgeHidesWhenActive" -> {
                    val badgeHidesWhenActive = parser.getAttributeBooleanValue(i, true)
                    workingTab.badgeHidesWhenActive = badgeHidesWhenActive
                }
                "iconOnly" -> {
                    val isTitleless = parser.getAttributeBooleanValue(i, false)
                    workingTab.isTitleless = isTitleless
                }
            }
        }

        return workingTab
    }

    private fun tabWithDefaults(): BottomBarTab {
        val tab = BottomBarTab(context)
        tab.setConfig(defaultTabConfig)

        return tab
    }

    private fun getTitleValue(parser: XmlResourceParser, @IntRange(from = 0) attrIndex: Int): String {
        val titleResource = parser.getAttributeResourceValue(attrIndex, 0)
        return if (titleResource == RESOURCE_NOT_FOUND)
            parser.getAttributeValue(attrIndex)
        else
            context.getString(titleResource)
    }

    @ColorInt
    private fun getColorValue(parser: XmlResourceParser, @IntRange(from = 0) attrIndex: Int): Int {
        val colorResource = parser.getAttributeResourceValue(attrIndex, 0)

        if (colorResource == RESOURCE_NOT_FOUND) {
            return try {
                val colorValue = parser.getAttributeValue(attrIndex)
                Color.parseColor(colorValue)
            } catch (ignored: Exception) {
                COLOR_NOT_SET
            }

        }

        return ContextCompat.getColor(context, colorResource)
    }

    // This class is just to be able to have a type of Runtime Exception that will make it clear where the error originated.
    class TabParserException : RuntimeException()

    companion object {
        private const val TAB_TAG = "tab"
        private const val AVG_NUMBER_OF_TABS = 5
        private const val COLOR_NOT_SET = -1
        private const val RESOURCE_NOT_FOUND = 0
    }
}
