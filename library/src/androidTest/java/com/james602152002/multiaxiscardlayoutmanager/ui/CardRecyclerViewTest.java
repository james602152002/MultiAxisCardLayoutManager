package com.james602152002.multiaxiscardlayoutmanager.ui;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;


public class CardRecyclerViewTest extends TestCase{

    public CardRecyclerView recyclerView;

    @Before
    public void setUp() throws Exception {
        final Context mContext = InstrumentationRegistry.getTargetContext();
        recyclerView = new CardRecyclerView(mContext);
    }

    @After
    public void tearDown() throws Exception {
        recyclerView = null;
    }
}