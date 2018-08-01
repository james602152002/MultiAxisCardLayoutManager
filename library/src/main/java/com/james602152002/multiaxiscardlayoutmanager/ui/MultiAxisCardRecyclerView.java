package com.james602152002.multiaxiscardlayoutmanager.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import com.james602152002.multiaxiscardlayoutmanager.MultiAxisCardLayoutManager;
import com.james602152002.multiaxiscardlayoutmanager.interfaces.ScrollAnimatorObserver;

/**
 * Created by shiki60215 on 18-3-6.
 */

public class MultiAxisCardRecyclerView extends RecyclerView {

    private float downX, downY, moveX, appbar_saved_offset;
    private boolean touching_horizontal_cards = false;
    private boolean sliding_horizontal_cards = false;
    private boolean scroll_vertical;
    private final short touchSlop;
    private MultiAxisCardLayoutManager layoutManager;
    private final short ANIM_DURATION = 300;
    private ObjectAnimator horizontal_scroll_animator;
    private VelocityTracker velocityTracker;

    public MultiAxisCardRecyclerView(Context context) {
        super(context);
        touchSlop = (short) ViewConfiguration.get(context).getScaledTouchSlop();
        init();
    }

    public MultiAxisCardRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        touchSlop = (short) ViewConfiguration.get(context).getScaledTouchSlop();
        init();
    }

    public MultiAxisCardRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        touchSlop = (short) ViewConfiguration.get(context).getScaledTouchSlop();
        init();
    }

    public void init() {
        layoutManager = new MultiAxisCardLayoutManager(this);
        setLayoutManager(layoutManager);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (horizontal_scroll_animator != null) {
                    horizontal_scroll_animator.cancel();
                    horizontal_scroll_animator = null;
                }
                sliding_horizontal_cards = false;
                scroll_vertical = false;
                downX = event.getX();
                moveX = event.getX();
                downY = event.getY();
                appbar_saved_offset = layoutManager.getAppBarVerticalOffset();
                touching_horizontal_cards = layoutManager.isTouchingHorizontalCard(downX, downY);
                initVelocityTracker(event);
                ViewCompat.setNestedScrollingEnabled(this, true);
                break;
            case MotionEvent.ACTION_MOVE:
                //if fling break the logic
                if (getScrollState() == SCROLL_STATE_DRAGGING && !atMostVertical()) {
                    break;
                }
                final float vertical_dx = Math.abs(event.getY() - downY + appbar_saved_offset - layoutManager.getAppBarVerticalOffset());
                final float horizontal_dx = Math.abs(event.getX() - downX);
                if (!sliding_horizontal_cards && vertical_dx > touchSlop && vertical_dx > horizontal_dx) {
                    scroll_vertical = true;
                }
                if (touching_horizontal_cards && !sliding_horizontal_cards && !scroll_vertical && horizontal_dx > touchSlop) {
                    sliding_horizontal_cards = true;
                }
                if (sliding_horizontal_cards) {
                    ViewCompat.setNestedScrollingEnabled(this, false);
                    layoutManager.scrollHorizontalBy((int) (moveX - event.getX()));
                    moveX = event.getX();
                }
                velocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                touching_horizontal_cards = false;
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                final float velocity = velocityTracker.getXVelocity();
                velocityTracker.clear();
                velocityTracker.recycle();
                velocityTracker = null;
                scrollHorizontalCards(event.getX(), velocity);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * bug fix
     * https://stackoverflow.com/questions/46452465/android-the-item-inside-recyclerview-cant-be-clicked-after-scroll
     **/
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Fix bug about scroll to top or bottom state cannot reset to idle.
                if (getScrollState() != SCROLL_STATE_IDLE && atMostVertical()) {
                    // stop scroll to enable child view to get the touch event
                    stopScroll();
//                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
                if (sliding_horizontal_cards) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(e);
    }

    public boolean isTouching_horizontal_cards() {
        return touching_horizontal_cards;
    }

    public boolean isSliding_horizontal_cards() {
        return sliding_horizontal_cards;
    }

    private void scrollHorizontalCards(float upX, float velocity) {
        if (!sliding_horizontal_cards)
            return;
        final float start_value = 0;
        final float end_value = 1;
        layoutManager.enableStartMeasureAnimatorDx();
        final int limit_width = layoutManager.getHorizontalCardLimit();
        final float abs_dx = Math.abs(downX - upX);
        if (Math.abs(velocity) > abs_dx && abs_dx < limit_width) {
            if (upX > downX) {
                layoutManager.setDirection(layoutManager.DIRECTION_LEFT);
            } else {
                layoutManager.setDirection(layoutManager.DIRECTION_RIGHT);
            }
        } else {
            layoutManager.setDirection(layoutManager.DIRECTION_ORIGIN);
        }
        horizontal_scroll_animator = ObjectAnimator.ofFloat(layoutManager, "AnimateCards", start_value, end_value);
        horizontal_scroll_animator.setDuration(ANIM_DURATION);
        horizontal_scroll_animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                horizontal_scroll_animator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        horizontal_scroll_animator.start();
    }

    private void initVelocityTracker(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
    }

    public boolean atMostVertical() {
        return !canScrollVertically(1) || !canScrollVertically(-1);
    }

    public void smoothScrollToPosition(int position, ScrollAnimatorObserver observer) {
        layoutManager.smoothScrollToPosition(position, observer);
    }
}
