package com.example.cameraxvideo.util;

import android.graphics.Color;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;

public class AnimationUtil {
    //setonChangeListener里放的动画函数
    public static void TVStyleFocusAnimation(View v, boolean hasFocus, float from, float to) { //recycleview里的动画没有用这个
        ScaleAnimation animation;
        if (hasFocus) {//当选中这个View时做一些你所需要的操作
            animation = new ScaleAnimation(from, to, from, to,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        } else {
            animation = new ScaleAnimation(to, from, to, from,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        }
        animation.setDuration(500);
        animation.setFillAfter(true);
        v.startAnimation(animation);
    }

    public static void ButtonTextColorChange(Button button,boolean hasFocus){
        if(hasFocus){
            button.setTextColor(Color.BLACK);
        }else{
            button.setTextColor(Color.WHITE);
        }
    }
}
