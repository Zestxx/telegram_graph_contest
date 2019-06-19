package org.telegram.graphic_contest.data;

public class ThemeData {

    private final int statusBarColor;
    private final int toolbarColor;
    private final int backgroundColor;
    private final int textColor;
    private final int rangeSelectorOverlayColor;

    public ThemeData(final int statusBarColor, final int toolbarColor, final int backgroundColor, final int textColor,
            final int rangeSelectorOverlayColor) {
        this.statusBarColor = statusBarColor;
        this.toolbarColor = toolbarColor;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.rangeSelectorOverlayColor = rangeSelectorOverlayColor;
    }

    public int getStatusBarColor() {
        return statusBarColor;
    }

    public int getToolbarColor() {
        return toolbarColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getRangeSelectorOverlayColor() {
        return rangeSelectorOverlayColor;
    }
}
