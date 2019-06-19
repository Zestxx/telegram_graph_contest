package org.telegram.graphic_contest.util;

import org.telegram.graphic_contest.R;
import org.telegram.graphic_contest.data.ThemeData;

import java.util.HashMap;
import java.util.Map;

public class ThemeUtil {

    private Map<Integer, ThemeData> themes;
    private int currentThemeId = R.style.LightTheme;

    public ThemeUtil() {
        themes = initThemes();
    }


    public ThemeData setTheme(int themeId) {
        currentThemeId = themeId;
        return themes.get(themeId);
    }

    public int getCurrentThemeId() {
        return currentThemeId;
    }

    public ThemeData switchTheme() {
        if (currentThemeId == R.style.LightTheme) {
            currentThemeId = R.style.DarkTheme;
        } else {
            currentThemeId = R.style.LightTheme;
        }

        return themes.get(currentThemeId);
    }

    private Map<Integer, ThemeData> initThemes() {
        themes = new HashMap<>();
        final ThemeData lightTheme = new ThemeData(
                R.color.colorPrimaryDark,
                R.color.colorPrimary,
                R.color.white,
                R.color.black,
                R.color.color_range_selector_overlay
        );
        themes.put(R.style.LightTheme, lightTheme);

        final ThemeData nightTheme = new ThemeData(
                R.color.colorPrimaryDark_dark_theme,
                R.color.colorPrimary_dark_theme,
                R.color.colorPrimaryDark_dark_theme,
                R.color.white,
                R.color.color_range_selector_overlay_dark
        );
        themes.put(R.style.DarkTheme, nightTheme);

        return themes;
    }
}
