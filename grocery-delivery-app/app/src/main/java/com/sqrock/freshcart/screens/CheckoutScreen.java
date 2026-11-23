package com.sqrock.freshcart.screens;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sqrock.freshcart.MainActivity;
import com.sqrock.freshcart.data.Models.Address;
import com.sqrock.freshcart.data.Models.CartLine;
import com.sqrock.freshcart.data.Models.Order;
import com.sqrock.freshcart.logic.Pricing;
import com.sqrock.freshcart.ui.Ui;
import java.util.List;

/** Address, payment method and order summary. Payment is simulated (cash or demo card). */
public class CheckoutScreen implements Screen {
    private final MainActivity a;
    private long addressId = -1;
    private String payment = "Cash on delivery";

    public CheckoutScreen(MainActivity a) { this.a = a; }

    @Override public String title() { return "Checkout"; }

    @Override public View build() {
        LinearLayout col = Views.page(a);
        List<Address> addrs = a.db.addresses(a.user.id);
        if (addressId == -1 && !addrs.isEmpty()) addressId = addrs.get(0).id;

        col.addView(Ui.sectionTitle(a, "Delivery address"));
        for (Address ad : addrs) {
            LinearLayout c = Ui.card(a);
            boolean sel = ad.id == addressId;
            c.addView(Ui.text(a, (sel ? "● " : "○ ") + ad.label, 15, sel ? Ui.primary() : Ui.text(), true));
            c.addView(Ui.muted(a, ad.line));
            c.setOnClickListener(v -> { addressId = ad.id; a.refresh(); });
            col.addView(c);
        }
        EditText label = Ui.input(a, "Label (Home, Work…)", InputType.TYPE_CLASS_TEXT);
        EditText line = Ui.input(a, "Street, number, city", InputType.TYPE_CLASS_TEXT);
        col.addView(Ui.muted(a, addrs.isEmpty() ? "Add your first address:" : "Or add a new one:"));
        col.addView(label);
        col.addView(line);
        col.addView(Ui.ghostButton(a, "Save address", v -> {
            if (line.getText().toString().trim().isEmpty()) { Ui.toast(a, "Enter the address"); return; }
            String l = label.getText().toString().trim();
            a.db.addAddress(a.user.id, l.isEmpty() ? "Home" : l, line.getText().toString().trim());
            addressId = -1;
            a.refresh();
        }));

        col.addView(Ui.sectionTitle(a, "Payment"));
        LinearLayout chips = Ui.hbox(a);
        for (String m : new String[]{"Cash on delivery", "Card (demo)"}) chips.addView(Ui.chip(a, m, m.equals(payment), v -> { payment = m; a.refresh(); }));
        col.addView(Ui.hscroll(a, chips));
        if (payment.startsWith("Card")) col.addView(Ui.muted(a, "Demo only: no real payment is processed and no card data is stored."));

        double sub = 0;
        for (CartLine l : a.db.cart(a.user.id)) sub += l.total();
        Pricing.Totals t = Pricing.totals(sub, a.coupon);
        col.addView(Ui.sectionTitle(a, "Summary"));
        LinearLayout s = Ui.card(a);
        s.addView(Ui.body(a, "Items: " + Ui.money(t.subtotal)));
        if (t.couponDiscount > 0) s.addView(Ui.body(a, "Coupon: -" + Ui.money(t.couponDiscount)));
        s.addView(Ui.body(a, "Delivery: " + (t.delivery == 0 ? "Free" : Ui.money(t.delivery))));
        s.addView(Ui.text(a, "Total: " + Ui.money(t.total), 18, Ui.primary(), true));
        col.addView(s);

        col.addView(Ui.primaryButton(a, "Place order", v -> {
            Address chosen = null;
            for (Address ad : addrs) if (ad.id == addressId) chosen = ad;
            try {
                Order o = a.db.placeOrder(a.user.id, chosen == null ? "" : chosen.label + " - " + chosen.line, payment, a.coupon);
                a.coupon = "";
                Ui.info(a, "Order confirmed 🎉", "Order #" + o.id + " for " + Ui.money(o.total) + " is on its way to being prepared.");
                a.setRoot(new OrdersScreen(a));
            } catch (IllegalArgumentException | IllegalStateException e) {
                Ui.info(a, "Cannot place order", e.getMessage());
            }
        }));
        return Ui.scroll(a, col);
    }
}
