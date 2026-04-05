package com.flocklab.html;

import com.flocklab.config.PlatformHints;

public class HtmlPlatformHints implements PlatformHints {

    @Override
    public boolean isTouchDevice() {
        return detectTouch();
    }

    @Override
    public int getLogicalWidth() {
        return getClientWidth();
    }

    @Override
    public int getLogicalHeight() {
        return getClientHeight();
    }

    // JSNI to check touch capability robustly
    private static native boolean detectTouch() /*-{
        return (navigator.maxTouchPoints > 0)
            || ('ontouchstart' in $wnd)
            || ($wnd.matchMedia && $wnd.matchMedia('(pointer: coarse)').matches);
    }-*/;

    private static native int getClientWidth() /*-{
        return $wnd.innerWidth;
    }-*/;

    private static native int getClientHeight() /*-{
        return $wnd.innerHeight;
    }-*/;
}
