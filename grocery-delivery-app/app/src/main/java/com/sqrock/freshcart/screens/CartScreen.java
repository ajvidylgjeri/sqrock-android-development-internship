package com.sqrock.freshcart.screens;

import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sqrock.freshcart.MainActivity;
import com.sqrock.freshcart.data.Models.CartLine;
import com.sqrock.freshcart.logic.Pricing;
import com.sqrock.freshcart.ui.Ui;
import java.util.List;

/** Cart tab: quantities, coupon and totals. */
public class CartScreen implements Screen {
    private final MainActivity a;

    public CartScreen(MainActivity a) { this.a = a; }

    @Override public String title() { return "My cart"; }
    @Override public boolean isTab() { return true; }

    @Override public View build() {
        List<CartLine> lines = a.db.cart(a.user.id);
        if (lines.isEmpty()) return Views.empty(a, "🛒", "Your cart is empty.\nBrowse the shop and add something fresh!");
        LinearLayout col = Views.page(a);
        double sub = 0;
        for (CartLine l : lines) {
            sub += l.total();
            LinearLayout c = Ui.card(a);
            LinearLayout r = Ui.hbox(a);
            r.setGravity(Gravity.CENTER_VERTICAL);
            TextView e = Ui.text(a, l.product.emoji, 30, Ui.text(), false);
            e.setPadding(0, 0, Ui.dp(a, 10), 0);
            LinearLayout mid = Ui.vbox(a);
            mid.addView(Ui.text(a, l.product.name, 15, Ui.text(), true));
            mid.addView(Ui.muted(a, Ui.money(l.product.finalPrice()) + " · " + l.product.unit));
            r.addView(e);
            r.addView(mid, Ui.weight(1));
            r.addView(Ui.button(a, "−", Ui.line(), Ui.text(), v -> { a.db.setCartQty(a.user.id, l.product.id, l.qty - 1); a.refresh(); }));
            TextView q = Ui.text(a, String.valueOf(l.qty), 16, Ui.text(), true);
            q.setPadding(Ui.dp(a, 10), 0, Ui.dp(a, 10), 0);
            r.addView(q);
            r.addView(Ui.button(a, "+", Ui.line(), Ui.text(), v -> {
                if (l.qty >= l.product.stock) Ui.toast(a, "Only " + l.product.stock + " in stock");
                else { a.db.setCartQty(a.user.id, l.product.id, l.qty + 1); a.refresh(); }
            }));
            c.addView(r);
            c.addView(Ui.text(a, "Line total " + Ui.money(l.total()), 13, Ui.primary(), true));
            col.addView(c);
        }

        col.addView(Ui.sectionTitle(a, "Coupon"));
        LinearLayout cr = Ui.hbox(a);
        EditText code = Ui.input(a, "FRESH10, WELCOME5, FREESHIP", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        code.setText(a.coupon);
        cr.addView(code, Ui.weight(1));
        cr.addView(Ui.primaryButton(a, "Apply", v -> { a.coupon = code.getText().toString().trim(); a.refresh(); }));
        col.addView(cr);

        Pricing.Totals t = Pricing.totals(sub, a.coupon);
        if (!a.coupon.isEmpty()) col.addView(Ui.text(a, t.couponMessage, 13, t.couponDiscount > 0 || t.couponMessage.contains("free") ? Ui.primary() : Ui.RED, false));

        LinearLayout sum = Ui.card(a);
        sum.addView(row("Subtotal", Ui.money(t.subtotal), false));
        if (t.couponDiscount > 0) sum.addView(row("Coupon", "-" + Ui.money(t.couponDiscount), false));
        sum.addView(row("Delivery", t.delivery == 0 ? "Free" : Ui.money(t.delivery), false));
        if (t.delivery > 0) sum.addView(Ui.muted(a, "Free delivery from " + Ui.money(Pricing.FREE_DELIVERY_FROM)));
        sum.addView(row("Total", Ui.money(t.total), true));
        col.addView(sum);
        col.addView(Ui.primaryButton(a, "Checkout · " + Ui.money(t.total), v -> a.show(new CheckoutScreen(a))));
        col.addView(Ui.ghostButton(a, "Clear cart", v -> Ui.confirm(a, "Clear cart", "Remove all items?", () -> {
            for (CartLine l : lines) a.db.setCartQty(a.user.id, l.product.id, 0);
            a.coupon = "";
            a.refresh();
        })));
        return Ui.scroll(a, col);
    }

    private View row(String k, String v, boolean bold) {
        LinearLayout r = Ui.hbox(a);
        r.addView(Ui.text(a, k, bold ? 17 : 14, Ui.text(), bold), Ui.weight(1));
        r.addView(Ui.text(a, v, bold ? 17 : 14, bold ? Ui.primary() : Ui.text(), bold));
        r.setPadding(0, Ui.dp(a, 3), 0, Ui.dp(a, 3));
        return r;
    }
}
