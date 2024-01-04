package com.example.cameraxvideo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class FocusRecyclerView extends RecyclerView {
    public FocusRecyclerView(Context context) {
        super(context);
    }

    public FocusRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean result = super.dispatchKeyEvent(event);
        View focusView = this.findFocus();
        if (focusView == null) {
            return result;
        } else {

            int dy = 0;
            int dx = 0;
            if (getChildCount() > 0) {
                View firstView = this.getChildAt(0);
                dy = firstView.getHeight();
                dx = firstView.getWidth();
            }
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    return super.dispatchKeyEvent(event);
                }
                return true;
            } else {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        View rightView = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_RIGHT);
                        //Log.i(TAG, "rightView is null:" + (rightView == null));
                        if (rightView != null) {
                            rightView.requestFocus();
                        } else {
                            this.smoothScrollBy(dx, 0);
                        }
                        return true;
                    }
                    case KeyEvent.KEYCODE_DPAD_LEFT -> {
                        View leftView = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_LEFT);
                        //Log.i(TAG, "leftView is null:" + (leftView == null));
                        if (leftView != null) {
                            leftView.requestFocus();
                        } else {
                            this.smoothScrollBy(-dx, 0);
                        }
                        return true;
                    }
                    case KeyEvent.KEYCODE_DPAD_DOWN -> {
                        View downView = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_DOWN);
                        //Log.i(TAG, " downView is null:" + (downView == null));
                        if (downView != null) {
                            downView.requestFocus();
                        } else {
                            this.smoothScrollBy(0, dy);
                        }
                        return true;
                    }
                    case KeyEvent.KEYCODE_DPAD_UP -> {
                        View upView = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_UP);
                        //Log.i(TAG, "upView is null:" + (upView == null));
                        if (event.getAction() != KeyEvent.ACTION_UP) {
                            if (upView != null) {
                                upView.requestFocus();
                            } else {
                                this.smoothScrollBy(0, -dy);
                            }

                        }
                        return true;
                    }
                }
            }

        }
        return result;
    }
}


