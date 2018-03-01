package com.james602152002.multiaxiscardlayoutmanager.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.james602152002.multiaxiscardlayoutmanager.viewholder.BaseCardViewHolder;
import com.james602152002.multiaxiscardlayoutmanager.viewholder.HorizontalCardViewHolder;
import com.james602152002.multiaxiscardlayoutmanager.viewholder.VerticalCardViewHolder;

import java.util.HashMap;
import java.util.List;

/**
 * Created by shiki60215 on 18-2-5.
 */

public class MultiAxisCardAdapter extends RecyclerView.Adapter<BaseCardViewHolder> {

    protected final LayoutInflater inflater;
    private SparseArray<Object> items;
    protected final short TYPE_VERTICAL = 0;
    protected final short TYPE_HORIZONTAL = 1;
    private final SparseArray<Boolean> horizontal_position_list = new SparseArray<>();
    //If you wanna find value of index by sparse array, it will have bug. Because comparison logic use == not equal.
    private final HashMap<Integer, Integer> first_horizontal_position_list = new HashMap<>();
    private final SparseArray<Integer> horizontal_cards_next_vertical_index = new SparseArray<>();
    private final SparseArray<Integer> horizontal_leftmost_card_index = new SparseArray<>();
    protected final int vertical_view_id;
    protected final int horizontal_view_id;
    private int count = 0;

    public MultiAxisCardAdapter(Context context, SparseArray<Object> items, int vertical_view_id, int horizontal_view_id) {
        inflater = LayoutInflater.from(context);
        this.items = items;
        this.vertical_view_id = vertical_view_id;
        this.horizontal_view_id = horizontal_view_id;
        if (items != null) {
            int count = 0;

            for (int i = 0; i < items.size(); i++) {
                Object item = items.get(i);
                if (item != null) {
                    if (item instanceof List) {
                        final int saved_count = count;
                        first_horizontal_position_list.put(first_horizontal_position_list.size(), count);
                        count += ((List) items.get(i)).size();
                        for (int position = saved_count; position < count; position++) {
                            horizontal_position_list.put(position, true);
                            horizontal_cards_next_vertical_index.put(position, count);
                            horizontal_leftmost_card_index.put(position, saved_count);
                        }
                    } else {
                        count++;
                    }
                }
            }
            this.count = count;
        }
    }

    public boolean isFirstHorizontalCard(int position) {
        return first_horizontal_position_list.containsValue(position);
    }

    public int getHorizontalCardNextVerticalIndex(int position) {
        return horizontal_cards_next_vertical_index.get(position);
    }

    public int[] getHorizontalCardsLeftmostRightMostBounds(int position) {
        int[] bounds = new int[2];
        bounds[0] = horizontal_leftmost_card_index.get(position);
        bounds[1] = horizontal_cards_next_vertical_index.get(position);
        return bounds;
    }

    @Override
    public BaseCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_VERTICAL:
                return new VerticalCardViewHolder(inflater.inflate(vertical_view_id, parent, false));
            case TYPE_HORIZONTAL:
                return new HorizontalCardViewHolder(inflater.inflate(horizontal_view_id, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseCardViewHolder holder, int position) {
        holder.initView(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (horizontal_position_list.get(position, false)) {
            return TYPE_HORIZONTAL;
        }
        return TYPE_VERTICAL;
    }

    @Override
    public int getItemCount() {
        return count;
    }

}
