package com.james602152002.multiaxiscardlayoutmanager.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.james602152002.multiaxiscardlayoutmanager.interfaces.ViewHolderCallBack;
import com.james602152002.multiaxiscardlayoutmanager.viewholder.BaseCardViewHolder;
import com.james602152002.multiaxiscardlayoutmanager.viewholder.HorizontalCardViewHolder;
import com.james602152002.multiaxiscardlayoutmanager.viewholder.VerticalCardViewHolder;

import java.util.List;

/**
 * Created by shiki60215 on 18-2-5.
 */

public class MultiAxisCardAdapter extends RecyclerView.Adapter<BaseCardViewHolder> {

    private final LayoutInflater inflater;
    private SparseArray<Object> items;
    private final short TYPE_VERTICAL = 0;
    private final short TYPE_HORIZONTAL = 1;
    private final SparseArray<Boolean> horizontal_position_list = new SparseArray<>();
    private final int vertical_view_id;
    private final int horizontal_view_id;
    private final ViewHolderCallBack callBack;

    public MultiAxisCardAdapter(Context context, SparseArray<Object> items, int vertical_view_id, int horizontal_view_id, ViewHolderCallBack callBack) {
        inflater = LayoutInflater.from(context);
        this.items = items;
        this.vertical_view_id = vertical_view_id;
        this.horizontal_view_id = horizontal_view_id;
        this.callBack = callBack;
    }

    @Override
    public BaseCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_VERTICAL:
                return new VerticalCardViewHolder(inflater.inflate(vertical_view_id, parent, false), callBack);
            case TYPE_HORIZONTAL:
                return new HorizontalCardViewHolder(inflater.inflate(horizontal_view_id, parent, false), callBack);
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
        horizontal_position_list.clear();
        if (items != null) {
            int count = 0;
            for (int i = 0; i < items.size(); i++) {
                Object item = items.get(i);
                if (item != null) {
                    if (item instanceof List) {
                        final int saved_count = count;
                        count += ((List) items.get(i)).size();
                        for (int position = saved_count; position < count; position++) {
                            horizontal_position_list.put(position, true);
                        }
                    } else {
                        count++;
                    }
                }
            }
            return count;
        }
        return 0;
    }


}