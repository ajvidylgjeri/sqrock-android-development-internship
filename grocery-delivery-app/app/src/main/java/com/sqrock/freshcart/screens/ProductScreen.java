package com.sqrock.freshcart.screens;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sqrock.freshcart.MainActivity;
import com.sqrock.freshcart.data.Models.Product;
import com.sqrock.freshcart.ui.Ui;

/** Product detail with quantity picker and wishlist toggle. */
public class ProductScreen implements Screen {
    private final MainActivity a;
    private final long id;
    private int qty = 1;

    public ProductScreen(MainActivity a, long id) { this.a = a; this.id = id; }

    @Override public String title() { return "Product"; }

    @Override public View build() {
        Product p = a.db.product(id);
        if (p == null) return Views.empty(a, "❓", "This product no longer exists.");
        LinearLayout col = Views.page(a);
        TextView emoji = Ui.text(a, p.emoji, 96, Ui.text(), false);
        emoji.setGravity(Gravity.CENTER);
        col.addView(emoji, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        col.addView(Ui.text(a, p.name, 24, Ui.text(), true));
        col.addView(Ui.muted(a, p.category + "  ·  " + p.unit));
        col.addView(Views.priceLine(a, p, 22));
        if (p.discountPct > 0) col.addView(Ui.badge(a, p.discountPct + "% off today", Ui.RED));
        col.addView(Ui.spacer(a, 8));
        col.addView(Ui.body(a, p.description));
        col.addView(Ui.spacer(a, 8));
        col.addView(Ui.muted(a, p.stock > 0 ? (p.stock <= 10 ? "Only " + p.stock + " left" : "In stock") : "Out of stock"));

        boolean fav = a.db.isFavorite(a.user.id, p.id);
        col.addView(Ui.button(a, fav ? "♥ Saved to wishlist" : "♡ Add to wishlist", Ui.line(), Ui.text(), v -> {
            boolean now = a.db.toggleFavorite(a.user.id, p.id);
            Ui.toast(a, now ? "Added to wishlist" : "Removed from wishlist");
            a.refresh();
        }));

        if (p.stock > 0) {
            LinearLayout q = Ui.hbox(a);
            q.setGravity(Gravity.CENTER_VERTICAL);
            TextView n = Ui.text(a, String.valueOf(qty), 20, Ui.text(), true);
            n.setPadding(Ui.dp(a, 18), 0, Ui.dp(a, 18), 0);
            q.addView(Ui.button(a, "−", Ui.line(), Ui.text(), v -> { if (qty > 1) { qty--; a.refresh(); } }));
            q.addView(n);
            q.addView(Ui.button(a, "+", Ui.line(), Ui.text(), v -> { if (qty < p.stock) { qty++; a.refresh(); } }));
            col.addView(q);
            col.addView(Ui.primaryButton(a, "Add to cart · " + Ui.money(p.finalPrice() * qty), v -> {
                a.db.addToCart(a.user.id, p.id, qty);
                Ui.toast(a, "Added to cart");
                a.back();
            }));
        }
        return Ui.scroll(a, col);
    }
}
