package com.sqrock.smarthome.screens;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.sqrock.smarthome.MainActivity;
import com.sqrock.smarthome.data.Models.Automation;
import com.sqrock.smarthome.data.Models.Device;
import com.sqrock.smarthome.logic.Scheduler;
import com.sqrock.smarthome.ui.Ui;
import java.util.List;

/** Daily schedules that switch a device on or off at a set time (run when the simulated clock passes them). */
public class AutomationsScreen implements Screen {
    private final MainActivity a;
    private long deviceId = -1;
    private boolean turnOn = true;

    public AutomationsScreen(MainActivity a) { this.a = a; }

    @Override public String title() { return "Automations"; }
    @Override public boolean isTab() { return true; }

    @Override public View build() {
        LinearLayout col = Views.page(a);
        List<Automation> rules = a.db.automations(a.user.id);
        if (rules.isEmpty()) col.addView(Ui.muted(a, "No automations yet."));
        for (Automation r : rules) {
            LinearLayout c = Ui.card(a);
            c.addView(Ui.text(a, Scheduler.hhmm(r.minuteOfDay) + "  ·  " + (r.turnOn ? "Turn on " : "Turn off ") + r.deviceName, 15, r.enabled ? Ui.text() : Ui.muted(), true));
            if (r.name != null && !r.name.isEmpty()) c.addView(Ui.muted(a, r.name + " · every day"));
            LinearLayout b = Ui.hbox(a);
            b.addView(Ui.ghostButton(a, r.enabled ? "Pause" : "Resume", v -> { a.db.setAutomationEnabled(r.id, !r.enabled); a.refresh(); }));
            b.addView(Ui.ghostButton(a, "Delete", v -> Ui.confirm(a, "Delete automation", "Delete this rule?", () -> { a.db.deleteAutomation(r.id); a.refresh(); })));
            c.addView(b);
            col.addView(c);
        }

        col.addView(Ui.sectionTitle(a, "New automation"));
        List<Device> devices = a.db.devices(a.user.id, 0);
        if (devices.isEmpty()) { col.addView(Ui.muted(a, "Add a device first.")); return Ui.scroll(a, col); }
        if (deviceId == -1) deviceId = devices.get(0).id;
        col.addView(Ui.muted(a, "Device"));
        LinearLayout dev = Ui.hbox(a);
        for (Device d : devices) dev.addView(Ui.chip(a, d.type.icon + " " + d.name, d.id == deviceId, v -> { deviceId = d.id; a.refresh(); }));
        col.addView(Ui.hscroll(a, dev));
        col.addView(Ui.muted(a, "Action"));
        LinearLayout act = Ui.hbox(a);
        act.addView(Ui.chip(a, "Turn on / lock", turnOn, v -> { turnOn = true; a.refresh(); }));
        act.addView(Ui.chip(a, "Turn off / unlock", !turnOn, v -> { turnOn = false; a.refresh(); }));
        col.addView(act);
        EditText time = Ui.input(a, "Time, e.g. 07:30", InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
        EditText name = Ui.input(a, "Name (optional)", InputType.TYPE_CLASS_TEXT);
        col.addView(time);
        col.addView(name);
        col.addView(Ui.primaryButton(a, "Save automation", v -> {
            try { a.db.addAutomation(a.user.id, deviceId, name.getText().toString(), time.getText().toString(), turnOn); Ui.toast(a, "Automation saved"); a.refresh(); }
            catch (IllegalArgumentException e) { Ui.toast(a, e.getMessage()); }
        }));
        return Ui.scroll(a, col);
    }
}
