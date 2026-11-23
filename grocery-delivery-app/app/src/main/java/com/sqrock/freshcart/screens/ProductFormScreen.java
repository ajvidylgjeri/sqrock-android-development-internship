package com.sqrock.freshcart.screens;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.sqrock.freshcart.MainActivity;
import com.sqrock.freshcart.data.Models;
import com.sqrock.freshcart.data.Models.Product;
import com.sqrock.freshcart.ui.Ui;

/** Create or edit a product (admin). */
public class ProductFormScreen implements Screen {
    private final MainActivity a;
    private final long id;
    private String category;

    public ProductFormScreen(MainActivity a, long id) { this.a = a; this.id = id; }

    @Override public String title() { return id == 0 ? "New product" : "Edit product"; }

    @Override public View build() {
        Product p = id == 0 ? new Product() : a.db.product(id);
        if (p == null) return Views.empty(a, "❓", "Product not found.");
        if (category == null) category = p.category != null ? p.category : Models.CATEGORIES[0];
        LinearLayout col = Views.page(a);
        EditText name = field(col, "Name", p.name, InputType.TYPE_CLASS_TEXT);
        EditText emoji = field(col, "Emoji icon", p.emoji, InputType.TYPE_CLASS_TEXT);
        EditText unit = field(col, "Unit (e.g. 1 kg)", p.unit, InputType.TYPE_CLASS_TEXT);
        EditText desc = field(col, "Description", p.description, InputType.TYPE_CLASS_TEXT);
        EditText price = field(col, "Price", id == 0 ? "" : String.valueOf(p.price), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText disc = field(col, "Discount %", String.valueOf(p.discountPct), InputType.TYPE_CLASS_NUMBER);
        EditText stock = field(col, "Stock", String.valueOf(p.stock), InputType.TYPE_CLASS_NUMBER);
        col.addView(Ui.sectionTitle(a, "Category"));
        LinearLayout chips = Ui.hbox(a);
        for (String c : Models.CATEGORIES) chips.addView(Ui.chip(a, c, c.equals(category), v -> { category = c; a.refresh(); }));
        col.addView(Ui.hscroll(a, chips));
        final boolean[] feat = {p.featured}, best = {p.bestSeller};
        col.addView(Ui.chip(a, "Featured", feat[0], v -> { feat[0] = !feat[0]; ((android.widget.TextView) v).setText(feat[0] ? "Featured ✔" : "Featured"); }));
        col.addView(Ui.chip(a, "Best seller", best[0], v -> { best[0] = !best[0]; ((android.widget.TextView) v).setText(best[0] ? "Best seller ✔" : "Best seller"); }));
        col.addView(Ui.primaryButton(a, "Save product", v -> {
            try {
                Product n = id == 0 ? new Product() : p;
                n.name = name.getText().toString().trim();
                n.emoji = emoji.getText().toString().trim().isEmpty() ? "🛍️" : emoji.getText().toString().trim();
                n.unit = unit.getText().toString().trim();
                n.description = desc.getText().toString().trim();
                n.category = category;
                n.price = Double.parseDouble(price.getText().toString().trim());
                n.discountPct = Integer.parseInt(disc.getText().toString().trim().isEmpty() ? "0" : disc.getText().toString().trim());
                n.stock = Integer.parseInt(stock.getText().toString().trim().isEmpty() ? "0" : stock.getText().toString().trim());
                n.featured = feat[0];
                n.bestSeller = best[0];
                if (n.name.length() < 2) throw new IllegalArgumentException("Enter a product name");
                if (n.price <= 0) throw new IllegalArgumentException("Price must be above 0");
                if (n.discountPct < 0 || n.discountPct > 90) throw new IllegalArgumentException("Discount must be between 0 and 90");
                if (n.stock < 0) throw new IllegalArgumentException("Stock cannot be negative");
                a.db.saveProduct(n);
                Ui.toast(a, "Product saved");
                a.back();
            } catch (NumberFormatException e) {
                Ui.toast(a, "Enter valid numbers");
            } catch (IllegalArgumentException e) {
                Ui.toast(a, e.getMessage());
            }
        }));
        return Ui.scroll(a, col);
    }

    private EditText field(LinearLayout col, String hint, String value, int type) {
        EditText e = Ui.input(a, hint, type);
        if (value != null) e.setText(value);
        col.addView(Ui.muted(a, hint));
        col.addView(e);
        return e;
    }
}
