package com.sqrock.freshcart.screens;

import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sqrock.freshcart.MainActivity;
import com.sqrock.freshcart.data.Db;
import com.sqrock.freshcart.data.Models.User;
import com.sqrock.freshcart.ui.Ui;

/** Login / register. */
public class AuthScreen implements Screen {
    private final MainActivity a;
    private boolean register;

    public AuthScreen(MainActivity a) { this.a = a; }

    @Override public String title() { return "FreshCart"; }

    @Override public View build() {
        LinearLayout col = Ui.vbox(a);
        col.setPadding(Ui.dp(a, 24), Ui.dp(a, 48), Ui.dp(a, 24), Ui.dp(a, 24));
        TextView logo = Ui.text(a, "🥬", 56, Ui.text(), false);
        logo.setGravity(Gravity.CENTER);
        col.addView(logo, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView brand = Ui.text(a, "FreshCart", 28, Ui.primary(), true);
        brand.setGravity(Gravity.CENTER);
        col.addView(brand, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView sub = Ui.muted(a, register ? "Create your account" : "Groceries delivered to your door");
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, Ui.dp(a, 18));
        col.addView(sub, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText name = Ui.input(a, "Full name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        EditText phone = Ui.input(a, "Phone (optional)", InputType.TYPE_CLASS_PHONE);
        EditText email = Ui.input(a, "Email", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText pw = Ui.input(a, "Password (min 8 characters)", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView error = Ui.text(a, "", 13, Ui.RED, false);
        if (register) { col.addView(name); col.addView(phone); }
        col.addView(email);
        col.addView(pw);
        col.addView(error);
        col.addView(Ui.primaryButton(a, register ? "Create account" : "Log in", v -> {
            try {
                Db db = a.db;
                User u;
                if (register) u = db.register(name.getText().toString(), email.getText().toString(), phone.getText().toString(), pw.getText().toString());
                else {
                    u = db.login(email.getText().toString(), pw.getText().toString());
                    if (u == null) throw new IllegalArgumentException("Incorrect email or password");
                }
                a.signIn(u);
            } catch (IllegalArgumentException e) {
                error.setText(e.getMessage());
            }
        }), Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView toggle = Ui.text(a, register ? "Already have an account? Log in" : "New here? Create an account", 14, Ui.primary(), true);
        toggle.setGravity(Gravity.CENTER);
        toggle.setPadding(0, Ui.dp(a, 16), 0, Ui.dp(a, 8));
        toggle.setOnClickListener(v -> { register = !register; a.refresh(); });
        col.addView(toggle, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView demo = Ui.muted(a, "Demo admin: " + Db.ADMIN_EMAIL + " / " + Db.ADMIN_PASSWORD);
        demo.setGravity(Gravity.CENTER);
        col.addView(demo, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return Ui.scroll(a, col);
    }
}
