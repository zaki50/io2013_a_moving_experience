package com.example.android.listviewremovalanimation;

import android.os.Bundle;
import android.app.Activity;
import android.view.*;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

public class ListViewRemovalAnimation extends Activity {

    final ArrayList<View> mCheckedViews = new ArrayList<View>();
    StableArrayAdapter mAdapter;
    ListView mListView;
    BackgroundContainer mBackgroundContainer;
    boolean mSwiping = false;
    final HashMap<View, Integer> mTopMap = new HashMap<View, Integer>();

    private static final int SWIPE_DURATION = 250;
    private static final int MOVE_DURATION = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_deletion);

        mBackgroundContainer = (BackgroundContainer) findViewById(R.id.listViewBackground);
        mListView = (ListView) findViewById(R.id.listview);
        final ArrayList<String> cheeseList = new ArrayList<String>();
        for (int i = 0; i < Cheeses.sCheeseStrings.length; i++) {
            cheeseList.add(Cheeses.sCheeseStrings[i]);
        }
        mAdapter = new StableArrayAdapter(this, R.layout.opaque_text_view, cheeseList,
                mTouchListener);
        mListView.setAdapter(mAdapter);
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        float mDownX;
        private int mSwipeSlop = -1;

        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            if (mSwipeSlop < 0) {
                mSwipeSlop = ViewConfiguration.get(ListViewRemovalAnimation.this).
                        getScaledTouchSlop();
            }
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mSwiping) {
                    return false;
                }
                mDownX = event.getX();
                break;
            case MotionEvent.ACTION_CANCEL:
                v.setAlpha(1f);
                v.setTranslationX(0f);
                break;
            case MotionEvent.ACTION_MOVE:
                {
                    float x = event.getX() + v.getTranslationX();
                    float deltaX = x - mDownX;
                    float deltaXAbs = Math.abs(deltaX);
                    if (!mSwiping) {
                        if (deltaXAbs > mSwipeSlop) {
                            mSwiping = true;
                            mListView.requestDisallowInterceptTouchEvent(true);
                            mBackgroundContainer.showBackground(v.getTop(), v.getHeight());
                        }
                    }
                    if (mSwiping) {
                        v.setTranslationX(x - mDownX);
                        v.setAlpha((1 - deltaXAbs / v.getWidth()));
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                {
                    // User let go - figure out whether to animate the view out, or back to...
                    if (mSwiping) {
                        float x = event.getX() + v.getTranslationX();
                        float deltaX = x - mDownX;
                        float deltaXAbs = Math.abs(deltaX);
                        float fractionCovered;
                        float endX;
                        float endAlpha;
                        final boolean remove;
                        if (deltaXAbs > v.getWidth() / 4) {
                            // Greater than a quarter of the width - animate it out
                            fractionCovered = deltaXAbs / v.getWidth();
                            endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                            endAlpha = 0f;
                            remove = true;
                        } else {
                            // Not far enough - animate it back
                            fractionCovered = 1 - (deltaXAbs / v.getWidth());
                            endX = 0f;
                            endAlpha = 1f;
                            remove = false;
                        }
                        // Animate position and alpha
                        long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
                        v.animate().setDuration(duration).
                                alpha(endAlpha).translationX(endX).
                                withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Restore animated values
                                        v.setAlpha(1f);
                                        v.setTranslationX(0f);
                                        if (remove) {
                                            // Delete the item from the adapter
                                            int position = mListView.getPositionForView(v);
                                            mAdapter.remove(mAdapter.getItem(position));
                                            // Animate everything else into place
                                            animateOtherViews(mListView, v);
                                        } else {
                                            mBackgroundContainer.hideBackground();
                                            mSwiping = false;
                                        }
                                    }
                                });

                    }
                }
                break;
            default:
                return false;
            }
            return true;
        }
    };

    /**
     * This method animates all other views in the ListView container (not including ignoreView)
     * into their final positions. It is called after ignoreView has been removed from the
     * adapter, but before layout has been run. The approach here is to figure out where
     * everything is now, then allow layout to run, then figure out where everything is a...
     * layout, and then run animations between all of those start/end positions.
     */
    private void animateOtherViews(final ListView listview, final View ignoreView) {
//        if (true) {
//            mSwiping = false;
//            mBackgroundContainer.hideBackground();
//            return;
//        }
        for (int i = 0; i < listview.getChildCount(); ++i) {
            View child = listview.getChildAt(i);
            if (child != ignoreView) {
                mTopMap.put(child, child.getTop());
                // Use transient state to avoid recycling during upcoming layout
                child.setHasTransientState(true);
            }
        }
        final ViewTreeObserver observer = listview.getViewTreeObserver();
        assert observer != null;
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                for (int i = 0; i < listview.getChildCount(); ++i) {
                    // memo: why I can see ignoreView here?

                    final View child = listview.getChildAt(i);
                    Integer startTop = mTopMap.get(child);
                    if (startTop != null) {
                        int top = child.getTop();
                        if (startTop != top) {
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            child.animate().setDuration(MOVE_DURATION).translationY(0);
                            if (firstAnimation) {
                                child.animate().withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        mBackgroundContainer.hideBackground();;
                                        mSwiping = false;
                                    }
                                });
                                firstAnimation = false;
                            }
                        }
                    } else {
                        // Animate new views along with the others. The catch is that they
                        // exist in the start state, so we must calculate their starting position
                        // based on neighboring views.
                        int top = child.getTop();
                        View prevChild;
                        if (i > 0) {
                            // entering from the bottom
                            prevChild = listview.getChildAt(i - 1);
                            int prevChildStartTop = mTopMap.get(prevChild);
                            startTop = prevChildStartTop + prevChild.getHeight() +
                                    listview.getDividerHeight();
                        } else {
                            // entering from the top
                            prevChild = listview.getChildAt(i + 1);
                            int prevChildStartTop = mTopMap.get(prevChild);
                            startTop = prevChildStartTop - listview.getDividerHeight() -
                                    child.getHeight();
                        }
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(MOVE_DURATION).translationY(0f);
                        if (firstAnimation) {
                            child.animate().withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    mBackgroundContainer.hideBackground();
                                    mSwiping = false;
                                }
                            });
                            firstAnimation = false;
                        }
                    }
                }
                for (View view : mTopMap.keySet()) {
                    view.setHasTransientState(false);
                }
                mTopMap.clear();
                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_view_removal_animation, menu);
        return true;
    }
    
}
