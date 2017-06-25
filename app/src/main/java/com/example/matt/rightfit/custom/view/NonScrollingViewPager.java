package com.example.matt.rightfit.custom.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Bradley Wang on 2/13/2017.
 */
public class NonScrollingViewPager extends ViewPager {
    public NonScrollingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonScrollingViewPager(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }
}