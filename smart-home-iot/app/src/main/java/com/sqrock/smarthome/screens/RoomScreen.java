package com.sqrock.smarthome.screens;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.sqrock.smarthome.MainActivity;
import com.sqrock.smarthome.data.Models.Device;
import com.sqrock.smarthome.data.Models.Room;
import com.sqrock.smarthome.logic.DeviceType;
import com.sqrock.smarthome.ui.Ui;
import java.util.List;

/** Devices of one room, with add-device, rename and delete-room. */
public class RoomScreen implements Screen {
    private final MainActivity a;
    private final long id;
    private DeviceType type = DeviceType.LIGHT;

    public RoomScreen(MainActivity a, long id) { this.a = a; this.id = id; }

    @Override public String title() { Room r = a.db.room(id); return r == null ? "Room" : r.name; }

    @Override public View build() {
        Room room = a.db.room(id);
        if (room == null) return Views.empty(a, "❓", "This room no longer exists.");
        LinearLayout col = Views.page(a);
        List<Device> devices = a.db.devices(a.user.id, id);
        if (devices.isEmpty()) col.addView(Ui.muted(a, "No devices in this room yet."));
        for (Device d : devices) col.addView(Views.deviceRow(a, d, false));

        col.addView(Ui.sectionTitle(a, "Add a device"));
        EditText name = Ui.input(a, "Device name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        col.addView(name);
        LinearLayout chips = Ui.hbox(a);
        for (DeviceType t : DeviceType.values()) chips.addView(Ui.chip(a, t.icon + " " + t.label, t == type, v -> { type = t; a.refresh(); }));
        col.addView(Ui.hscroll(a, chips));
        col.addView(Ui.primaryButton(a, "Add device", v -> {
            try { a.db.addDevice(id, name.getText().toString(), type); Ui.toast(a, "Device added"); a.refresh(); }
            catch (IllegalArgumentException e) { Ui.toast(a, e.getMessage()); }
        }));

        col.addView(Ui.sectionTitle(a, "Room settings"));
        EditText rename = Ui.input(a, "Room name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        rename.setText(room.name);
        col.addView(rename);
        col.addView(Ui.button(a, "Rename room", Ui.line(), Ui.text(), v -> {
            try { a.db.renameRoom(id, rename.getText().toString()); a.refresh(); }
            catch (IllegalArgumentException e) { Ui.toast(a, e.getMessage()); }
        }));
        col.addView(Ui.button(a, "Delete room", Ui.RED, android.graphics.Color.WHITE, v -> Ui.confirm(a, "Delete room",
                "Delete " + room.name + " with its " + devices.size() + " device(s) and their automations?", () -> { a.db.deleteRoom(id); a.setRoot(new RoomsScreen(a)); })));
        return Ui.scroll(a, col);
    }
}
