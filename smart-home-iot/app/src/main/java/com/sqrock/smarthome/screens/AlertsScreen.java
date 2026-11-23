package com.sqrock.smarthome.screens;

import android.view.View;
import android.widget.LinearLayout;
import com.sqrock.smarthome.MainActivity;
import com.sqrock.smarthome.data.Models.Alert;
import com.sqrock.smarthome.ui.Ui;
import java.util.List;

/** Notification centre: device changes and automation runs. */
public class AlertsScreen implements Screen {
    private final MainActivity a;
    private boolean marked;

    public AlertsScreen(MainActivity a) { this.a = a; }

    @Override public String title() { return "Notifications"; }
    @Override public boolean isTab() { return true; }

    @Override public View build() {
        List<Alert> alerts = a.db.alerts(a.user.id);
        if (alerts.isEmpty()) return Views.empty(a, "🔔", "No notifications yet.\nSwitch a device or fast-forward the clock.");
        LinearLayout col = Views.page(a);
        LinearLayout bar = Ui.hbox(a);
        bar.addView(Ui.ghostButton(a, "Mark all read", v -> { a.db.markAllRead(a.user.id); a.refresh(); }));
        bar.addView(Ui.ghostButton(a, "Clear all", v -> Ui.confirm(a, "Clear notifications", "Delete all notifications?", () -> { a.db.clearAlerts(a.user.id); a.refresh(); })));
        col.addView(bar);
        for (Alert n : alerts) {
            LinearLayout c = Ui.card(a);
            c.addView(Ui.text(a, (n.read ? "" : "● ") + n.message, 14, n.read ? Ui.muted() : Ui.text(), !n.read));
            c.addView(Ui.muted(a, n.time));
            col.addView(c);
        }
        return Ui.scroll(a, col);
    }
}
