package com.james602152002.multiaxiscardlayoutmanager;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.james602152002.multiaxiscardlayoutmanager.adapter.MultiAxisCardAdapter;
import com.james602152002.multiaxiscardlayoutmanager.ui.CardRecyclerView;
import com.james602152002.multiaxiscardlayoutmanager.viewholder.BaseCardViewHolder;
import com.james602152002.multiaxiscardlayoutmanager.viewholder.HorizontalCardViewHolder;

import java.lang.reflect.Field;

/**
 * Created by shiki60215 on 18-1-31.
 */

public class MultiAxisCardLayoutManager extends RecyclerView.LayoutManager {

    private int mVerticalOffset;
    private int mFirstVisiPos;
    private int mLastVisiPos;

    private SparseArray<Rect> mItemRects;
    private SparseArray<Rect> horizontalCardItemRects;
    private SparseArray<View> horizontalCards;

    private final CardRecyclerView recyclerView;
    private AppBarLayout appBarLayout;
    private int appBarVerticalOffset;
    private int appBarTotalScrollRange;
    private short appBarMaximumHeight;
    private boolean app_bar_offset_init = false;
    private RecyclerView.Recycler recycler;
    private int[] horizontal_cards_scroll_bounds;
    private MultiAxisCardAdapter mAdapter;
    private boolean init = false;
    private short layout_times = 0;
    private boolean start_measure_animator_dx = true;
    private int animator_dest_x = 0;
    private int center_card_position = -1;
    //Max visible cards in window.
    private final int MAX_VIS_H_CARDS_IN_WINDOW = 2;

    public MultiAxisCardLayoutManager(@NonNull CardRecyclerView recyclerView) {
        mItemRects = new SparseArray<>();
        horizontalCardItemRects = new SparseArray<>();
        horizontalCards = new SparseArray<>();
        this.recyclerView = recyclerView;
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return true;
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        findAppBarLayout();
    }

    private void findAppBarLayout() {
        ViewGroup parent = ((AppCompatActivity) recyclerView.getContext()).getWindow().getDecorView().findViewById(android.R.id.content);
        if (parent != null)
            findAppBarLayout(parent);
        if (appBarLayout != null) {
            try {
                Field field = CardRecyclerView.class.getSuperclass().getDeclaredField("mRecycler");
                field.setAccessible(true);
                recycler = (RecyclerView.Recycler) field.get(recyclerView);
            } catch (Exception e) {

            }

            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

                private int savedVerticalOffset = 0;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (appBarMaximumHeight == 0)
                        appBarMaximumHeight = (short) appBarLayout.getHeight();
                    appBarTotalScrollRange = appBarLayout.getTotalScrollRange();
                    appBarVerticalOffset = appBarTotalScrollRange + verticalOffset;
                    if (recycler != null && app_bar_offset_init) {
                        fill(recycler, 0, savedVerticalOffset - verticalOffset);
                    }
                    savedVerticalOffset = verticalOffset;
                    app_bar_offset_init = true;
                }
            });
        }
    }

    private boolean findAppBarLayout(ViewGroup parent) {
        boolean has_app_bar_layout = false;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (view instanceof AppBarLayout) {
                appBarLayout = (AppBarLayout) view;
                has_app_bar_layout = true;
            } else if (view instanceof ViewGroup) {
                has_app_bar_layout = findAppBarLayout((ViewGroup) view);
            }
        }
        return has_app_bar_layout;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(final RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }

        if (!init) {
            removeAndRecycleAllViews(recycler);
            if (layout_times > 0)
                init = true;
            layout_times++;
        }
        //onLayoutChildren will execute twice
        if (mAdapter == null) {
            mAdapter = (MultiAxisCardAdapter) recyclerView.getAdapter();
            if (mAdapter != null)
                mAdapter.registerDefaultAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

                    @Override
                    public void onItemRangeChanged(int positionStart, int itemCount) {
                        super.onItemRangeChanged(positionStart, itemCount);
                        RecyclerView.AdapterDataObserver defaultAdapterDataObserver = mAdapter.getCustomizeAdapterDataObserver();
                        if (defaultAdapterDataObserver != null)
                            defaultAdapterDataObserver.onItemRangeChanged(positionStart, itemCount);
                        Log.i("", "1");
                    }

                    @Override
                    public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                        super.onItemRangeChanged(positionStart, itemCount, payload);
                        RecyclerView.AdapterDataObserver defaultAdapterDataObserver = mAdapter.getCustomizeAdapterDataObserver();
                        if (defaultAdapterDataObserver != null)
                            defaultAdapterDataObserver.onItemRangeChanged(positionStart, itemCount, payload);
                        Log.i("", "2");
                    }

                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);
                        RecyclerView.AdapterDataObserver defaultAdapterDataObserver = mAdapter.getCustomizeAdapterDataObserver();
                        if (defaultAdapterDataObserver != null)
                            defaultAdapterDataObserver.onItemRangeInserted(positionStart, itemCount);
                        Log.i("", "3");
                        mAdapter.reset();
