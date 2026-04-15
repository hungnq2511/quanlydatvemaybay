package com.quanlydatvemaybay.ui;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UIConstants {
    public static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    public static final Color PRIMARY_DARK = new Color(21, 67, 96);
    public static final Color SECONDARY_COLOR = new Color(39, 174, 96);
    public static final Color DANGER_COLOR = new Color(192, 57, 43);
    public static final Color WARNING_COLOR = new Color(243, 156, 18);
    public static final Color BG_COLOR = new Color(245, 248, 250);
    public static final Color TABLE_HEADER_BG = new Color(52, 73, 94);
    public static final Color TABLE_ROW_ALT = new Color(236, 240, 241);
    public static final Color SIDEBAR_BG = new Color(44, 62, 80);
    public static final Color SIDEBAR_TEXT = Color.WHITE;
    public static final Color SIDEBAR_HOVER = new Color(52, 73, 94);
    public static final Color WHITE = Color.WHITE;

    private static final String UI_FONT = detectFont();

    private static String detectFont() {
        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : new String[]{"Segoe UI", "SF Pro Text", "Helvetica Neue", "Arial Unicode MS", "Arial"}) {
            if (available.contains(f)) return f;
        }
        return Font.DIALOG;
    }

    public static final Font TITLE_FONT  = new Font(UI_FONT, Font.BOLD,  20);
    public static final Font HEADER_FONT = new Font(UI_FONT, Font.BOLD,  14);
    public static final Font NORMAL_FONT = new Font(UI_FONT, Font.PLAIN, 13);
    public static final Font SMALL_FONT  = new Font(UI_FONT, Font.PLAIN, 12);
    public static final Font BUTTON_FONT = new Font(UI_FONT, Font.BOLD,  13);

    public static final int BUTTON_HEIGHT = 36;
    public static final int INPUT_HEIGHT  = 32;
}
