package com.cmput301f21t26.habittracker;

import android.view.View;
import android.widget.ImageView;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Used to check if a given view has a drawable.
 * Ref: https://medium.com/@dbottillo/android-ui-test-espresso-matcher-for-imageview-1a28c832626f#.zem2ltpr7
 * Ref: https://stackoverflow.com/questions/38867613/espresso-testing-that-imageview-contains-a-drawable/38896816
 */
public class DrawableMatcher extends TypeSafeMatcher<View> {
    private final int expectedId;
    static final int EMPTY = -1;
    static final int ANY = -2;

    public DrawableMatcher(int resourceId) {
        super(View.class);
        this.expectedId = resourceId;
    }

    @Override
    protected boolean matchesSafely(View target) {
        ImageView imageView = (ImageView) target;
        if (expectedId == EMPTY){
            return imageView.getDrawable() == null;
        }
        if (expectedId == ANY){
            return imageView.getDrawable() != null;
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {

    }
}
