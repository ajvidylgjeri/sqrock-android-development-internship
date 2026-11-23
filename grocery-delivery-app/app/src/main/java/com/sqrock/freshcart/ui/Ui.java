package com.sqrock.freshcart.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Tiny view factory: the whole UI is built in code, so the app needs no XML layouts, AppCompat or Material libraries. */
public final class Ui {
    private Ui() {}

    // palette (switches with dark mode)
    public static boolean dark = false;
    public static int primary() { return Color.parseColor("#2E9E4F"); }
    public static int bg() { return dark ? Color.parseColor("#101614") : Color.parseColor("#F4F7F5"); }
    public static int card() { return dark ? Color.parseColor("#1B2420") : Color.WHITE; }
    public static int text() { return dark ? Color.parseColor("#E9F0EC") : Color.parseColor("#1B2420"); }
    public static int muted() { return dark ? Color.parseColor("#9DB0A6") : Color.parseColor("#66756D"); }
    public static int line() { return dark ? Color.parseColor("#2B3832") : Color.parseColor("#DDE5E0"); }
    public static final int RED = Color.parseColor("#D64545"), ORANGE = Color.parseColor("#E8912D"), BLUE = Color.parseColor("#2F7BD8");

    public static int dp(Context c, float v) { return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics())); }

    public static GradientDrawable rounded(Context c, int color, float radiusDp, int strokeColor) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radiusDp));
        if (strokeColor != 0) g.setStroke(dp(c, 1), strokeColor);
        return g;
    }

    public static LinearLayout vbox(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static LinearLayout hbox(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    public static TextView text(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    public static TextView body(Context c, String s) { return text(c, s, 14, text(), false); }
    public static TextView muted(Context c, String s) { return text(c, s, 13, muted(), false); }
    public static TextView title(Context c, String s) { return text(c, s, 18, text(), true); }

    public static LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }

    public static LinearLayout.LayoutParams margin(Context c, int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(dp(c, l), dp(c, t), dp(c, r), dp(c, b));
        return p;
    }

    public static LinearLayout.LayoutParams weight(float w) { return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, w); }

    public static Button button(Context c, String label, int bgColor, int fg, View.OnClickListener click) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(fg);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setBackground(rounded(c, bgColor, 10, bgColor == Color.TRANSPARENT ? primary() : 0));
        b.setPadding(dp(c, 14), dp(c, 8), dp(c, 14), dp(c, 8));
        b.setMinHeight(0);
        b.setMinimumHeight(dp(c, 40));
        b.setOnClickListener(click);
        return b;
    }

    public static Button primaryButton(Context c, String label, View.OnClickListener click) { return button(c, label, primary(), Color.WHITE, click); }
    public static Button ghostButton(Context c, String label, View.OnClickListener click) { return button(c, label, Color.TRANSPARENT, primary(), click); }

    public static LinearLayout card(Context c) {
        LinearLayout l = vbox(c);
        l.setBackground(rounded(c, card(), 14, line()));
        l.setPadding(dp(c, 14), dp(c, 12), dp(c, 14), dp(c, 12));
        l.setLayoutParams(margin(c, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 10));
        return l;
    }

    public static TextView chip(Context c, String label, boolean selected, View.OnClickListener click) {
        TextView t = text(c, label, 13, selected ? Color.WHITE : text(), true);
        t.setBackground(rounded(c, selected ? primary() : card(), 20, selected ? 0 : line()));
        t.setPadding(dp(c, 14), dp(c, 8), dp(c, 14), dp(c, 8));
        t.setLayoutParams(margin(c, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 8, 0));
        t.setOnClickListener(click);
        return t;
    }

    public static EditText input(Context c, String hint, int inputType) {
        EditText e = new EditText(c);
        e.setHint(hint);
        e.setInputType(inputType);
        e.setSingleLine(inputType != (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE));
        e.setTextColor(text());
        e.setHintTextColor(muted());
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        e.setBackground(rounded(c, card(), 10, line()));
        e.setPadding(dp(c, 12), dp(c, 10), dp(c, 12), dp(c, 10));
        e.setLayoutParams(margin(c, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 4, 0, 8));
        return e;
    }

    public static ScrollView scroll(Context c, View child) {
        ScrollView s = new ScrollView(c);
        s.setFillViewport(true);
        s.addView(child);
        return s;
    }

    public static HorizontalScrollView hscroll(Context c, View child) {
        HorizontalScrollView s = new HorizontalScrollView(c);
        s.setHorizontalScrollBarEnabled(false);
        s.addView(child);
        return s;
    }

    public static View spacer(Context c, int heightDp) {
        View v = new View(c);
        v.setLayoutParams(lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(c, heightDp)));
        return v;
    }

    public static TextView sectionTitle(Context c, String s) {
        TextView t = title(c, s);
        t.setLayoutParams(margin(c, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 14, 0, 8));
        return t;
    }

    public static TextView badge(Context c, String s, int color) {
        TextView t = text(c, s, 12, Color.WHITE, true);
        t.setBackground(rounded(c, color, 8, 0));
        t.setPadding(dp(c, 8), dp(c, 3), dp(c, 8), dp(c, 3));
        return t;
    }

    public static void toast(Context c, String s) { Toast.makeText(c, s, Toast.LENGTH_SHORT).show(); }

    public static void confirm(Context c, String title, String message, Runnable yes) {
        new AlertDialog.Builder(c).setTitle(title).setMessage(message).setNegativeButton("No", null).setPositiveButton("Yes", (d, w) -> yes.run()).show();
    }

    public static void info(Context c, String title, String message) {
        new AlertDialog.Builder(c).setTitle(title).setMessage(message).setPositiveButton("OK", null).show();
    }

    public static String money(double v) { return String.format(java.util.Locale.US, "$%.2f", v); }
}
