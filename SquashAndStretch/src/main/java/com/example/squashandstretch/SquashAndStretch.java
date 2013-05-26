package com.example.squashandstretch;

import android.animation.*;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class SquashAndStretch extends Activity {

    private static final long BASE_DURATION = 300L;
    private static final Interpolator sAccelerator = new AccelerateInterpolator();
    private static final Interpolator sDecelerator = new DecelerateInterpolator();

    private float mAnimatorScale = 1f;
    private View mContainer;

    private void toggleAnimationSpeed() {
        mAnimatorScale = (mAnimatorScale == 1f) ? 4f : 1f;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squashandstretch);

        mContainer = findViewById(R.id.container);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.squash_and_stretch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_slow:
                toggleAnimationSpeed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onButtonClick(View view) {
        long animationDuration = (long) (BASE_DURATION * mAnimatorScale);

        // Scale around bottom/middle to simplify squash against the window bottom
        view.setPivotX(view.getWidth() / 2);
        view.setPivotY(view.getHeight());

        // Animate the button down, accelerating, while also stretching in Y and squashing...
        PropertyValuesHolder pvhTY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y,
                mContainer.getHeight() - view.getHeight());
        PropertyValuesHolder pvhSX = PropertyValuesHolder.ofFloat(View.SCALE_X, .7f);
        PropertyValuesHolder pvhSY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f);
        ObjectAnimator downAnim = ObjectAnimator.ofPropertyValuesHolder(
                view, pvhTY, pvhSX, pvhSY);
        downAnim.setInterpolator(sAccelerator);
        downAnim.setDuration((long) (animationDuration * 2));

        // Stretch in X, squash in Y, then reverse
        pvhSX = PropertyValuesHolder.ofFloat(View.SCALE_X, 2f);
        pvhSY = PropertyValuesHolder.ofFloat(View.SCALE_Y, .5f);
        ObjectAnimator stretchAnim =
                ObjectAnimator.ofPropertyValuesHolder(view, pvhSX, pvhSY);
        stretchAnim.setRepeatCount(1);
        stretchAnim.setRepeatMode(ValueAnimator.REVERSE);
        stretchAnim.setInterpolator(sDecelerator);
        stretchAnim.setDuration(animationDuration);

        // Animate back to the start
        pvhTY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0);
        pvhSX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f);
        pvhSY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f);
        ObjectAnimator upAnim = ObjectAnimator.ofPropertyValuesHolder(
                view, pvhTY, pvhSX, pvhSY);
        upAnim.setDuration((long) (animationDuration * 2));
        upAnim.setInterpolator(sDecelerator);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(downAnim, stretchAnim, upAnim);
        set.start();
    }
}
