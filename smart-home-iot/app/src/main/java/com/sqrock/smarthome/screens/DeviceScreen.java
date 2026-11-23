package com.sqrock.smarthome.screens;

import android.graphics.Color;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.sqrock.smarthome.MainActivity;
import com.sqrock.smarthome.data.Models.Device;
import com.sqrock.smarthome.logic.DeviceType;
import com.sqrock.smarthome.logic.Energy;
import com.sqrock.smarthome.ui.Ui;

/** Control one device: power, level (brightness / speed / temperature / volume), energy, rename, delete. */
public class DeviceScreen implements Screen {
    private final MainActivity a;
    private final long id;

    public DeviceScreen(MainActivity a, long id) { this.a = a; this.id = id; }

    @Override public String title() { Device d = a.db.device(id); return d == null ? "Device" : d.name; }

    @Override public View build() {
        Device d = a.db.device(id);
        if (d == null) return Views.empty(a, "❓", "This device no longer exists.");
        DeviceType t = d.type;
        LinearLayout col = Views.page(a);
        TextView icon = Ui.text(a, t.icon, 80, Ui.text(), false);
        icon.setGravity(Gravity.CENTER);
        col.addView(icon, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView st = Ui.text(a, d.stateText(), 22, d.on ? Ui.primary() : Ui.muted(), true);
        st.setGravity(Gravity.CENTER);
        col.addView(st, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView where = Ui.muted(a, t.label + " · " + d.roomName);
        where.setGravity(Gravity.CENTER);
        col.addView(where, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        col.addView(d.on
                ? Ui.button(a, t == DeviceType.LOCK ? "Unlock" : "Turn off", Ui.line(), Ui.text(), v -> { a.db.setPower(a.user.id, id, false, "manual"); a.refresh(); })
                : Ui.primaryButton(a, t == DeviceType.LOCK ? "Lock" : "Turn on", v -> { a.db.setPower(a.user.id, id, true, "manual"); a.refresh(); }));

        if (t.hasLevel()) {
            col.addView(Ui.sectionTitle(a, t.levelName + ": " + d.level + t.unit));
            SeekBar bar = new SeekBar(a);
            bar.setMax(t.max - t.min);
            bar.setProgress(d.level - t.min);
            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean user) {}
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) { a.db.setLevel(id, t.min + s.getProgress()); a.refresh(); }
            });
            col.addView(bar);
        }

        col.addView(Ui.sectionTitle(a, "Energy"));
        LinearLayout e = Ui.card(a);
        e.addView(Ui.body(a, "Power now: " + Math.round(d.powerNow()) + " W"));
        e.addView(Ui.body(a, "Used so far: " + Energy.formatKwh(d.whTotal) + " (" + Ui.money(Energy.cost(d.whTotal)) + ")"));
        e.addView(Ui.muted(a, "Rated power " + t.watts + " W"));
        col.addView(e);

        col.addView(Ui.sectionTitle(a, "Settings"));
        EditText name = Ui.input(a, "Device name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        name.setText(d.name);
        col.addView(name);
        col.addView(Ui.button(a, "Rename", Ui.line(), Ui.text(), v -> {
            try { a.db.renameDevice(id, name.getText().toString()); a.refresh(); }
            catch (IllegalArgumentException ex) { Ui.toast(a, ex.getMessage()); }
        }));
        col.addView(Ui.button(a, "Remove device", Ui.RED, Color.WHITE, v -> Ui.confirm(a, "Remove device", "Remove " + d.name + " and its automations?", () -> { a.db.deleteDevice(id); a.back(); })));
        return Ui.scroll(a, col);
    }
}
