package com.james602152002.multiaxiscardlayoutmanager.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by shiki60215 on 18-2-5.
 */

public abstract  class BaseCardViewHolder extends RecyclerView.ViewHolder {
    public BaseCardViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void initView(int v_card_position, int h_card_position);
}
