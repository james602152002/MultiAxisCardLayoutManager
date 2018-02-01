package com.james602152002.multiaxiscardlayoutmanager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.test.AndroidTestCase;

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
        manager = new MultiAxisCardLayoutManager(context, null, 0, 0);
        manager = new MultiAxisCardLayoutManager(context, LinearLayoutManager.VERTICAL, true);
        manager = new MultiAxisCardLayoutManager(context);
    }

    @After
    public void tearDown() throws Exception {
        manager = null;
    }

}