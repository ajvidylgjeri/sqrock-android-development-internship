package com.sqrock.smarthome.screens;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sqrock.smarthome.MainActivity;
import com.sqrock.smarthome.data.Models.Device;
import com.sqrock.smarthome.data.Models.Room;
import com.sqrock.smarthome.logic.Energy;
import com.sqrock.smarthome.logic.Scheduler;
import com.sqrock.smarthome.ui.Ui;
import java.util.List;

/** Overview: simulated clock, energy meter, rooms and devices that are currently on. */
public class DashboardScreen implements Screen {
    private final MainActivity a;

    public DashboardScreen(MainActivity a) { this.a = a; }

    @Override public String title() { return "My home"; }
    @Override public boolean isTab() { return true; }

    @Override public View build() {
        long uid = a.user.id;
        LinearLayout col = Views.page(a);
        col.addView(Ui.text(a, "Hello, " + a.user.name.split(" ")[0] + " 👋", 20, Ui.text(), true));

        LinearLayout hero = Ui.vbox(a);
        hero.setBackground(Ui.rounded(a, Ui.primary(), 16, 0));
        hero.setPadding(Ui.dp(a, 16), Ui.dp(a, 14), Ui.dp(a, 16), Ui.dp(a, 14));
        hero.addView(Ui.text(a, "Using now", 13, Color.WHITE, false));
        hero.addView(Ui.text(a, Math.round(a.db.powerNow(uid)) + " W", 32, Color.WHITE, true));
        double wh = a.db.totalWh(uid);
        hero.addView(Ui.text(a, Energy.formatKwh(wh) + " used so far · est. " + Ui.money(Energy.cost(wh)), 13, Color.WHITE, false));
        col.addView(hero, Ui.margin(a, android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0, 8, 0, 4));

        col.addView(Ui.sectionTitle(a, "Simulated time · " + Scheduler.clock(a.db.clock(uid))));
        col.addView(Ui.muted(a, "Fast-forward the clock to meter energy and run your automations."));
        LinearLayout chips = Ui.hbox(a);
        int[] mins = {30, 60, 360, 1440};
        String[] names = {"+30 min", "+1 h", "+6 h", "+1 day"};
        for (int i = 0; i < mins.length; i++) {
            final int m = mins[i];
            chips.addView(Ui.chip(a, names[i], false, v -> {
                int fired = a.db.advance(uid, m);
                Ui.toast(a, fired == 0 ? "Time moved forward" : fired + " automation(s) ran");
                a.refresh();
            }));
        }
        col.addView(Ui.hscroll(a, chips));

        col.addView(Ui.sectionTitle(a, "Rooms"));
        List<Room> rooms = a.db.rooms(uid);
        LinearLayout row = Ui.hbox(a);
        for (Room r : rooms) {
            LinearLayout c = Ui.card(a);
            c.setLayoutParams(Ui.margin(a, Ui.dp(a, 130), android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 10, 4));
            TextView icon = Ui.text(a, r.icon, 30, Ui.text(), false);
            c.addView(icon);
            c.addView(Ui.text(a, r.name, 15, Ui.text(), true));
            c.addView(Ui.muted(a, r.devicesOn + " of " + r.devices + " on"));
            c.setOnClickListener(v -> a.show(new RoomScreen(a, r.id)));
            row.addView(c);
        }
        col.addView(Ui.hscroll(a, row));
        if (rooms.isEmpty()) col.addView(Ui.muted(a, "No rooms yet. Add one in the Rooms tab."));

        col.addView(Ui.sectionTitle(a, "Devices on now"));
        int n = 0;
        for (Device d : a.db.devices(uid, 0)) if (d.on) { col.addView(Views.deviceRow(a, d, true)); n++; }
        if (n == 0) col.addView(Ui.muted(a, "Everything is off."));
        return Ui.scroll(a, col);
    }
}
