package com.sqrock.smarthome.screens;

import android.view.View;

/** One page of the app. build() is called every time the page is shown or refreshed. */
public interface Screen {
    String title();
    View build();
    /** True for the four bottom-navigation tabs (the back arrow is hidden there). */
    default boolean isTab() { return false; }
}
