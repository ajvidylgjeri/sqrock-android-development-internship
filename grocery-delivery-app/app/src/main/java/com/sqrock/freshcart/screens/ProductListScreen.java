package com.sqrock.freshcart.screens;

import android.view.View;
import android.widget.LinearLayout;
import com.sqrock.freshcart.MainActivity;
import com.sqrock.freshcart.data.Models.Product;
import com.sqrock.freshcart.ui.Ui;
import java.util.List;

/** Products of one category and/or matching a search text. */
public class ProductListScreen implements Screen {
    private final MainActivity a;
    private final String category, query;

    public ProductListScreen(MainActivity a, String category, String query) { this.a = a; this.category = category; this.query = query; }

    @Override public String title() { return category != null ? category : "Search: " + query; }

    @Override public View build() {
        LinearLayout col = Views.page(a);
        List<Product> items = a.db.products(category, query);
        if (items.isEmpty()) return Views.empty(a, "🔍", "No products found.");
        col.addView(Ui.muted(a, items.size() + " products"));
        for (Product p : items) col.addView(Views.row(a, p));
        return Ui.scroll(a, col);
    }
}
