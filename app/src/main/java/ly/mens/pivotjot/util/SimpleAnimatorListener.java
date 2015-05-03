package ly.mens.pivotjot.util;

import android.animation.Animator;

/**
 * Don't require all methods when only one is needed.
 * Created by mensly on 3/05/2015.
 */
public class SimpleAnimatorListener implements Animator.AnimatorListener {
    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {

    }

    @Override
    public void onAnimationCancel(Animator animation) {
        onAnimationEnd(animation);
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
