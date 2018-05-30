package com.vlntdds.bottombar.behavior

import android.os.Build
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorCompat
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.view.View
import android.view.ViewGroup

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

internal class BottomNavigationBehavior<V : View>(private val bottomNavHeight: Int, private val defaultOffset: Int, tablet: Boolean) : VerticalScrollingBehavior<V>() {

    private var isTablet = false
    private var mTranslationAnimator: ViewPropertyAnimatorCompat? = null
    private var hidden = false
    private var mSnackbarHeight = -1
    private val mWithSnackBarImpl = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) LollipopBottomNavWithSnackBarImpl() else PreLollipopBottomNavWithSnackBarImpl()
    private var mScrollingEnabled = true
    private val scrollDirectionUp = 1
    private val scrollDirectionDown = -1

    init {
        isTablet = tablet
    }

    override fun layoutDependsOn(parent: CoordinatorLayout?, child: V?, dependency: View?): Boolean {
        mWithSnackBarImpl.updateSnackbar(parent, dependency, child)
        return dependency is Snackbar.SnackbarLayout
    }

    public override fun onNestedVerticalOverScroll(coordinatorLayout: CoordinatorLayout, child: V, direction: Int, currentOverScroll: Int, totalOverScroll: Int) {}

    override fun onDependentViewRemoved(parent: CoordinatorLayout?, child: V?, dependency: View?) {
        updateScrollingForSnackbar(dependency, true)
        super.onDependentViewRemoved(parent, child, dependency)
    }

    private fun updateScrollingForSnackbar(dependency: View?, enabled: Boolean) {
        if (!isTablet && dependency is Snackbar.SnackbarLayout) {
            mScrollingEnabled = enabled
        }
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: V?, dependency: View?): Boolean {
        updateScrollingForSnackbar(dependency, false)
        return super.onDependentViewChanged(parent, child, dependency)
    }

    public override fun onDirectionNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int, consumed: IntArray, scrollDirection: Int) {
        handleDirection(child, scrollDirection)
    }

    private fun handleDirection(child: V, scrollDirection: Int) {
        if (!mScrollingEnabled) return
        if (scrollDirection == scrollDirectionDown && hidden) {
            hidden = false
            animateOffset(child, defaultOffset)
        } else if (scrollDirection == scrollDirectionUp && !hidden) {
            hidden = true
            animateOffset(child, bottomNavHeight + defaultOffset)
        }
    }

    override fun onNestedDirectionFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float, scrollDirection: Int): Boolean {
        handleDirection(child, scrollDirection)
        return true
    }

    private fun animateOffset(child: V, offset: Int) {
        ensureOrCancelAnimator(child)
        mTranslationAnimator!!.translationY(offset.toFloat()).start()
    }

    private fun ensureOrCancelAnimator(child: V) {
        if (mTranslationAnimator == null) {
            mTranslationAnimator = ViewCompat.animate(child)
            mTranslationAnimator!!.duration = 300
            mTranslationAnimator!!.interpolator = INTERPOLATOR
        } else {
            mTranslationAnimator!!.cancel()
        }
    }

    fun setHidden(view: V, bottomLayoutHidden: Boolean) {
        if (!bottomLayoutHidden && hidden) {
            animateOffset(view, defaultOffset)
        } else if (bottomLayoutHidden && !hidden) {
            animateOffset(view, bottomNavHeight + defaultOffset)
        }
        hidden = bottomLayoutHidden
    }

    private interface BottomNavigationWithSnackbar {
        fun updateSnackbar(parent: CoordinatorLayout?, dependency: View?, child: View?)
    }


    private inner class PreLollipopBottomNavWithSnackBarImpl : BottomNavigationWithSnackbar {

        override fun updateSnackbar(parent: CoordinatorLayout?, dependency: View?, child: View?) {
            if (!isTablet && dependency is Snackbar.SnackbarLayout) {
                if (mSnackbarHeight == -1) {
                    mSnackbarHeight = dependency.height
                }
                if (child!!.translationY != 0f) return
                val targetPadding = bottomNavHeight + mSnackbarHeight - defaultOffset

                val layoutParams = dependency.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.bottomMargin = targetPadding
                child.bringToFront()
                child.parent.requestLayout()
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    (child.parent as View).invalidate()
                }

            }
        }
    }

    private inner class LollipopBottomNavWithSnackBarImpl : BottomNavigationWithSnackbar {
        override fun updateSnackbar(parent: CoordinatorLayout?, dependency: View?, child: View?) {
            if (!isTablet && dependency is Snackbar.SnackbarLayout) {
                if (mSnackbarHeight == -1) {
                    mSnackbarHeight = dependency.height
                }
                if (child!!.translationY != 0f) return
                val targetPadding = mSnackbarHeight + bottomNavHeight - defaultOffset
                dependency.setPadding(dependency.paddingLeft,
                        dependency.paddingTop, dependency.paddingRight, targetPadding
                )
            }
        }
    }

    companion object {
        private val INTERPOLATOR = LinearOutSlowInInterpolator()

        fun <V : View> from(view: V): BottomNavigationBehavior<V> {
            val params = view.layoutParams as? CoordinatorLayout.LayoutParams
                    ?: throw IllegalArgumentException("The view is not a child of CoordinatorLayout")

            val behavior = params.behavior
            if (behavior is BottomNavigationBehavior<View>) {
                @Suppress("UNCHECKED_CAST")
                return behavior as BottomNavigationBehavior<V>
            }

            throw IllegalArgumentException("The view is not associated with BottomNavigationBehavior")
        }
    }
}
