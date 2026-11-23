package com.sqrock.freshcart;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sqrock.freshcart.data.Db;
import com.sqrock.freshcart.data.Models.User;
import com.sqrock.freshcart.screens.*;
import com.sqrock.freshcart.ui.Ui;
import java.util.ArrayDeque;
import java.util.Deque;

/** Single activity that hosts every screen: top bar, content area and bottom navigation. */
public class MainActivity extends Activity {
    public Db db;
    public User user;
    public String coupon = "";                       // coupon currently applied in the cart

    private SharedPreferences prefs;
    private final Deque<Screen> stack = new ArrayDeque<>();
    private FrameLayout content;
    private LinearLayout topBar, bottomNav;
    private TextView titleView, backView, cartView;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = new Db(this);
        prefs = getSharedPreferences("freshcart", Context.MODE_PRIVATE);
        Ui.dark = prefs.getBoolean("dark", false);
        long uid = prefs.getLong("uid", 0);
        user = uid == 0 ? null : db.user(uid);
        buildChrome();
        if (user == null) setRoot(new AuthScreen(this)); else setRoot(new HomeScreen(this));
    }

    // ---------------- session ----------------
    public void signIn(User u) {
        user = u;
        coupon = "";
        prefs.edit().putLong("uid", u.id).apply();
        setRoot(new HomeScreen(this));
    }

    public void signOut() {
        user = null;
        prefs.edit().remove("uid").apply();
        setRoot(new AuthScreen(this));
    }

    public void setDark(boolean d) {
        Ui.dark = d;
        prefs.edit().putBoolean("dark", d).apply();
        buildChrome();
        setRoot(new ProfileScreen(this));
    }

    // ---------------- navigation ----------------
    public void show(Screen s) { stack.push(s); render(); }

    /** Clears the history and shows a tab or the login page. */
    public void setRoot(Screen s) { stack.clear(); stack.push(s); render(); }

    public void refresh() { render(); }

    public void back() {
        if (stack.size() > 1) { stack.pop(); render(); }
    }

    @Override public void onBackPressed() {
        if (stack.size() > 1) back();
        else if (stack.peek() != null && stack.peek().isTab() && !(stack.peek() instanceof HomeScreen)) setRoot(new HomeScreen(this));
        else super.onBackPressed();
    }

    public void hideKeyboard() {
        View v = getCurrentFocus();
        if (v != null) ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void render() {
        Screen s = stack.peek();
        if (s == null) return;
        hideKeyboard();
        boolean loggedIn = user != null && !(s instanceof AuthScreen);
        topBar.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        bottomNav.setVisibility(loggedIn && s.isTab() ? View.VISIBLE : View.GONE);
        titleView.setText(s.title());
        backView.setVisibility(s.isTab() ? View.GONE : View.VISIBLE);
        cartView.setText(loggedIn ? "🛒 " + db.cartCount(user.id) : "");
        content.removeAllViews();
        content.addView(s.build(), new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        paintNav(s);
    }

    private void buildChrome() {
        getWindow().setStatusBarColor(Ui.card());
        getWindow().setNavigationBarColor(Ui.card());
        int flags = Ui.dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
        LinearLayout root = Ui.vbox(this);
        root.setBackgroundColor(Ui.bg());

        topBar = Ui.hbox(this);
        topBar.setBackgroundColor(Ui.card());
        topBar.setPadding(Ui.dp(this, 8), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10));
        backView = Ui.text(this, "←", 22, Ui.text(), true);
        backView.setPadding(Ui.dp(this, 10), 0, Ui.dp(this, 14), 0);
        backView.setOnClickListener(v -> back());
        titleView = Ui.text(this, "", 19, Ui.text(), true);
        cartView = Ui.text(this, "", 15, Ui.primary(), true);
        cartView.setOnClickListener(v -> setRoot(new CartScreen(this)));
        topBar.addView(backView);
        topBar.addView(titleView, Ui.weight(1));
        topBar.addView(cartView);
        root.addView(topBar, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        bottomNav = Ui.hbox(this);
        bottomNav.setBackgroundColor(Ui.card());
        String[] labels = {"🏠\nHome", "🛒\nCart", "📦\nOrders", "👤\nProfile"};
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            TextView t = Ui.text(this, labels[i], 12, Ui.muted(), true);
            t.setGravity(Gravity.CENTER);
            t.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));
            t.setTag("nav" + i);
            t.setOnClickListener(v -> {
                switch (idx) {
                    case 0: setRoot(new HomeScreen(this)); break;
                    case 1: setRoot(new CartScreen(this)); break;
                    case 2: setRoot(new OrdersScreen(this)); break;
                    default: setRoot(new ProfileScreen(this));
                }
            });
            bottomNav.addView(t, Ui.weight(1));
        }
        root.addView(bottomNav, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(root);
    }

    private void paintNav(Screen s) {
        int active = s instanceof HomeScreen ? 0 : s instanceof CartScreen ? 1 : s instanceof OrdersScreen ? 2 : s instanceof ProfileScreen ? 3 : -1;
        for (int i = 0; i < 4; i++) {
            TextView t = (TextView) bottomNav.findViewWithTag("nav" + i);
            if (t != null) t.setTextColor(i == active ? Ui.primary() : Color.GRAY);
        }
    }
}
