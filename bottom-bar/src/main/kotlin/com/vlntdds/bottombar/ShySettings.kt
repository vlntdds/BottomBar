package com.vlntdds.bottombar

import com.vlntdds.bottombar.behavior.BottomNavigationBehavior

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

class ShySettings internal constructor(private val bottomBar: BottomBar) {
    private var pendingIsVisibleInShyMode: Boolean? = null

    internal fun shyHeightCalculated() {
        updatePendingShyVisibility()
    }

    /**
     * Shows the BottomBar if it was hidden, with a translate animation.
     */
    fun showBar() {
        toggleIsVisibleInShyMode(true)
    }

    /**
     * Hides the BottomBar in if it was visible, with a translate animation.
     */
    fun hideBar() {
        toggleIsVisibleInShyMode(false)
    }

    private fun toggleIsVisibleInShyMode(visible: Boolean) {
        if (!bottomBar.isShy) {
            return
        }

        if (bottomBar.isShyHeightAlreadyCalculated) {
            val behavior = BottomNavigationBehavior.from(bottomBar)
            val isHidden = !visible
            behavior.setHidden(bottomBar, isHidden)
        } else {
            pendingIsVisibleInShyMode = true
        }
    }

    private fun updatePendingShyVisibility() {
        if (pendingIsVisibleInShyMode != null) {
            toggleIsVisibleInShyMode(pendingIsVisibleInShyMode!!)
            pendingIsVisibleInShyMode = null
        }
    }
}
