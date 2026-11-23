package com.sqrock.smarthome.screens;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.sqrock.smarthome.MainActivity;
import com.sqrock.smarthome.data.Db;
import com.sqrock.smarthome.data.Models.Room;
import com.sqrock.smarthome.ui.Ui;

/** List of rooms and a form to add a new one. */
public class RoomsScreen implements Screen {
    private final MainActivity a;
    private String icon = Db.ROOM_ICONS[0];

    public RoomsScreen(MainActivity a) { this.a = a; }

    @Override public String title() { return "Rooms"; }
    @Override public boolean isTab() { return true; }

    @Override public View build() {
        LinearLayout col = Views.page(a);
        for (Room r : a.db.rooms(a.user.id)) {
            LinearLayout c = Ui.card(a);
            c.addView(Ui.text(a, r.icon + "  " + r.name, 17, Ui.text(), true));
            c.addView(Ui.muted(a, r.devices + " devices · " + r.devicesOn + " on"));
            c.setOnClickListener(v -> a.show(new RoomScreen(a, r.id)));
            col.addView(c);
        }
        col.addView(Ui.sectionTitle(a, "Add a room"));
        EditText name = Ui.input(a, "Room name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        col.addView(name);
        LinearLayout chips = Ui.hbox(a);
        for (String i : Db.ROOM_ICONS) chips.addView(Ui.chip(a, i, i.equals(icon), v -> { icon = i; a.refresh(); }));
        col.addView(Ui.hscroll(a, chips));
        col.addView(Ui.primaryButton(a, "Add room", v -> {
            try { a.db.addRoom(a.user.id, name.getText().toString(), icon); Ui.toast(a, "Room added"); a.refresh(); }
            catch (IllegalArgumentException e) { Ui.toast(a, e.getMessage()); }
        }));
        return Ui.scroll(a, col);
    }
}
