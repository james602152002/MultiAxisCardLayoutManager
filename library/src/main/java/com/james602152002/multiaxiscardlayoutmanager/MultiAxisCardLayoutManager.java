package com.james602152002.multiaxiscardlayoutmanager;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.james602152002.multiaxiscardlayoutmanager.adapter.MultiAxisCardAdapter;
import com.james602152002.multiaxiscardlayoutmanager.viewholder.HorizontalCardViewHolder;

import java.lang.reflect.Field;

/**
 * Created by shiki60215 on 18-1-31.
 */

public class MultiAxisCardLayoutManager extends RecyclerView.LayoutManager implements View.OnTouchListener {

    private int mVerticalOffset;//竖直偏移量 每次换行时，要根据这个offset判断
    private int mFirstVisiPos;//屏幕可见的第一个View的Position
    private int mLastVisiPos;//屏幕可见的最后一个View的Position

    private SparseArray<Rect> mItemRects;//key 是View的position，保存View的bounds 和 显示标志，
    private SparseArray<Rect> horizontalCardItemRects;
    private SparseArray<View> horizontalCards;

    private final RecyclerView recyclerView;
    private float downX, downY;
    private boolean touching_horizontal_cards = false;
    private boolean sliding_horizontal_cards = false;
    private boolean scrolling = false;
    private final short touchSlop;
    private Rect horizontal_card_rect;
    private AppBarLayout appBarLayout;
    private int appBarVerticalOffset;
    private int appBarTotalScrollRange;
    private RecyclerView.Recycler recycler;

    public MultiAxisCardLayoutManager(@NonNull RecyclerView recyclerView) {
        setAutoMeasureEnabled(true);
        mItemRects = new SparseArray<>();
        horizontalCardItemRects = new SparseArray<>();
        horizontalCards = new SparseArray<>();
        this.recyclerView = recyclerView;
        recyclerView.setOnTouchListener(this);
        touchSlop = (short) ViewConfiguration.get(recyclerView.getContext()).getScaledTouchSlop();
        findAppBarLayout();
    }

