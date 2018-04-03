package com.james602152002.multiaxiscardlayoutmanager.ui;

import android.content.Context;
import android.test.AndroidTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultiAxisCardRecyclerViewTest extends AndroidTestCase {

    private MultiAxisCardRecyclerView recyclerView;

    @Before
    public void setUp() throws Exception {
        recyclerView = new MultiAxisCardRecyclerView(getContext());
    }

    @Test
    public void testContext() {
        final Context context = getContext();
        recyclerView = new MultiAxisCardRecyclerView(context);
        recyclerView = new MultiAxisCardRecyclerView(context, null);
        recyclerView = new MultiAxisCardRecyclerView(context, null, 0);
    }

    @After
    public void tearDown() throws Exception {
    }
}