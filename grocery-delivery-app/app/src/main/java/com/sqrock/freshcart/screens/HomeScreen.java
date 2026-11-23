package com.sqrock.freshcart.screens;

import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sqrock.freshcart.MainActivity;
import com.sqrock.freshcart.data.Models;
import com.sqrock.freshcart.data.Models.Product;
import com.sqrock.freshcart.ui.Ui;
import java.util.List;

/** Home tab: search, categories, daily offers, featured and best-selling products. */
public class HomeScreen implements Screen {
    private final MainActivity a;

    public HomeScreen(MainActivity a) { this.a = a; }

    @Override public String title() { return "FreshCart"; }
    @Override public boolean isTab() { return true; }

    @Override public View build() {
        LinearLayout col = Views.page(a);
        TextView hello = Ui.text(a, "Hello, " + a.user.name.split(" ")[0] + " 👋", 20, Ui.text(), true);
        col.addView(hello);
        col.addView(Ui.muted(a, "What would you like to order today?"));

        LinearLayout search = Ui.hbox(a);
        EditText q = Ui.input(a, "Search groceries…", InputType.TYPE_CLASS_TEXT);
        q.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
        q.setOnEditorActionListener((v, id, e) -> { openSearch(q.getText().toString()); return true; });
        search.addView(q, Ui.weight(1));
        search.addView(Ui.primaryButton(a, "Search", v -> openSearch(q.getText().toString())));
        col.addView(search);

        LinearLayout banner = Ui.vbox(a);
        banner.setBackground(Ui.rounded(a, Ui.primary(), 16, 0));
        banner.setPadding(Ui.dp(a, 16), Ui.dp(a, 14), Ui.dp(a, 16), Ui.dp(a, 14));
        banner.addView(Ui.text(a, "Daily offers", 18, android.graphics.Color.WHITE, true));
        banner.addView(Ui.text(a, "Use FRESH10 for 10% off · FREESHIP for free delivery · WELCOME5 for $5 off $20+", 13, android.graphics.Color.WHITE, false));
        col.addView(banner, Ui.margin(a, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 8, 0, 4));

        col.addView(Ui.sectionTitle(a, "Categories"));
        LinearLayout chips = Ui.hbox(a);
        for (String c : Models.CATEGORIES) chips.addView(Ui.chip(a, c, false, v -> a.show(new ProductListScreen(a, c, ""))));
        col.addView(Ui.hscroll(a, chips));

        carousel(col, "Today's offers", a.db.offers());
        carousel(col, "Featured products", a.db.featured());
        carousel(col, "Best sellers", a.db.bestSellers());
        return Ui.scroll(a, col);
    }

    private void openSearch(String query) {
        if (query.trim().isEmpty()) { Ui.toast(a, "Type something to search"); return; }
        a.show(new ProductListScreen(a, null, query.trim()));
    }

    private void carousel(LinearLayout col, String title, List<Product> items) {
        if (items.isEmpty()) return;
        col.addView(Ui.sectionTitle(a, title));
        LinearLayout row = Ui.hbox(a);
        row.setGravity(android.view.Gravity.TOP);
        for (Product p : items) row.addView(Views.card(a, p));
        col.addView(Ui.hscroll(a, row));
    }
}
