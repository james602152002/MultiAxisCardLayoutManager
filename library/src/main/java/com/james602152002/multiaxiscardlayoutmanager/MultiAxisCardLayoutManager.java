package com.james602152002.multiaxiscardlayoutmanager;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.james602152002.multiaxiscardlayoutmanager.adapter.MultiAxisCardAdapter;
import com.james602152002.multiaxiscardlayoutmanager.ui.CardRecyclerView;
import com.james602152002.multiaxiscardlayoutmanager.viewholder.HorizontalCardViewHolder;

import java.lang.reflect.Field;

/**
 * Created by shiki60215 on 18-1-31.
 */

public class MultiAxisCardLayoutManager extends RecyclerView.LayoutManager {

    private int mVerticalOffset;//竖直偏移量 每次换行时，要根据这个offset判断
    private int mFirstVisiPos;//屏幕可见的第一个View的Position
    private int mLastVisiPos;//屏幕可见的最后一个View的Position

    private SparseArray<Rect> mItemRects;//key 是View的position，保存View的bounds 和 显示标志，
    private SparseArray<Rect> horizontalCardItemRects;
    private SparseArray<View> horizontalCards;

    private final CardRecyclerView recyclerView;
    private AppBarLayout appBarLayout;
    private int appBarVerticalOffset;
    private int appBarTotalScrollRange;
    private short appBarMaximumHeight;
    private RecyclerView.Recycler recycler;
    private int[] horizontal_cards_scroll_bounds;
    private MultiAxisCardAdapter mAdapter;
    private boolean init = false;
    private short layout_times = 0;

