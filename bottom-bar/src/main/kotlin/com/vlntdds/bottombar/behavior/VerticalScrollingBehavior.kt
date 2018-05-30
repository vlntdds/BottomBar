package com.vlntdds.bottombar.behavior

import android.annotation.SuppressLint
import android.support.design.widget.CoordinatorLayout
import android.view.View

/*
 * BottomBar library for Android
 * Copyright (c) 2016 Iiro Krankka (http://github.com/roughike).
 * Copyright (c) 2018 Eduardo Calazans Júnior (http://github.com/vlntdds).
 * Copyright (c) 2016 Nikola Despotoski (https://github.com/NikolaDespotoski).
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

@Suppress("DEPRECATION")
internal abstract class VerticalScrollingBehavior<V : View> : CoordinatorLayout.Behavior<V>() {

    private var totalDyUnconsumed = 0
    private var totalDy = 0
    private val scrollDirectionUp = 1
    private val scrollDirectionDown = -1
    private val scrollNone = 0

    private var overScrollDirection = scrollNone
    private var scrollDirection = scrollNone

    /**
     * @param coordinatorLayout
     * @param child
     * @param direction         Direction of the overscroll: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN
     * @param currentOverScroll Unconsumed value, negative or positive based on the direction;
     * @param totalOverScroll   Cumulative value for current direction
     */
    internal abstract fun onNestedVerticalOverScroll(coordinatorLayout: CoordinatorLayout, child: V, direction: Int, currentOverScroll: Int, totalOverScroll: Int)

    /**
     * @param scrollDirection Direction of the overscroll: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN
     */
    internal abstract fun onDirectionNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int, consumed: IntArray, scrollDirection: Int)

    @SuppressLint("InlinedApi")
    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, directTargetChild: View, target: View, nestedScrollAxes: Int): Boolean {
        return nestedScrollAxes and View.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        if (dyUnconsumed > 0 && totalDyUnconsumed < 0) {
            totalDyUnconsumed = 0
            overScrollDirection = scrollDirectionUp
        } else if (dyUnconsumed < 0 && totalDyUnconsumed > 0) {
            totalDyUnconsumed = 0
            overScrollDirection = scrollDirectionDown
        }
        totalDyUnconsumed += dyUnconsumed
        onNestedVerticalOverScroll(coordinatorLayout, child, overScrollDirection, dyConsumed, totalDyUnconsumed)
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int, consumed: IntArray) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed)
        if (dy > 0 && totalDy < 0) {
            totalDy = 0
            scrollDirection = scrollDirectionUp
        } else if (dy < 0 && totalDy > 0) {
            totalDy = 0
            scrollDirection = scrollDirectionDown
        }
        totalDy += dy
        onDirectionNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, scrollDirection)
    }


    override fun onNestedFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
        scrollDirection = if (velocityY > 0) scrollDirectionUp else scrollDirectionDown
        return onNestedDirectionFling(coordinatorLayout, child, target, velocityX, velocityY, scrollDirection)
    }

    internal abstract fun onNestedDirectionFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float, scrollDirection: Int): Boolean

}