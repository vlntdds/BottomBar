package com.vlntdds.bottombar.tab.listeners

import android.support.annotation.IdRes

import com.vlntdds.bottombar.tab.BottomBarTab

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

interface TabSelectionInterceptor {
    /**
     * The method being called when currently visible [BottomBarTab] is about to change.
     *
     *
     * This listener is fired when the current [BottomBar] is about to change. This gives
     * an opportunity to interrupt the [BottomBarTab] change.
     *
     * @param oldTabId the currently visible [BottomBarTab]
     * @param newTabId the [BottomBarTab] that will be switched to
     * @return true if you want to override/stop the tab change, false to continue as normal
     */
    fun shouldInterceptTabSelection(@IdRes oldTabId: Int, @IdRes newTabId: Int): Boolean
}