    public MultiAxisCardLayoutManager(@NonNull CardRecyclerView recyclerView) {
        setAutoMeasureEnabled(true);
        mItemRects = new SparseArray<>();
        horizontalCardItemRects = new SparseArray<>();
        horizontalCards = new SparseArray<>();
        this.recyclerView = recyclerView;
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

                private boolean init = false;
                private int savedVerticalOffset = 0;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (appBarMaximumHeight == 0)
                        appBarMaximumHeight = (short) appBarLayout.getHeight();
                    appBarTotalScrollRange = appBarLayout.getTotalScrollRange();
                    appBarVerticalOffset = appBarTotalScrollRange + verticalOffset;
                    if (recycler != null && init) {
                        fill(recycler, 0, savedVerticalOffset - verticalOffset);
                    }
                    savedVerticalOffset = verticalOffset;
                    init = true;
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
        if (getItemCount() == 0) {//没有Item，界面空着吧
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (getChildCount() == 0 && state.isPreLayout()) {//state.isPreLayout()是支持动画的
            return;
        }

        if (!init) {
            removeAndRecycleAllViews(recycler);
            if (layout_times > 0)
                init = true;
            layout_times++;
        }
        //onLayoutChildren方法在RecyclerView 初始化时 会执行两遍
//        detachAndScrapAttachedViews(recycler);
        if (mAdapter == null) {
            mAdapter = (MultiAxisCardAdapter) recyclerView.getAdapter();
            if (mAdapter != null)
                mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        if (recycler != null)
                            removeAndRecycleAllViews(recycler);
                        //初始化区域
                        mVerticalOffset = 0;
                        mFirstVisiPos = 0;
                        mLastVisiPos = getItemCount();

                        //重置child记录区域
                        mItemRects.clear();
//        horizontalCardItemRects.clear();
                        horizontalCards.clear();
                        Log.i("", "data changed !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ");
                    }
                });
        }

        Log.i("", "layout changed !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ");
        //初始化时调用 填充childView
        fill(recycler, state);
    }

    /**
     * 初始化时调用 填充childView
     *
     * @param recycler
     * @param state
     */
    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state) {
        fill(recycler, 0, 0);
    }

    /**
     * 填充childView的核心方法,应该先填充，再移动。
     * 在填充时，预先计算dy的在内，如果View越界，回收掉。
     * 一般情况是返回dy，如果出现View数量不足，则返回修正后的dy.
     *
     * @param recycler
     * @param dx       Horizontal Card View偏移量
     * @param dy       RecyclerView给我们的位移量,+,显示底端， -，显示头部  @return 修正以后真正的dy（可能剩余空间不够移动那么多了 所以return <|savedVerticalOffset|）
     */
    private int fill(RecyclerView.Recycler recycler, int dx, int dy) {
        int topOffset = getPaddingTop();
        int leftOffset;
        //回收越界子View
        if (getChildCount() > 0) {//滑动时进来的
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(child);

                if (dy > 0) {//需要回收当前屏幕，上越界的View
                    if (getDecoratedBottom(child) + appBarVerticalOffset - dy < topOffset) {
                        detachAndScrapView(child, recycler);
                        mFirstVisiPos++;
                        continue;
                    }
                } else if (dy < 0) {//回收当前屏幕，下越界的View
                    if (getDecoratedTop(child) + appBarVerticalOffset - dy > getHeight() - getPaddingBottom()) {
                        detachAndScrapView(child, recycler);
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
                if (viewHolder instanceof HorizontalCardViewHolder)
                    minPos = ((MultiAxisCardAdapter) recyclerView.getAdapter()).getHorizontalCardNextVerticalIndex(getPosition(lastView));
                else
                    minPos = getPosition(lastView) + 1;//从最后一个View+1开始吧
                topOffset = getDecoratedTop(lastView);
                lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(lastView));
            }
            //顺序addChildView
            leftOffset = getPaddingLeft() + dx;
            for (int i = minPos; i <= mLastVisiPos; i++) {
                //找recycler要一个childItemView,我们不管它是从scrap里取，还是从RecyclerViewPool里取，亦或是onCreateViewHolder里拿。
                View child = recycler.getViewForPosition(i);
                //layout child when view is in visible position
                RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(child);

                //改变top  left  lineHeight
                if (viewHolder instanceof HorizontalCardViewHolder) {
                    if (((MultiAxisCardAdapter) recyclerView.getAdapter()).isFirstHorizontalCard(i)) {
                        topOffset += lineMaxHeight;
                    }

                    leftOffset += lineMaxWidth;
                    lineMaxWidth = Math.max(lineMaxWidth, getDecoratedMeasurementHorizontal(child));
                    horizontalCards.put(i, child);

                    //horizontal child over right or left bounds need not layout child
                    if (leftOffset > getWidth() - getPaddingRight() || leftOffset + getDecoratedMeasuredWidth(child) < getPaddingLeft()) {
//                        layout_child_flag = false;
                    }
                } else {
                    lineMaxWidth = 0;
                    topOffset += lineMaxHeight;
                    leftOffset = getPaddingLeft() + dx;
                }

                //you need add child first to measure child
                addAndMeasureChild(child);

                lineMaxHeight = 0;
                //新起一行的时候要判断一下边界
                if (topOffset - dy > getHeight() - getPaddingBottom() - appBarVerticalOffset) {
                    //越界了 就回收
                    detachAndScrapView(child, recycler);
                    mLastVisiPos = i - 1;
                } else {
                    //保存Rect供逆序layout用
                    Rect rect = new Rect();
                    rect.left = leftOffset;
                    rect.top = topOffset + mVerticalOffset;
                    rect.right = leftOffset + getDecoratedMeasurementHorizontal(child);
                    rect.bottom = topOffset + getDecoratedMeasurementVertical(child) + mVerticalOffset;
                    if (viewHolder instanceof HorizontalCardViewHolder) {
                        horizontalCardItemRects.put(i, rect);
                    }
                    mItemRects.put(i, rect);
                    //改变 left  lineHeight
                    lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(child));
                    layoutDecoratedWithMargins(child, leftOffset, topOffset, rect.right, topOffset + getDecoratedMeasurementVertical(child));
                }
            }
            //添加完后，判断是否已经没有更多的ItemView，并且此时屏幕仍有空白，则需要修正dy
            View lastChild = getChildAt(getChildCount() - 1);
            if (getPosition(lastChild) == getItemCount() - 1) {
                int gap = getHeight() - getPaddingBottom() - getDecoratedBottom(lastChild);
                if (gap > 0) {
                    dy -= gap;
                }

            }

        } else {
            /**
             * ##  利用Rect保存子View边界
             正序排列时，保存每个子View的Rect，逆序时，直接拿出来layout。
             */
            int maxPos = getItemCount() - 1;
            mFirstVisiPos = 0;
            if (getChildCount() > 0) {
                View firstView = getChildAt(0);
                maxPos = getPosition(firstView) - 1;
            }
            for (int i = maxPos; i >= mFirstVisiPos; i--) {
                Rect rect = mItemRects.get(i);

                if (rect.bottom - mVerticalOffset - dy < getPaddingTop()) {
                    mFirstVisiPos = i + 1;
                    break;
                } else {
                    View child = recycler.getViewForPosition(i);
                    RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(child);

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
                    for (int position = horizontal_cards_scroll_bounds[0]; position < horizontal_cards_scroll_bounds[1]; position++) {
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
        //位移0、没有子View 当然不移动
        if (dy == 0 || getChildCount() == 0 || appBarVerticalOffset != 0) {
            return 0;
        }

        int realOffset = dy;//实际滑动的距离， 可能会在边界处被修复
        //边界修复代码
        if (mVerticalOffset + realOffset < 0) {//上边界
            realOffset = -mVerticalOffset;
        } else if (realOffset > 0) {//下边界
            //利用最后一个子View比较修正
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
                realOffset = fill(recycler, 0, realOffset);//先填充，再位移。}
            }
            mVerticalOffset += realOffset;//累加实际滑动距离
            offsetChildrenVertical(-realOffset);//滑动
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

    //模仿LLM Horizontal 源码

    /**
     * 获取某个childView在水平方向所占的空间
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
     * 获取某个childView在竖直方向所占的空间
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
}