//                        mLastVisiPos = getItemCount();
                        app_bar_offset_init = false;
                    }

                    @Override
                    public void onItemRangeRemoved(int positionStart, int itemCount) {
                        super.onItemRangeRemoved(positionStart, itemCount);
                        RecyclerView.AdapterDataObserver defaultAdapterDataObserver = mAdapter.getCustomizeAdapterDataObserver();
                        if (defaultAdapterDataObserver != null)
                            defaultAdapterDataObserver.onItemRangeRemoved(positionStart, itemCount);
                        Log.i("", "4");
                    }

                    @Override
                    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                        super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                        RecyclerView.AdapterDataObserver defaultAdapterDataObserver = mAdapter.getCustomizeAdapterDataObserver();
                        if (defaultAdapterDataObserver != null)
                            defaultAdapterDataObserver.onItemRangeMoved(fromPosition, toPosition, itemCount);
                        Log.i("", "5");
                    }

                    @Override
                    public void onChanged() {
                        super.onChanged();
                        RecyclerView.AdapterDataObserver defaultAdapterDataObserver = mAdapter.getCustomizeAdapterDataObserver();
                        if (defaultAdapterDataObserver != null)
                            defaultAdapterDataObserver.onChanged();
                        Log.i("", "6");
                        if (recycler != null)
                            removeAndRecycleAllViews(recycler);
                        //reset
                        mVerticalOffset = 0;
                        mFirstVisiPos = 0;
                        mLastVisiPos = getItemCount();

                        mItemRects.clear();
                        horizontalCardItemRects.clear();
                        horizontalCards.clear();
                        mAdapter.reset();
                    }
                });
        }
        fill(recycler);
    }

    /**
     * fill child view when init
     *
     * @param recycler
     */
    private void fill(RecyclerView.Recycler recycler) {
        fill(recycler, 0, 0);
    }

    /**
     * @param recycler
     * @param dx       Horizontal Card View dx
     * @param dy       RecyclerView dy
     */
    private int fill(RecyclerView.Recycler recycler, int dx, int dy) {
        int topOffset = getPaddingTop();
        int leftOffset;
        //Recycle Child Logic
        if (getChildCount() > 0) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (dy > 0) {//Recycle top child out of bounds.
                    if (getDecoratedBottom(child) + appBarVerticalOffset - dy < topOffset) {
                        detachAndScrapView(child, recycler);
//                        removeAndRecycleView(child, recycler);
                        mFirstVisiPos++;
                        continue;
                    }
                } else if (dy < 0) {//Recycle bottom child out of bounds.
                    if (getDecoratedTop(child) + appBarVerticalOffset - dy > getHeight() - getPaddingBottom()) {
                        detachAndScrapView(child, recycler);
//                        removeAndRecycleView(child, recycler);
                        mLastVisiPos--;
                        continue;
                    }
                }
            }
        }

        int lineMaxHeight = 0;
        int lineMaxWidth = 0;
        //fix appbar layout vertical offset of dy
        if (appBarVerticalOffset != 0)
            dy = 0;
        //layout child view
        if (dy >= 0) {
            int minPos = mFirstVisiPos;
            mLastVisiPos = getItemCount() - 1;
            if (getChildCount() > 0) {
                View lastView = getChildAt(getChildCount() - 1);
                RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(lastView);
                //Calculate your new line position
                if (viewHolder instanceof HorizontalCardViewHolder) {
                    minPos = ((MultiAxisCardAdapter) recyclerView.getAdapter()).getHorizontalCardNextVerticalIndex(mFirstVisiPos + getChildCount() - 1);
                }
                // If you use get lastView it will have bug when datasetchanged. Because of wrong position, you cannot calculate minPosition precisely.
                //minPos = ((MultiAxisCardAdapter) recyclerView.getAdapter()).getHorizontalCardNextVerticalIndex(getPosition(lastView));
                else {
                    minPos = getPosition(lastView) + 1;
                }
                topOffset = getDecoratedTop(lastView);
                lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(lastView));
            }
            //add child view in order
            leftOffset = getPaddingLeft() + dx;
            for (int i = minPos; i <= mLastVisiPos; i++) {
                //Get view from recycler to save your memory and cpu.
                View child = recycler.getViewForPosition(i);
                BaseCardViewHolder viewHolder = (BaseCardViewHolder) recyclerView.getChildViewHolder(child);
                //you need add child first to measure child
                Rect rect = mItemRects.get(i);
                if (rect == null) {
                    //add child view to measure it
                    addAndMeasureChild(child);
                    //change child left and lineHeight
                    if (viewHolder instanceof HorizontalCardViewHolder) {
                        if (((MultiAxisCardAdapter) recyclerView.getAdapter()).isFirstHorizontalCard(i)) {
                            topOffset += lineMaxHeight;
                        }

                        leftOffset += lineMaxWidth;
                        lineMaxWidth = Math.max(lineMaxWidth, getDecoratedMeasurementHorizontal(child));
                    } else {
                        lineMaxWidth = 0;
                        topOffset += lineMaxHeight;
                        leftOffset = getPaddingLeft() + dx;
                    }

                    lineMaxHeight = 0;
                } else {
                    //If you saved child rect you needn't measure child rect.
                    leftOffset = rect.left;
                    topOffset = rect.top - mVerticalOffset;
                }
                //scroll to new line and decide to add child
                if (topOffset - dy > getHeight() - getPaddingBottom() - appBarVerticalOffset) {
                    //recycle when out of bounds
                    if (rect == null) {
//                        removeView(child);
//                        removeAndRecycleView(child, recycler);
                        detachAndScrapView(child, recycler);
                    }
                    mLastVisiPos = i - 1;
                } else {
                    if (rect != null) {
                        addAndMeasureChild(child);
                    } else {
                        //Save rect for reverse order.
                        rect = new Rect();
                        rect.left = leftOffset;
                        rect.top = topOffset + mVerticalOffset;
                        rect.right = leftOffset + getDecoratedMeasurementHorizontal(child);
                        rect.bottom = topOffset + getDecoratedMeasurementVertical(child) + mVerticalOffset;
                        mItemRects.put(i, rect);
                    }

                    if (viewHolder instanceof HorizontalCardViewHolder) {
                        horizontalCardItemRects.put(i, rect);
                        horizontalCards.put(i, child);
                    }

                    //change child left and lineHeight
                    lineMaxHeight = Math.max(lineMaxHeight, rect.bottom - rect.top);
                    layoutDecoratedWithMargins(child, rect.left, rect.top - mVerticalOffset, rect.right, rect.bottom - mVerticalOffset);
                    child.setX(rect.left + getLeftDecorationWidth(child));
                    mAdapter.onBindViewHolder(viewHolder, i);
                }
            }
            //If you don't have more item view in bottom then fix it.
            View lastChild = getChildAt(getChildCount() - 1);
            if (lastChild != null && getPosition(lastChild) == getItemCount() - 1) {
                int gap = getHeight() - getPaddingBottom() - getDecoratedBottom(lastChild);
                if (gap > 0) {
                    dy -= gap;
                }

            }

        } else {
            //Reverse order to add child.
            int maxPos = getItemCount() - 1;
            mFirstVisiPos = 0;
            if (getChildCount() > 0) {
                View firstView = getChildAt(0);
                maxPos = recyclerView.getChildViewHolder(firstView).getAdapterPosition() - 1;
            }
            //when add first child in adapter you need not add any view at all.
            if (maxPos >= 0)
                for (int i = maxPos; i >= mFirstVisiPos; i--) {
                    Rect rect = mItemRects.get(i);
                    View child = recycler.getViewForPosition(i);
                    RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(child);
                    if (getDecoratedBottom(child) + appBarVerticalOffset - dy < getPaddingTop()) {
                        mFirstVisiPos = i + 1;
                        break;
                    } else {
                        addView(child, 0);
                        measureChildWithMargins(child, 0, 0);

                        layoutDecoratedWithMargins(child, rect.left, rect.top - mVerticalOffset, rect.right, rect.bottom - mVerticalOffset);
                        if (viewHolder instanceof HorizontalCardViewHolder) {
                            horizontalCards.put(i, child);
                            child.setX(rect.left + getLeftDecorationWidth(child));
                        }
                    }
                }
        }

        //when dy is equal to zero that means it probably in horizontal slide mode
        if (dy == 0) {
            //scrolling horizontal cards
            if (recyclerView.isSliding_horizontal_cards()) {
                if (horizontal_cards_scroll_bounds != null) {
                    //first and last horizontal cards are not allow scroll over layout padding.
                    final int first_child_position = horizontal_cards_scroll_bounds[0];
                    final int last_child_position = horizontal_cards_scroll_bounds[1];

                    //Don't scroll when card size is less than 1
                    if (last_child_position - first_child_position < MAX_VIS_H_CARDS_IN_WINDOW - 1) {
                        dx = 0;
                        return dx;
                    }

                    Rect first_child_rect = horizontalCardItemRects.get(first_child_position);
                    Rect last_child_rect = horizontalCardItemRects.get(last_child_position);
                    if (first_child_rect.left - dx > getPaddingLeft()) {
                        dx = first_child_rect.left - getPaddingLeft();
                    } else if (last_child_rect.right - dx < getWidth() + getPaddingRight()) {
                        dx = last_child_rect.right - getWidth() - getPaddingRight();
                    }
                    for (int position = first_child_position; position <= last_child_position; position++) {
                        View child = horizontalCards.get(position);
                        Rect childRect = horizontalCardItemRects.get(position);
                        childRect.left = childRect.left - dx;
                        childRect.right = childRect.right - dx;
                        child.setX(childRect.left + getLeftDecorationWidth(child));
                    }
                }
            }
            return dx;
        }

        return dy;
    }

    private void addAndMeasureChild(View child) {
        addView(child);
        measureChildWithMargins(child, 0, 0);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (dy == 0 || getChildCount() == 0 || appBarVerticalOffset != 0) {
            return 0;
        }

        int realOffset = dy;
        if (mVerticalOffset + realOffset < 0) {
            realOffset = -mVerticalOffset;
        } else if (realOffset > 0) {
            View lastChild = getChildAt(getChildCount() - 1);
            if (getPosition(lastChild) == getItemCount() - 1) {
                int gap = getHeight() - getPaddingBottom() - getDecoratedBottom(lastChild);
                if (gap > 0) {
                    realOffset = 0;
                } else if (gap == 0) {
                    realOffset = 0;
                } else {
                    realOffset = Math.min(realOffset, -gap);
                }
            }
        }

        if (!recyclerView.isSliding_horizontal_cards()) {
            //If you have AppBarLayout, you need terminate fill method when AppBarLayout height is changing.
            if (appBarLayout == null || mVerticalOffset + realOffset > 0) {
                //fill child first then scroll vertical.
                realOffset = fill(recycler, 0, realOffset);
            }
            mVerticalOffset += realOffset;//Calculate real vertical offset.
            offsetChildrenVertical(-realOffset);//scrolling
        }
        return realOffset;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }


    public void scrollHorizontalBy(int dx) {
        if (recycler != null && (dx == 0 || getChildCount() == 0)) {
            return;
        }
        int realOffset = dx;

        if (recyclerView.isTouching_horizontal_cards()) {
            fill(recycler, realOffset, 0);
        }
    }

    /**
     * Fetch width of child
     *
     * @param view
     * @return
     */
    public int getDecoratedMeasurementHorizontal(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredWidth(view) + params.leftMargin
                + params.rightMargin;
    }

    /**
     * Fetch height of child
     *
     * @param view
     * @return
     */
    public int getDecoratedMeasurementVertical(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredHeight(view) + params.topMargin
                + params.bottomMargin;
    }

    public boolean isTouchingHorizontalCard(float x, float y) {
        y = y + mVerticalOffset;
        for (int i = 0; i < horizontalCardItemRects.size(); i++) {
            int position = horizontalCardItemRects.keyAt(i);
            Rect rect = horizontalCardItemRects.get(position);
            if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) {
                horizontal_cards_scroll_bounds = ((MultiAxisCardAdapter) recyclerView.getAdapter()).getHorizontalCardsLeftmostRightMostBounds(position);
                return true;
            }
        }
        return false;
    }

    public int getAppBarVerticalOffset() {
        return appBarVerticalOffset;
    }

    private void setAnimateCards(float ratio) {
        if (horizontal_cards_scroll_bounds != null) {
            //first and last horizontal cards are not allow scroll over layout padding.
            final int first_child_position = horizontal_cards_scroll_bounds[0];
            final int last_child_position = horizontal_cards_scroll_bounds[1];
            if (last_child_position - first_child_position < MAX_VIS_H_CARDS_IN_WINDOW - 1)
                return;
            int card_count = 0;
            SparseArray<View> cards = new SparseArray<>();
            SparseArray<Rect> cardsRect = new SparseArray<>();


            for (int position = first_child_position; position <= last_child_position; position++) {
                final View child = horizontalCards.get(position);
                cards.put(card_count, child);
                final Rect childRect = horizontalCardItemRects.get(position);
                cardsRect.put(card_count, childRect);
                final int half_width = getWidth() >> 1;
                if (start_measure_animator_dx && center_card_position == -1 && childRect.left <= half_width && childRect.right >= half_width) {
                    center_card_position = card_count;
                }
                card_count++;
            }

            if (center_card_position == -1) {
                cards.clear();
                cardsRect.clear();
                return;
            }

            if (start_measure_animator_dx) {
                Rect cardRect = cardsRect.get(center_card_position);
                if (center_card_position == 0) {
                    //center card is equal to most left horizontal card
                    animator_dest_x = 0;
                } else if (center_card_position == card_count - 1) {
                    //center card is equal to most right horizontal card
                    animator_dest_x = getWidth() - getDecoratedMeasuredWidth(cards.get(center_card_position));
                } else {
                    animator_dest_x = (getWidth() - cardRect.width()) >> 1;
                }
                start_measure_animator_dx = false;
            }

            int dx = (int) ((animator_dest_x - cardsRect.get(center_card_position).left) * ratio);

            for (int position = 0; position < card_count; position++) {
                View card = cards.get(position);
                Rect cardRect = cardsRect.get(position);
                cardRect.left = cardRect.left + dx;
                cardRect.right = cardRect.right + dx;
                card.setX(cardRect.left + getLeftDecorationWidth(card));
            }

            cards.clear();
            cardsRect.clear();
        }
    }

    public void enableStartMeasureAnimatorDx() {
        start_measure_animator_dx = true;
        animator_dest_x = 0;
        center_card_position = -1;
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        removeAndRecycleAllViews(recycler);
    }
}
