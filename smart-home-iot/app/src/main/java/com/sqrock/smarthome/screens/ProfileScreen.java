package com.sqrock.smarthome.screens;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.sqrock.smarthome.MainActivity;
import com.sqrock.smarthome.ui.Ui;

public class ProfileScreen implements Screen {
    private final MainActivity a;

    public ProfileScreen(MainActivity a) { this.a = a; }

    @Override public String title() { return "Profile"; }
    @Override public boolean isTab() { return true; }

    @Override public View build() {
        LinearLayout col = Views.page(a);
        col.addView(Ui.text(a, a.user.name, 22, Ui.text(), true));
        col.addView(Ui.muted(a, a.user.email));
        col.addView(Ui.sectionTitle(a, "Edit profile"));
        EditText name = Ui.input(a, "Full name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        name.setText(a.user.name);
        EditText phone = Ui.input(a, "Phone", InputType.TYPE_CLASS_PHONE);
        phone.setText(a.user.phone);
        col.addView(name);
        col.addView(phone);
        col.addView(Ui.primaryButton(a, "Save changes", v -> {
            try {
                a.db.updateProfile(a.user.id, name.getText().toString(), phone.getText().toString());
                a.user = a.db.user(a.user.id);
                Ui.toast(a, "Profile updated");
                a.refresh();
            } catch (IllegalArgumentException e) { Ui.toast(a, e.getMessage()); }
        }));
        col.addView(Ui.sectionTitle(a, "Settings"));
        col.addView(Ui.button(a, Ui.dark ? "☀ Switch to light mode" : "🌙 Switch to dark mode", Ui.line(), Ui.text(), v -> a.setDark(!Ui.dark)));
        col.addView(Ui.button(a, "Reset energy meters", Ui.line(), Ui.text(), v -> Ui.confirm(a, "Reset energy", "Set every device's energy counter back to 0?", () -> { a.db.resetEnergy(a.user.id); Ui.toast(a, "Energy counters reset"); })));
        col.addView(Ui.button(a, "Log out", Ui.RED, android.graphics.Color.WHITE, v -> a.signOut()));
        return Ui.scroll(a, col);
    }
}
