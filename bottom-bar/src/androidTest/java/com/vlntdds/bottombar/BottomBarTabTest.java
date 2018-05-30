package com.vlntdds.bottombar;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.widget.FrameLayout;

import com.vlntdds.bottombar.tab.BottomBarTab;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class BottomBarTabTest {
    private BottomBarTab tab;

    @Before
    public void setUp() {
        FrameLayout tabContainer = new FrameLayout(InstrumentationRegistry.getContext());
        tab = new BottomBarTab(InstrumentationRegistry.getContext());

        tabContainer.addView(tab);
    }

    @Test
    public void correctLayoutReturnedForFixedTab() {
        tab.setType(BottomBarTab.Type.FIXED);
        assertEquals(R.layout.bb_bottom_bar_item_fixed, tab.getLayoutResource());
    }

    @Test(expected = IllegalStateException.class)
    public void setIsTitleless_WhenTrueAndIconDoesNotExist_ThrowsException() {
        tab.setTitleless(true);
        assertEquals(R.layout.bb_bottom_bar_item_titleless, tab.getLayoutResource());
    }

    @Test
    public void correctLayoutForShiftingTab() {
        tab.setType(BottomBarTab.Type.SHIFTING);
        assertEquals(R.layout.bb_bottom_bar_item_shifting, tab.getLayoutResource());
    }

    @Test
    public void correctLayoutForTabletTab() {
        tab.setType(BottomBarTab.Type.TABLET);
        assertEquals(R.layout.bb_bottom_bar_item_fixed_tablet, tab.getLayoutResource());
    }

    @Test
    public void testSavedStateWithBadge_StaysIntact() {
        tab.setBadgeCount(5);

        Bundle savedState = (Bundle) tab.onSaveInstanceState();
        assert savedState != null;
        assertEquals(5, savedState.getInt("STATE_BADGE_COUNT_FOR_TAB_" + 69));

        tab.setBadgeCount(9);
        assertEquals(9, Objects.requireNonNull(tab.getBadge()).getCount());

        tab.onRestoreInstanceState(savedState);
        assertEquals(5, tab.getBadge().getCount());
    }
}
