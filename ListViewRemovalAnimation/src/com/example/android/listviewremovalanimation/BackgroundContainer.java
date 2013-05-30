package com.example.android.listviewremovalanimation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class BackgroundContainer extends FrameLayout {

    boolean mShowing = false;
    ColorDrawable mBackgroundDrawable = new ColorDrawable(0xffefefef);
    Drawable mDropShadow, mDropShadowLower;
    int mOpenAreaTop, mOpenAreaBottom, mOpenAreaHeight;
    private static final int  SHADOW_SIZE = 15;

    public BackgroundContainer(Context context) {
        super(context);
        init();
    }

    public BackgroundContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BackgroundContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
//        mDropShadow = getContext().getResources().getDrawable(R.id.drop_bottom_3_11);
//        mDropShadowLower = getContext().getResources().getDrawable(R.id.drop_top_3_11);
        mDropShadow = new ColorDrawable(0xff000000);
        mDropShadowLower = new ColorDrawable(0xff000000);
    }

    public void showBackground(int top, int bottom) {
        setWillNotDraw(false);
        mOpenAreaTop = top;
        mOpenAreaHeight = bottom;
        mShowing = true;
    }

    public void hideBackground() {
        setWillNotDraw(true);
        mShowing = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mDropShadow.setBounds(0, 0, getWidth(), SHADOW_SIZE);
        mDropShadowLower.setBounds(0, 0, getWidth(), SHADOW_SIZE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mShowing) {
            canvas.save();
            canvas.translate(0, mOpenAreaTop);
            canvas.clipRect(0, 0, getWidth(), mOpenAreaHeight);
            canvas.drawColor(0xffdfdfdf);
            mDropShadow.draw(canvas);
            canvas.translate(0, mOpenAreaHeight - SHADOW_SIZE);
            mDropShadowLower.draw(canvas);
            canvas.restore();
        }
    }

}
