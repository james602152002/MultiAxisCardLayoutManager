package com.james602152002.multiaxiscardlayoutmanager.viewholder;

import android.view.View;

import com.james602152002.multiaxiscardlayoutmanager.interfaces.ViewHolderCallBack;

/**
 * Created by shiki60215 on 18-2-5.
 */

public class VerticalCardViewHolder extends BaseCardViewHolder {

    private final ViewHolderCallBack callBack;

    public VerticalCardViewHolder(View itemView, ViewHolderCallBack callBack) {
        super(itemView);
        this.callBack = callBack;
    }

    @Override
    public void initView(int position) {
        if (callBack != null) {
            callBack.verticalViewCallBack(position);
        }
    }
}
