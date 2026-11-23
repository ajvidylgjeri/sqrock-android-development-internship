package com.sqrock.freshcart.screens;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.sqrock.freshcart.MainActivity;
import com.sqrock.freshcart.data.Models.Address;
import com.sqrock.freshcart.data.Models.Product;
import com.sqrock.freshcart.ui.Ui;
import java.util.List;

/** Profile, addresses, wishlist, dark mode, admin entry and logout. */
public class ProfileScreen implements Screen {
    private final MainActivity a;

    public ProfileScreen(MainActivity a) { this.a = a; }

    @Override public String title() { return "Profile"; }
    @Override public boolean isTab() { return true; }

    @Override public View build() {
        LinearLayout col = Views.page(a);
        col.addView(Ui.text(a, a.user.name, 22, Ui.text(), true));
        col.addView(Ui.muted(a, a.user.email + (a.user.isAdmin() ? "  ·  Admin" : "")));

        col.addView(Ui.sectionTitle(a, "Edit profile"));
        EditText name = Ui.input(a, "Full name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        name.setText(a.user.name);
        EditText phone = Ui.input(a, "Phone", InputType.TYPE_CLASS_PHONE);
        phone.setText(a.user.phone);
        col.addView(name);
        col.addView(phone);
        col.addView(Ui.primaryButton(a, "Save changes", v -> {
            if (name.getText().toString().trim().length() < 2) { Ui.toast(a, "Enter your name"); return; }
            a.db.updateProfile(a.user.id, name.getText().toString().trim(), phone.getText().toString().trim());
            a.user = a.db.user(a.user.id);
            Ui.toast(a, "Profile updated");
            a.refresh();
        }));

        col.addView(Ui.sectionTitle(a, "Saved addresses"));
        List<Address> addrs = a.db.addresses(a.user.id);
        if (addrs.isEmpty()) col.addView(Ui.muted(a, "No addresses yet. Add one during checkout."));
        for (Address ad : addrs) {
            LinearLayout c = Ui.card(a);
            c.addView(Ui.text(a, ad.label, 15, Ui.text(), true));
            c.addView(Ui.muted(a, ad.line));
            c.addView(Ui.ghostButton(a, "Delete", v -> Ui.confirm(a, "Delete address", "Remove " + ad.label + "?", () -> { a.db.deleteAddress(ad.id); a.refresh(); })));
            col.addView(c);
        }

        col.addView(Ui.sectionTitle(a, "Wishlist"));
        List<Product> wish = a.db.wishlist(a.user.id);
        if (wish.isEmpty()) col.addView(Ui.muted(a, "Tap ♡ on a product to save it here."));
        for (Product p : wish) col.addView(Views.row(a, p));

        col.addView(Ui.sectionTitle(a, "Settings"));
        col.addView(Ui.button(a, Ui.dark ? "☀ Switch to light mode" : "🌙 Switch to dark mode", Ui.line(), Ui.text(), v -> a.setDark(!Ui.dark)));
        if (a.user.isAdmin()) col.addView(Ui.primaryButton(a, "Open admin panel", v -> a.show(new AdminScreen(a))));
        col.addView(Ui.button(a, "Log out", Ui.RED, android.graphics.Color.WHITE, v -> a.signOut()));
        return Ui.scroll(a, col);
    }
}
