package ch.burci.docslock.models;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import ch.burci.docslock.R;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

/**
 * Created by shaobin on 2014/3/22.
 */
public class StatusBarExpansionLocker {
    private static WindowManager.LayoutParams localLayoutParams;
    private static View view;

    public static void lock(Activity activity) {
        if(localLayoutParams == null || view == null) {
            initStatusBarExansion(activity);
        }
        try {
            WindowManager manager = ((WindowManager) activity.getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE));
            manager.addView(view, localLayoutParams);
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

    public static void unlock(Activity activity) {
        if(view != null) {
            WindowManager manager = ((WindowManager) activity.getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE));
            manager.removeView(view);
        }
    }

    public static void initStatusBarExansion(Context context){
        Activity activity = (Activity)context;
        localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        localLayoutParams.gravity = Gravity.TOP;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|

                // this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |

                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        //http://stackoverflow.com/questions/1016896/get-screen-dimensions-in-pixels
        int resId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int result = 0;
        if (resId > 0) {
            result = activity.getResources().getDimensionPixelSize(resId);
        }

        localLayoutParams.height = result;

        localLayoutParams.format = PixelFormat.TRANSPARENT;

        view = new ViewGroup(context) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                Log.v("customViewGroup", "**********Intercepted");
                return false;
            }
        };
    }
}