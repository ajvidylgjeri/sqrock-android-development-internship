package com.sqrock.smarthome;

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
import com.sqrock.smarthome.data.Db;
import com.sqrock.smarthome.data.Models.User;
import com.sqrock.smarthome.logic.Scheduler;
import com.sqrock.smarthome.screens.*;
import com.sqrock.smarthome.ui.Ui;
import java.util.ArrayDeque;
import java.util.Deque;

/** Single activity hosting every screen: top bar, content and bottom navigation. */
public class MainActivity extends Activity {
    public Db db;
    public User user;

    private SharedPreferences prefs;
    private final Deque<Screen> stack = new ArrayDeque<>();
    private FrameLayout content;
    private LinearLayout topBar, bottomNav;
    private TextView titleView, backView, clockView;
    private static final String[] NAV = {"🏠\nHome", "🚪\nRooms", "⏰\nAutomate", "🔔\nAlerts", "👤\nProfile"};

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = new Db(this);
        prefs = getSharedPreferences("smarthome", Context.MODE_PRIVATE);
        Ui.dark = prefs.getBoolean("dark", false);
        long uid = prefs.getLong("uid", 0);
        user = uid == 0 ? null : db.user(uid);
        buildChrome();
        setRoot(user == null ? new AuthScreen(this) : new DashboardScreen(this));
    }

    public void signIn(User u) { user = u; prefs.edit().putLong("uid", u.id).apply(); setRoot(new DashboardScreen(this)); }

    public void signOut() { user = null; prefs.edit().remove("uid").apply(); setRoot(new AuthScreen(this)); }

    public void setDark(boolean d) {
        Ui.dark = d;
        prefs.edit().putBoolean("dark", d).apply();
        buildChrome();
        setRoot(new ProfileScreen(this));
    }

    public void show(Screen s) { stack.push(s); render(); }
    public void setRoot(Screen s) { stack.clear(); stack.push(s); render(); }
    public void refresh() { render(); }
    public void back() { if (stack.size() > 1) { stack.pop(); render(); } }

    @Override public void onBackPressed() {
        if (stack.size() > 1) back();
        else if (stack.peek() != null && !(stack.peek() instanceof DashboardScreen) && !(stack.peek() instanceof AuthScreen)) setRoot(new DashboardScreen(this));
        else super.onBackPressed();
    }

    private void hideKeyboard() {
        View v = getCurrentFocus();
        if (v != null) ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void render() {
        Screen s = stack.peek();
        if (s == null) return;
        hideKeyboard();
        boolean in = user != null && !(s instanceof AuthScreen);
        topBar.setVisibility(in ? View.VISIBLE : View.GONE);
        bottomNav.setVisibility(in && s.isTab() ? View.VISIBLE : View.GONE);
        titleView.setText(s.title());
        backView.setVisibility(s.isTab() ? View.GONE : View.VISIBLE);
        clockView.setText(in ? "🕒 " + Scheduler.clock(db.clock(user.id)) : "");
        content.removeAllViews();
        content.addView(s.build(), new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        int active = s instanceof DashboardScreen ? 0 : s instanceof RoomsScreen ? 1 : s instanceof AutomationsScreen ? 2 : s instanceof AlertsScreen ? 3 : s instanceof ProfileScreen ? 4 : -1;
        int unread = in ? db.unreadAlerts(user.id) : 0;
        for (int i = 0; i < NAV.length; i++) {
            TextView t = (TextView) bottomNav.findViewWithTag("nav" + i);
            if (t == null) continue;
            t.setText(i == 3 && unread > 0 ? "🔔 " + unread + "\nAlerts" : NAV[i]);
            t.setTextColor(i == active ? Ui.primary() : Color.GRAY);
        }
    }

    private void buildChrome() {
        getWindow().setStatusBarColor(Ui.card());
        getWindow().setNavigationBarColor(Ui.card());
        getWindow().getDecorView().setSystemUiVisibility(Ui.dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        LinearLayout root = Ui.vbox(this);
        root.setBackgroundColor(Ui.bg());

        topBar = Ui.hbox(this);
        topBar.setBackgroundColor(Ui.card());
        topBar.setPadding(Ui.dp(this, 8), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10));
        backView = Ui.text(this, "←", 22, Ui.text(), true);
        backView.setPadding(Ui.dp(this, 10), 0, Ui.dp(this, 14), 0);
        backView.setOnClickListener(v -> back());
        titleView = Ui.text(this, "", 19, Ui.text(), true);
        clockView = Ui.text(this, "", 12, Ui.muted(), true);
        topBar.addView(backView);
        topBar.addView(titleView, Ui.weight(1));
        topBar.addView(clockView);
        root.addView(topBar, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        bottomNav = Ui.hbox(this);
        bottomNav.setBackgroundColor(Ui.card());
        for (int i = 0; i < NAV.length; i++) {
            final int idx = i;
            TextView t = Ui.text(this, NAV[i], 11, Ui.muted(), true);
            t.setGravity(Gravity.CENTER);
            t.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));
            t.setTag("nav" + i);
            t.setOnClickListener(v -> {
                switch (idx) {
                    case 0: setRoot(new DashboardScreen(this)); break;
                    case 1: setRoot(new RoomsScreen(this)); break;
                    case 2: setRoot(new AutomationsScreen(this)); break;
                    case 3: setRoot(new AlertsScreen(this)); break;
                    default: setRoot(new ProfileScreen(this));
                }
            });
            bottomNav.addView(t, Ui.weight(1));
        }
        root.addView(bottomNav, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(root);
    }
}
