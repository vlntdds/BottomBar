package com.vlntdds.bottombar;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.vlntdds.bottombar.badge.BottomBarBadge;
import com.vlntdds.bottombar.tab.BottomBarTab;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by iiro on 8.8.2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BadgeTest {
    private BottomBar bottomBar;
    private BottomBarTab nearby;

    @Before
    public void setUp() {
        bottomBar = new BottomBar(InstrumentationRegistry.getContext());
        bottomBar.setItems(com.vlntdds.bottombar.test.R.xml.dummy_tabs_three);
        nearby = bottomBar.getTabWithId(com.vlntdds.bottombar.test.R.id.tab_nearby);
        nearby.setBadgeCount(5);
    }

    @Test
    public void hasNoBadges_ExceptNearby() {
        assertNull(bottomBar.getTabWithId(com.vlntdds.bottombar.test.R.id.tab_favorites).getBadge());
        assertNull(bottomBar.getTabWithId(com.vlntdds.bottombar.test.R.id.tab_friends).getBadge());

        assertNotNull(nearby.getBadge());
    }

    @Test
    @UiThreadTest
    public void whenTabWithBadgeClicked_BadgeIsHidden() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                bottomBar.selectTabWithId(com.vlntdds.bottombar.test.R.id.tab_nearby);
                assertFalse(nearby.getBadge().isVisible());
            }
        });
    }

    @Test
    @UiThreadTest
    public void whenBadgeCountIsZero_BadgeIsRemoved() {
        nearby.setBadgeCount(0);
        assertNull(nearby.getBadge());
    }

    @Test
    @UiThreadTest
    public void whenBadgeCountIsNegative_BadgeIsRemoved() {
        nearby.setBadgeCount(-1);
        assertNull(nearby.getBadge());
    }

    @Test
    @UiThreadTest
    public void whenBadgeStateRestored_CountPersists() {
        nearby.setBadgeCount(1);
        assertEquals(1, nearby.getBadge().getCount());


        int tabIndex = nearby.getIndexInTabContainer();
        Bundle savedInstanceState = new Bundle();
        savedInstanceState.putInt(BottomBarTab.Companion.getSTATE_BADGE_COUNT() + tabIndex, 2);
        nearby.restoreState(savedInstanceState);

        assertEquals(2, nearby.getBadge().getCount());
    }

    @Test
    @UiThreadTest
    public void badgeRemovedProperly() {
        assertNotEquals(bottomBar.findViewById(R.id.bb_bottom_bar_item_container), nearby.getOuterView());
        assertEquals(2, nearby.getOuterView().getChildCount());
        assertTrue(nearby.getOuterView().getChildAt(1) instanceof BottomBarBadge);

        nearby.removeBadge();
        assertNull(nearby.getBadge());
        assertEquals(bottomBar.findViewById(R.id.bb_bottom_bar_item_container), nearby.getOuterView());
    }
}