    private void findAppBarLayout() {
        ViewGroup parent = ((AppCompatActivity) recyclerView.getContext()).getWindow().getDecorView().findViewById(android.R.id.content);
        if (parent != null)
            findAppBarLayout(parent);
        if (appBarLayout != null) {
            try {
                Field field = RecyclerView.class.getDeclaredField("mRecycler");
                field.setAccessible(true);
                recycler = (RecyclerView.Recycler) field.get(recyclerView);
            } catch (Exception e) {
            }

            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

                private boolean init = false;
                private int savedVerticalOffset = 0;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    appBarTotalScrollRange = appBarLayout.getTotalScrollRange();
                    appBarVerticalOffset = appBarTotalScrollRange + verticalOffset;
                    if (recycler != null && init) {
                        fill(recycler, 0, savedVerticalOffset - verticalOffset);
                    }
                    savedVerticalOffset = verticalOffset;
                    init = true;
                    scrolling = true;
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
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {//没有Item，界面空着吧
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (getChildCount() == 0 && state.isPreLayout()) {//state.isPreLayout()是支持动画的
            return;
        }
        //onLayoutChildren方法在RecyclerView 初始化时 会执行两遍
        detachAndScrapAttachedViews(recycler);

        //初始化区域
        mVerticalOffset = 0;
        mFirstVisiPos = 0;
        mLastVisiPos = getItemCount();

        //重置child记录区域
        mItemRects.clear();
        horizontalCardItemRects.clear();
        horizontalCards.clear();


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
        int leftOffset = getPaddingLeft();
        //回收越界子View
        if (getChildCount() > 0) {//滑动时进来的
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(child);

                if (holder instanceof HorizontalCardViewHolder) {
                    if (dx > 0) {
                        if (child.getX() + getDecoratedMeasuredWidth(child) - getLeftDecorationWidth(child) - dx < leftOffset) {
//                            removeAndRecycleView(child, recycler);
                            detachAndScrapView(child, recycler);
                        }
                    } else if (dx < 0) {
                        if (child.getX() - dx > getWidth() - getPaddingRight()) {
//                            removeAndRecycleView(child, recycler);
                            detachAndScrapView(child, recycler);
                        }
                    }
                }

                if (dy > 0) {//需要回收当前屏幕，上越界的View
                    if (getDecoratedBottom(child) + appBarVerticalOffset - dy < topOffset) {
                        removeOverBoundsHorizontalCards(holder, child);
//                        removeAndRecycleView(child, recycler);
                        detachAndScrapView(child, recycler);
                        Log.i("", "child count ============= " + getChildCount());
                        mFirstVisiPos++;
                        continue;
                    }
                } else if (dy < 0) {//回收当前屏幕，下越界的View
                    if (getDecoratedTop(child) + appBarVerticalOffset - dy > getHeight() - getPaddingBottom()) {
                        removeOverBoundsHorizontalCards(holder, child);
//                        removeAndRecycleView(child, recycler);
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
//            Log.i("", "y > 0");
            int minPos = mFirstVisiPos;
            mLastVisiPos = getItemCount() - 1;
            if (getChildCount() > 0) {
                Log.i("", "child count > 0");
                View lastView = getChildAt(getChildCount() - 1);
                RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(lastView);
                if (viewHolder instanceof HorizontalCardViewHolder)
                    minPos = ((MultiAxisCardAdapter) recyclerView.getAdapter()).getHorizontalCardNextIndex(getPosition(lastView));
                else
                    minPos = getPosition(lastView) + 1;//从最后一个View+1开始吧
                topOffset = getDecoratedTop(lastView);
//                leftOffset = getDecoratedRight(lastView);
                lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(lastView));
            }
            Log.i("", "min position ================ " + minPos);
            //顺序addChildView
            leftOffset = getPaddingLeft() + dx;
            for (int i = minPos; i <= mLastVisiPos; i++) {
                //找recycler要一个childItemView,我们不管它是从scrap里取，还是从RecyclerViewPool里取，亦或是onCreateViewHolder里拿。
                View child = recycler.getViewForPosition(i);
                RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(child);
                addAndMeasureChild(child);

                //改变top  left  lineHeight
                if (viewHolder instanceof HorizontalCardViewHolder) {
                    if (((MultiAxisCardAdapter) recyclerView.getAdapter()).isFirstHorizontalCard(i)) {
                        topOffset += lineMaxHeight;
                    }

                    leftOffset += lineMaxWidth;
//                    leftOffset = leftOffset + getPaddingLeft() + dx + lineMaxWidth;
                    lineMaxWidth = Math.max(lineMaxWidth, getDecoratedMeasurementHorizontal(child));
                    horizontalCards.put(i, child);
                } else {
                    lineMaxWidth = 0;
                    topOffset += lineMaxHeight;
                    leftOffset = getPaddingLeft() + dx;
                }

                lineMaxHeight = 0;
//                if (i == 2) {
//                    Log.i("", "topOffset ============= " + topOffset);
//                    Log.i("", "measure ================ " + (topOffset - dy));
//                    Log.i("", "limit ============ " + (getHeight() - getPaddingBottom() - appBarVerticalOffset));
//                    Log.i("", "appBarVerticalOffset ==================== " + appBarVerticalOffset);
//                }
                //新起一行的时候要判断一下边界
                if (topOffset - dy > getHeight() - getPaddingBottom() - appBarVerticalOffset) {
                    //越界了 就回收

                    // need to remove horizontal map size
                    removeAndRecycleView(child, recycler);
                    mLastVisiPos = i - 1;
//                    Log.i("", "remove i ================== " + i);
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
                    if (viewHolder instanceof HorizontalCardViewHolder) {
                        child.setX(rect.left + getLeftDecorationWidth(child));
//                        Log.i("", "minPosition ========== " + minPos);
//                        Log.i("", "top ============== " + (topOffset - dy));
//                        Log.i("", "add i ================== " + i);
                    }
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
//                    Log.i("" ,"add i ================== " + i);
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

        if (dy == 0) {
            return dx;
        }

        return dy;
    }

    private void addAndMeasureChild(View child) {
        addView(child);
        measureChildWithMargins(child, 0, 0);
    }

    private void removeOverBoundsHorizontalCards(RecyclerView.ViewHolder holder, View child) {
        if (holder instanceof HorizontalCardViewHolder) {
            int index = horizontalCards.indexOfValue(child);
            if (index >= 0) {
                horizontalCards.removeAt(index);
            }
        }
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
//                    realOffset = -gap;
                    realOffset = 0;
                } else if (gap == 0) {
                    realOffset = 0;
                } else {
                    realOffset = Math.min(realOffset, -gap);
                }
            }
        }

        if (!sliding_horizontal_cards) {
            //If you have AppBarLayout, you need terminate fill method when AppBarLayout height is changing.
            if (appBarLayout == null || mVerticalOffset + realOffset > 0) {
                realOffset = fill(recycler, 0, realOffset);//先填充，再位移。}
            }
            mVerticalOffset += realOffset;//累加实际滑动距离
            offsetChildrenVertical(-realOffset);//滑动
        }
        scrolling = true;
        return realOffset;
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }


    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (dx == 0 || getChildCount() == 0) {
            return 0;
        }
        int realOffset = dx;

//        if (mHorizontalOffset + realOffset < 0) {
//            realOffset = -mHorizontalOffset;
//        } else if (realOffset > 0) {
//
//        }
        if (touching_horizontal_cards && scrolling) {
            fill(recycler, realOffset, 0);
            for (int i = 0; i < horizontalCards.size(); i++) {
                final int key_index = horizontalCards.keyAt(i);
                View child = horizontalCards.get(key_index);
                Rect childRect = horizontalCardItemRects.get(key_index);
                if (horizontal_card_rect != null && childRect != null && childRect.top <= horizontal_card_rect.top && childRect.bottom >= horizontal_card_rect.bottom) {
                    childRect.left = childRect.left - realOffset;
                    childRect.right = childRect.right - realOffset;
                    child.setX(childRect.left + getLeftDecorationWidth(child));
                }
            }
        }


        realOffset = 0;
        return realOffset;
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

    public int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                sliding_horizontal_cards = false;
                scrolling = false;
                downX = event.getX();
                downY = event.getY();
                touching_horizontal_cards = isTouchingHorizontalCard(downX, downY + mVerticalOffset);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!scrolling && touching_horizontal_cards && !sliding_horizontal_cards)
                    if (Math.abs(event.getX() - downX) > touchSlop) {
                        sliding_horizontal_cards = true;
                    }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                touching_horizontal_cards = false;
                horizontal_card_rect = null;
                break;
        }
        return false;
    }

    private boolean isTouchingHorizontalCard(float x, float y) {
        for (int i = 0; i < horizontalCardItemRects.size(); i++) {
            Rect rect = horizontalCardItemRects.get(horizontalCardItemRects.keyAt(i));
            if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) {
                horizontal_card_rect = rect;
                return true;
            }
        }
        return false;
    }
}
