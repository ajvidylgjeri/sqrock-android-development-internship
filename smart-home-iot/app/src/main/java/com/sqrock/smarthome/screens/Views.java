package com.sqrock.smarthome.screens;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sqrock.smarthome.MainActivity;
import com.sqrock.smarthome.data.Models.Device;
import com.sqrock.smarthome.ui.Ui;

final class Views {
    private Views() {}

    static LinearLayout page(MainActivity a) {
        LinearLayout l = Ui.vbox(a);
        l.setPadding(Ui.dp(a, 14), Ui.dp(a, 10), Ui.dp(a, 14), Ui.dp(a, 20));
        return l;
    }

    static View empty(MainActivity a, String emoji, String message) {
        LinearLayout l = Ui.vbox(a);
        l.setPadding(Ui.dp(a, 24), Ui.dp(a, 48), Ui.dp(a, 24), Ui.dp(a, 24));
        TextView e = Ui.text(a, emoji, 52, Color.GRAY, false);
        e.setGravity(Gravity.CENTER);
        TextView m = Ui.muted(a, message);
        m.setGravity(Gravity.CENTER);
        l.addView(e, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        l.addView(m, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return l;
    }

    /** Card with icon, name, state and an on/off switch button. Tapping the card opens the device page. */
    static View deviceRow(MainActivity a, Device d, boolean showRoom) {
        LinearLayout c = Ui.card(a);
        LinearLayout r = Ui.hbox(a);
        r.setGravity(Gravity.CENTER_VERTICAL);
        TextView icon = Ui.text(a, d.type.icon, 28, Ui.text(), false);
        icon.setPadding(0, 0, Ui.dp(a, 12), 0);
        LinearLayout mid = Ui.vbox(a);
        mid.addView(Ui.text(a, d.name, 15, Ui.text(), true));
        mid.addView(Ui.muted(a, (showRoom ? d.roomName + " · " : "") + d.stateText() + (d.on && d.powerNow() > 0 ? " · " + Math.round(d.powerNow()) + " W" : "")));
        r.addView(icon);
        r.addView(mid, Ui.weight(1));
        r.addView(d.on
                ? Ui.button(a, d.type.onWord(), Ui.primary(), Color.WHITE, v -> { a.db.setPower(a.user.id, d.id, false, "manual"); a.refresh(); })
                : Ui.button(a, d.type.offWord(), Ui.line(), Ui.text(), v -> { a.db.setPower(a.user.id, d.id, true, "manual"); a.refresh(); }));
        c.addView(r);
        c.setOnClickListener(v -> a.show(new DeviceScreen(a, d.id)));
        return c;
    }
}
