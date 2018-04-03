package com.james602152002.multiaxiscardlayoutmanager;

import android.content.Context;
import android.test.AndroidTestCase;

import com.james602152002.multiaxiscardlayoutmanager.ui.MultiAxisCardRecyclerView;

import org.junit.After;
import org.junit.Before;

/**
 * Created by shiki60215 on 18-1-31.
 */
public class MultiAxisCardLayoutManagerTest extends AndroidTestCase {

    private MultiAxisCardLayoutManager manager;

    @Before
    public void setUp() throws Exception {
        final Context context = getContext();
        MultiAxisCardRecyclerView recyclerView = new MultiAxisCardRecyclerView(context);
        manager = new MultiAxisCardLayoutManager(recyclerView);
    }

    @After
    public void tearDown() throws Exception {
        manager = null;
    }

}