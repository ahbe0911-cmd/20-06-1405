package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Central font-selection helper for the Persian UI font feature.
 *
 * This class intentionally does not override Telegram's special-purpose fonts
 * (monospace, condensed, Merriweather, number font, italic assets, etc.).
 * Only the regular/medium UI family is eligible for replacement.
 */
public final class PersianFontManager {

    public static final String PREFS_NAME = "mainconfig";
    public static final String PREF_KEY = "persian_ui_font";

    public static final String FONT_DEFAULT = "default";
    public static final String FONT_VAZIRMATN = "vazirmatn";
    public static final String FONT_NAZANIN = "nazanin";
    public static final String FONT_TITR = "titr";

    public static final String ASSET_VAZIRMATN_REGULAR = "fonts/vazirmatn_regular.ttf";
    public static final String ASSET_VAZIRMATN_MEDIUM = "fonts/vazirmatn_medium.ttf";
    public static final String ASSET_VAZIRMATN_BOLD = "fonts/vazirmatn_bold.ttf";
    public static final String ASSET_NAZANIN = "fonts/far_nazanin.ttf";
    public static final String ASSET_TITR = "fonts/far_titr_bold.ttf";

    private static final Set<String> PROTECTED_ASSETS = new HashSet<>(Arrays.asList(
            AndroidUtilities.TYPEFACE_ROBOTO_MONO,
            AndroidUtilities.TYPEFACE_MERRIWEATHER_BOLD,
            "fonts/mw_bolditalic.ttf",
            "fonts/rcondensedbold.ttf",
            "fonts/ritalic.ttf",
            "fonts/num.otf"
    ));

    private PersianFontManager() {
    }

    public static String getSelectedFont() {
        Context context = ApplicationLoader.applicationContext;
        if (context == null) {
            return FONT_DEFAULT;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_KEY, FONT_DEFAULT);
    }

    public static void setSelectedFont(String fontId) {
        Context context = ApplicationLoader.applicationContext;
        if (context == null) {
            return;
        }
        if (!isKnownFont(fontId)) {
            fontId = FONT_DEFAULT;
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_KEY, fontId)
                .apply();
    }

    public static boolean isKnownFont(String fontId) {
        return FONT_DEFAULT.equals(fontId)
                || FONT_VAZIRMATN.equals(fontId)
                || FONT_NAZANIN.equals(fontId)
                || FONT_TITR.equals(fontId);
    }

    public static String getDisplayName(String fontId) {
        if (FONT_VAZIRMATN.equals(fontId)) {
            return "Vazirmatn";
        } else if (FONT_NAZANIN.equals(fontId)) {
            return "Far Nazanin";
        } else if (FONT_TITR.equals(fontId)) {
            return "Far Titr";
        }
        return "Telegram Default";
    }

    /**
     * Returns the asset path that should actually be loaded for the requested
     * Telegram typeface asset.
     */
    public static String resolveAssetPath(String requestedAssetPath) {
        if (TextUtils.isEmpty(requestedAssetPath) || PROTECTED_ASSETS.contains(requestedAssetPath)) {
            return requestedAssetPath;
        }

        String selected = getSelectedFont();
        if (FONT_DEFAULT.equals(selected)) {
            return requestedAssetPath;
        }

        boolean mediumOrBold = requestedAssetPath.contains("medium")
                || requestedAssetPath.contains("rbold")
                || requestedAssetPath.contains("extrabold");

        if (FONT_VAZIRMATN.equals(selected)) {
            return mediumOrBold ? ASSET_VAZIRMATN_MEDIUM : ASSET_VAZIRMATN_REGULAR;
        }
        if (FONT_NAZANIN.equals(selected)) {
            return ASSET_NAZANIN;
        }
        if (FONT_TITR.equals(selected)) {
            return ASSET_TITR;
        }
        return requestedAssetPath;
    }

    public static Typeface createTypeface(String requestedAssetPath) {
        String resolvedPath = resolveAssetPath(requestedAssetPath);
        try {
            return Typeface.createFromAsset(ApplicationLoader.applicationContext.getAssets(), resolvedPath);
        } catch (Throwable error) {
            if (!TextUtils.equals(resolvedPath, requestedAssetPath)) {
                try {
                    return Typeface.createFromAsset(ApplicationLoader.applicationContext.getAssets(), requestedAssetPath);
                } catch (Throwable ignored) {
                }
            }
            FileLog.e(error);
            return null;
        }
    }
}
