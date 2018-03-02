package com.james602152002.multiaxiscardlayoutmanager.viewholder;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by shiki60215 on 18-2-5.
 */

public class HorizontalCardViewHolder extends BaseCardViewHolder {


    public HorizontalCardViewHolder(View itemView) {
        super(itemView);
        Resources resources = itemView.getResources();
        itemView.setLayoutParams(new RecyclerView.LayoutParams((int) (resources.getDisplayMetrics().widthPixels * .5f), dp2px(resources, 100)));
    }

    private int dp2px(Resources resources, float dpValue) {
        return (int) (0.5f + dpValue * resources.getDisplayMetrics().density);
    }

    @Override
    public void initView(int v_card_position, int h_card_position) {
    }
}
