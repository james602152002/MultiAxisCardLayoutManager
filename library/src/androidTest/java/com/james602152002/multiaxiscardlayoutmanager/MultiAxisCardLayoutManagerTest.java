package com.james602152002.multiaxiscardlayoutmanager;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnitRunner;
import android.util.SparseArray;

import com.james602152002.multiaxiscardlayoutmanager.adapter.MultiAxisCardAdapter;
import com.james602152002.multiaxiscardlayoutmanager.ui.MultiAxisCardRecyclerView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by shiki60215 on 18-1-31.
 */
public class MultiAxisCardLayoutManagerTest extends AndroidJUnitRunner {

    private MultiAxisCardLayoutManager manager;
    private MultiAxisCardRecyclerView recyclerView;

    @Before
    public void setUp() throws Exception {
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        recyclerView = new MultiAxisCardRecyclerView(context);
        manager = new MultiAxisCardLayoutManager(recyclerView);
        recyclerView.setLayoutManager(manager);
        SparseArray array = new SparseArray();
        recyclerView.setAdapter(new MultiAxisCardAdapter(context, array, R.layout.multiaxis_test_h_layout, R.layout.multiaxis_test_v_layout));
                for (int i = 0; i < 10; i++) {
            array.put(i, i);
        }
    }

    @Test
    public void testGenerateDefaultLayoutParams() {
        manager.generateDefaultLayoutParams();
    }

    @Test
    public void testOnLayoutChildren() {
//        RecyclerView.Recycler recycler = null;
//        RecyclerView.State state = null;
//        try {
//            Field field = MultiAxisCardRecyclerView.class.getSuperclass().getDeclaredField("mRecycler");
//            field.setAccessible(true);
//            recycler = (RecyclerView.Recycler) field.get(recyclerView);
//        } catch (Exception e) {
//
//        }
//        try {
//            Field field = MultiAxisCardRecyclerView.class.getSuperclass().getDeclaredField("mState");
//            field.setAccessible(true);
//            state = (RecyclerView.State) field.get(recyclerView);
//        } catch (Exception e) {
//
//        }
////        manager.addView(new View(InstrumentationRegistry.getInstrumentation().getContext()));
//        manager.generateDefaultLayoutParams();
//        manager.onLayoutChildren(recycler, state);
//        recyclerView.getAdapter().notifyDataSetChanged();
//        manager.onLayoutChildren(recycler, state);
    }

    @After
    public void tearDown() throws Exception {
        manager = null;
        recyclerView = null;
    }

}