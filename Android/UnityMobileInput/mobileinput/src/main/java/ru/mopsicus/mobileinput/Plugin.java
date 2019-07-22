// ----------------------------------------------------------------------------
// The MIT License
// UnityMobileInput https://github.com/mopsicus/UnityMobileInput
// Copyright (c) 2018 Mopsicus <mail@mopsicus.ru>
// ----------------------------------------------------------------------------

package ru.mopsicus.mobileinput;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import ru.mopsicus.common.Common;
import com.unity3d.player.UnityPlayer;

import org.json.JSONException;
import org.json.JSONObject;


public class Plugin {

    public static String name = "mobileinput";

    public static String KEYBOARD_ACTION = "KEYBOARD_ACTION";
    public static Activity activity;
    public static RelativeLayout layout;
    public static Common common;
    private static ViewGroup group;
    private static KeyboardProvider keyboardProvider;
    private static KeyboardListener keyboardListener;

    // Get view recursive
    private static View getLeafView(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup)view;
            for (int i = 0; i < viewGroup.getChildCount(); ++i) {
                View result = getLeafView(viewGroup.getChildAt(i));
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
        else {
            return view;
        }
    }

    // Init plugin, create layout for MobileInputs
    public static void init() {
        common = new Common();
        activity = UnityPlayer.currentActivity;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (layout != null) {
                    group.removeView(layout);
                }
                ViewGroup rootView = (ViewGroup) activity.findViewById (android.R.id.content);
                View topMostView = getLeafView(rootView);
                group = (ViewGroup) topMostView.getParent();
                layout = new RelativeLayout(activity);
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                group.addView(layout, params);
                keyboardListener = new KeyboardListener();
                keyboardProvider = new KeyboardProvider(activity, group, keyboardListener);
            }
        });
    }

    // Destroy plugin, remove layout
    public static void destroy() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                keyboardProvider.disable();
                keyboardProvider = null;
                keyboardListener = null;
                if (layout != null) {
                    group.removeView(layout);
                }
            }
        });
    }

    // Send data to MobileInput
    public static void execute(final int id, final String data) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (id != -1) {
                    MobileInput.processMessage(id, data);
                } else {
                    try {
                        JSONObject json = new JSONObject(data);
                        String msg = json.getString("msg");
                        if (msg.equals("SET_CLIP_RECT")) {
                            int left = json.getInt("left");
                            int top = json.getInt("top");
                            int right = json.getInt("right");
                            int bottom = json.getInt("bottom");
                            setClipRect(left, top, right, bottom);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    public static Rect clipRect = new Rect();

    private static void setClipRect(int left, int top, int right, int bottom) {
        clipRect.left = left;
        clipRect.top = top;
        clipRect.right = right;
        clipRect.bottom = bottom;

        layout.setPadding(left, top, right, bottom);

        MobileInput.updatePositions();
    }

}
