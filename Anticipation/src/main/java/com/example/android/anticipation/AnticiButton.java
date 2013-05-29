package com.example.android.anticipation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.*;
import android.widget.Button;

public class AnticiButton extends Button {

    private static final LinearInterpolator sLinearInterpolator = new LinearInterpolator();
    private static final DecelerateInterpolator sDecelerateor = new DecelerateInterpolator();
    private static final AccelerateInterpolator sAccelerator = new AccelerateInterpolator();
    private static final OvershootInterpolator sOvershooter = new OvershootInterpolator();
    private static final DecelerateInterpolator sQuickDecelerator = new DecelerateInterpolator(100);

    private float mSkewX = 0;
    ObjectAnimator downAnim = null;
    boolean mOnLeft = true;

    public AnticiButton(Context context) {
        super(context);
        init();
    }

    public AnticiButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnticiButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOnTouchListener(mTouchListener);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                runClickAnim();
            }
        });
    }

    @Override
    public void draw(Canvas canvas) {
        if (mSkewX != 0) {
            canvas.translate(0, getHeight());
            canvas.skew(mSkewX, 0);
            canvas.translate(0, -getHeight());
        }
        super.draw(canvas);
    }

    private void runPressAnim() {
        downAnim = ObjectAnimator.ofFloat(this, "skewX", mOnLeft ? .5f : -.5f);
        downAnim.setDuration(2500);
        downAnim.setInterpolator(sDecelerateor);
        downAnim.start();
    }

    private void runClickAnim() {
        ObjectAnimator finishDownAnim = null;
        if (downAnim != null && downAnim.isRunning()) {
            // finish the skew animation quickly
            downAnim.cancel();
            finishDownAnim = ObjectAnimator.ofFloat(this, "skewX", mOnLeft ? .5f : -.5f);
            finishDownAnim.setDuration(150);
            finishDownAnim.setInterpolator(sQuickDecelerator);
        }

        ObjectAnimator moveAnim = ObjectAnimator.ofFloat(this,
                View.TRANSLATION_X, mOnLeft ? 400 : 0);
        moveAnim.setInterpolator(sLinearInterpolator);
        moveAnim.setDuration(150);

        ObjectAnimator skewAnim = ObjectAnimator.ofFloat(this, "skewX",
                mOnLeft ? -.5f : .5f);
        skewAnim.setInterpolator(sQuickDecelerator);
        skewAnim.setDuration(150);
        // and wobble it
        ObjectAnimator wobbleAnim = ObjectAnimator.ofFloat(this, "skewX", 0);
        wobbleAnim.setInterpolator(sOvershooter);
        wobbleAnim.setDuration(150);
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(moveAnim, skewAnim, wobbleAnim);
        if (finishDownAnim != null) {
            set.play(finishDownAnim).before(moveAnim);
        }
        set.start();
        mOnLeft = !mOnLeft;
    }

    private void runCancelAnim() {
        if (downAnim != null && downAnim.isRunning()) {
            downAnim.cancel();
            ObjectAnimator reverser = ObjectAnimator.ofFloat(this, "skewX", 0);
            reverser.setDuration(200);
            reverser.setInterpolator(sAccelerator);
            reverser.start();
            downAnim = null;

            // ここから下は映ってなかった

        }
    }
}