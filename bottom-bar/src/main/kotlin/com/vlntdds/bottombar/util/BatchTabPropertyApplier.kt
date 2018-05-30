package com.vlntdds.bottombar.util

import com.vlntdds.bottombar.BottomBar
import com.vlntdds.bottombar.tab.BottomBarTab

internal class BatchTabPropertyApplier(private val bottomBar: BottomBar) {

    fun applyToAllTabs(function: (BottomBarTab) -> Unit) {
        val tabCount = bottomBar.tabCount

        if (tabCount > 0) {
            for (i in 0 until tabCount) {
                val tab = bottomBar.getTabAtPosition(i)
                function.invoke(tab!!)
            }
        }
    }
}
