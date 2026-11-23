package com.sqrock.freshcart.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.sqrock.freshcart.data.Models.*;
import com.sqrock.freshcart.logic.OrderStatus;
import com.sqrock.freshcart.logic.Pricing;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** SQLite storage for users, catalogue, cart, wishlist, addresses and orders. */
public class Db extends SQLiteOpenHelper {
    public static final String ADMIN_EMAIL = "admin@freshcart.app", ADMIN_PASSWORD = "Admin@123";

    public Db(Context c) { super(c, "freshcart.db", null, 1); }

    @Override public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT NOT NULL UNIQUE, phone TEXT, salt TEXT NOT NULL, hash TEXT NOT NULL, role TEXT NOT NULL DEFAULT 'customer')");
        db.execSQL("CREATE TABLE products(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, category TEXT NOT NULL, emoji TEXT, description TEXT, unit TEXT, price REAL NOT NULL, discount INTEGER NOT NULL DEFAULT 0, stock INTEGER NOT NULL DEFAULT 0, featured INTEGER NOT NULL DEFAULT 0, best INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE cart(user_id INTEGER NOT NULL, product_id INTEGER NOT NULL, qty INTEGER NOT NULL, PRIMARY KEY(user_id, product_id), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE wishlist(user_id INTEGER NOT NULL, product_id INTEGER NOT NULL, PRIMARY KEY(user_id, product_id), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE addresses(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, label TEXT NOT NULL, line TEXT NOT NULL, FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE orders(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, subtotal REAL, discount REAL, delivery REAL, total REAL, payment TEXT, address TEXT, coupon TEXT, status TEXT NOT NULL, created_at TEXT, FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE order_items(id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER NOT NULL, name TEXT, emoji TEXT, unit_price REAL, qty INTEGER, FOREIGN KEY(order_id) REFERENCES orders(id) ON DELETE CASCADE)");
        String salt = Security.newSalt();
        ContentValues a = new ContentValues();
        a.put("name", "Store Admin"); a.put("email", ADMIN_EMAIL); a.put("salt", salt); a.put("hash", Security.hash(ADMIN_PASSWORD, salt)); a.put("role", "admin");
        db.insert("users", null, a);
        Object[][] seed = {
            {"Bananas", 0, "🍌", "Ripe Ecuadorian bananas, great for snacks and smoothies.", "1 kg", 1.20, 0, 80, 1, 1},
            {"Red Apples", 0, "🍎", "Crisp and sweet apples.", "1 kg", 2.40, 15, 60, 1, 0},
            {"Tomatoes", 0, "🍅", "Vine-ripened tomatoes.", "500 g", 1.80, 0, 50, 0, 1},
            {"Broccoli", 0, "🥦", "Fresh green broccoli crowns.", "1 piece", 1.60, 10, 30, 0, 0},
            {"Whole Milk", 1, "🥛", "Fresh pasteurised whole milk.", "1 L", 1.10, 0, 70, 1, 1},
            {"Cheddar Cheese", 1, "🧀", "Mature cheddar block.", "250 g", 3.50, 20, 25, 0, 0},
            {"Greek Yogurt", 1, "🥣", "Thick and creamy natural yogurt.", "500 g", 2.20, 0, 40, 0, 1},
            {"Potato Chips", 2, "🥔", "Salted crinkle-cut chips.", "150 g", 1.90, 0, 90, 0, 1},
            {"Dark Chocolate", 2, "🍫", "70% cocoa dark chocolate bar.", "100 g", 2.80, 25, 45, 1, 0},
            {"Mixed Nuts", 2, "🥜", "Roasted unsalted mixed nuts.", "300 g", 5.90, 0, 35, 0, 0},
            {"Orange Juice", 3, "🧃", "100% pressed orange juice.", "1 L", 2.60, 10, 55, 1, 0},
            {"Sparkling Water", 3, "💧", "Natural sparkling mineral water.", "1.5 L", 0.90, 0, 120, 0, 1},
            {"Ground Coffee", 3, "☕", "Medium roast ground coffee.", "250 g", 4.90, 0, 40, 0, 0},
            {"Sourdough Bread", 4, "🍞", "Baked fresh every morning.", "1 loaf", 3.20, 0, 20, 1, 1},
            {"Croissants", 4, "🥐", "Buttery croissants, pack of 4.", "4 pcs", 3.60, 15, 25, 0, 0},
            {"Dish Soap", 5, "🧼", "Lemon-scented dishwashing liquid.", "500 ml", 1.70, 0, 60, 0, 0},
            {"Paper Towels", 5, "🧻", "Absorbent paper towels, 2 rolls.", "2 rolls", 2.90, 10, 50, 0, 0},
            {"Shampoo", 6, "🧴", "Nourishing everyday shampoo.", "400 ml", 4.20, 20, 35, 0, 0},
            {"Toothpaste", 6, "🪥", "Fluoride toothpaste, mint.", "100 ml", 2.10, 0, 80, 0, 1}};
        for (Object[] s : seed) {
            ContentValues v = new ContentValues();
            v.put("name", (String) s[0]); v.put("category", Models.CATEGORIES[(Integer) s[1]]); v.put("emoji", (String) s[2]); v.put("description", (String) s[3]);
            v.put("unit", (String) s[4]); v.put("price", (Double) s[5]); v.put("discount", (Integer) s[6]); v.put("stock", (Integer) s[7]);
            v.put("featured", (Integer) s[8]); v.put("best", (Integer) s[9]);
            db.insert("products", null, v);
        }
    }

    @Override public void onUpgrade(SQLiteDatabase db, int o, int n) { /* version 1 only */ }

    // ---------------- users ----------------
    private static User user(Cursor c) {
        User u = new User();
        u.id = c.getLong(0); u.name = c.getString(1); u.email = c.getString(2); u.phone = c.getString(3); u.role = c.getString(4);
        return u;
    }

    public User register(String name, String email, String phone, String password) {
        name = name == null ? "" : name.trim();
        email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (name.length() < 2) throw new IllegalArgumentException("Enter your name");
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) throw new IllegalArgumentException("Enter a valid email address");
        if (password == null || password.length() < 8) throw new IllegalArgumentException("Password must have at least 8 characters");
        SQLiteDatabase db = getWritableDatabase();
        try (Cursor c = db.rawQuery("SELECT 1 FROM users WHERE email=?", new String[]{email})) {
            if (c.moveToFirst()) throw new IllegalArgumentException("This email is already registered");
        }
        String salt = Security.newSalt();
        ContentValues v = new ContentValues();
        v.put("name", name); v.put("email", email); v.put("phone", phone == null ? "" : phone.trim()); v.put("salt", salt); v.put("hash", Security.hash(password, salt));
        long id = db.insert("users", null, v);
        return user(id);
    }

    public User login(String email, String password) {
        if (email == null || password == null) return null;
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,email,phone,role,salt,hash FROM users WHERE email=?", new String[]{email.trim().toLowerCase(Locale.ROOT)})) {
            if (!c.moveToFirst() || !Security.matches(password, c.getString(5), c.getString(6))) return null;
            return user(c);
        }
    }

    public User user(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,email,phone,role FROM users WHERE id=?", new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? user(c) : null;
        }
    }

    public void updateProfile(long id, String name, String phone) {
        if (name == null || name.trim().length() < 2) throw new IllegalArgumentException("Enter your name");
        ContentValues v = new ContentValues();
        v.put("name", name.trim()); v.put("phone", phone == null ? "" : phone.trim());
        getWritableDatabase().update("users", v, "id=?", new String[]{String.valueOf(id)});
    }

    // ---------------- products ----------------
    private static final String PCOLS = "id,name,category,emoji,description,unit,price,discount,stock,featured,best";

    private static Product product(Cursor c) {
        Product p = new Product();
        p.id = c.getLong(0); p.name = c.getString(1); p.category = c.getString(2); p.emoji = c.getString(3); p.description = c.getString(4); p.unit = c.getString(5);
        p.price = c.getDouble(6); p.discountPct = c.getInt(7); p.stock = c.getInt(8); p.featured = c.getInt(9) == 1; p.bestSeller = c.getInt(10) == 1;
        return p;
    }

    private List<Product> productQuery(String where, String... args) {
        List<Product> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT " + PCOLS + " FROM products" + (where.isEmpty() ? "" : " WHERE " + where) + " ORDER BY name", args)) {
            while (c.moveToNext()) out.add(product(c));
        }
        return out;
    }

    public List<Product> products(String category, String query) {
        String q = query == null ? "" : query.trim().replace("%", "").replace("_", "");
        if (category != null && !q.isEmpty()) return productQuery("category=? AND name LIKE ?", category, "%" + q + "%");
        if (category != null) return productQuery("category=?", category);
        if (!q.isEmpty()) return productQuery("name LIKE ?", "%" + q + "%");
        return productQuery("");
    }

    public List<Product> featured() { return productQuery("featured=1"); }
    public List<Product> bestSellers() { return productQuery("best=1"); }
    public List<Product> offers() { return productQuery("discount>0"); }

    public Product product(long id) {
        List<Product> l = productQuery("id=?", String.valueOf(id));
        return l.isEmpty() ? null : l.get(0);
    }

    public void saveProduct(Product p) {
        if (p.name == null || p.name.trim().isEmpty()) throw new IllegalArgumentException("Product name is required");
        if (!(p.price > 0)) throw new IllegalArgumentException("Price must be greater than 0");
        if (p.discountPct < 0 || p.discountPct > 90) throw new IllegalArgumentException("Discount must be 0-90%");
        if (p.stock < 0) throw new IllegalArgumentException("Stock cannot be negative");
        ContentValues v = new ContentValues();
        v.put("name", p.name.trim()); v.put("category", p.category); v.put("emoji", p.emoji == null || p.emoji.isEmpty() ? "🛒" : p.emoji); v.put("description", p.description == null ? "" : p.description);
        v.put("unit", p.unit == null ? "" : p.unit); v.put("price", p.price); v.put("discount", p.discountPct); v.put("stock", p.stock); v.put("featured", p.featured ? 1 : 0); v.put("best", p.bestSeller ? 1 : 0);
        if (p.id == 0) p.id = getWritableDatabase().insert("products", null, v);
        else getWritableDatabase().update("products", v, "id=?", new String[]{String.valueOf(p.id)});
    }

    public void deleteProduct(long id) { getWritableDatabase().delete("products", "id=?", new String[]{String.valueOf(id)}); }

    // ---------------- cart ----------------
    public List<CartLine> cart(long userId) {
        List<CartLine> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT p.id,p.name,p.category,p.emoji,p.description,p.unit,p.price,p.discount,p.stock,p.featured,p.best,c.qty FROM cart c JOIN products p ON p.id=c.product_id WHERE c.user_id=? ORDER BY p.name", new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) {
                CartLine l = new CartLine();
                l.product = product(c); l.qty = c.getInt(11);
                out.add(l);
            }
        }
        return out;
    }

    public int cartCount(long userId) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT COALESCE(SUM(qty),0) FROM cart WHERE user_id=?", new String[]{String.valueOf(userId)})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    /** Sets the quantity (0 removes the line). Quantity is capped by the stock level. */
    public void setCartQty(long userId, long productId, int qty) {
        SQLiteDatabase db = getWritableDatabase();
        Product p = product(productId);
        if (p == null || qty <= 0) { db.delete("cart", "user_id=? AND product_id=?", new String[]{String.valueOf(userId), String.valueOf(productId)}); return; }
        ContentValues v = new ContentValues();
        v.put("user_id", userId); v.put("product_id", productId); v.put("qty", Math.min(qty, p.stock));
        db.insertWithOnConflict("cart", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void addToCart(long userId, long productId, int qty) {
        int have = 0;
        try (Cursor c = getReadableDatabase().rawQuery("SELECT qty FROM cart WHERE user_id=? AND product_id=?", new String[]{String.valueOf(userId), String.valueOf(productId)})) {
            if (c.moveToFirst()) have = c.getInt(0);
        }
        setCartQty(userId, productId, have + qty);
    }

    // ---------------- wishlist ----------------
    public boolean isFavorite(long userId, long productId) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT 1 FROM wishlist WHERE user_id=? AND product_id=?", new String[]{String.valueOf(userId), String.valueOf(productId)})) {
            return c.moveToFirst();
        }
    }

    public boolean toggleFavorite(long userId, long productId) {
        SQLiteDatabase db = getWritableDatabase();
        if (isFavorite(userId, productId)) {
            db.delete("wishlist", "user_id=? AND product_id=?", new String[]{String.valueOf(userId), String.valueOf(productId)});
            return false;
        }
        ContentValues v = new ContentValues();
        v.put("user_id", userId); v.put("product_id", productId);
        db.insert("wishlist", null, v);
        return true;
    }

    public List<Product> wishlist(long userId) {
        return productQuery("id IN (SELECT product_id FROM wishlist WHERE user_id=?)", String.valueOf(userId));
    }

    // ---------------- addresses ----------------
    public List<Address> addresses(long userId) {
        List<Address> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,label,line FROM addresses WHERE user_id=? ORDER BY id", new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) {
                Address a = new Address();
                a.id = c.getLong(0); a.label = c.getString(1); a.line = c.getString(2);
                out.add(a);
            }
        }
        return out;
    }

    public void addAddress(long userId, String label, String line) {
        if (label == null || label.trim().isEmpty() || line == null || line.trim().length() < 5) throw new IllegalArgumentException("Enter a label and a full address");
        ContentValues v = new ContentValues();
        v.put("user_id", userId); v.put("label", label.trim()); v.put("line", line.trim());
        getWritableDatabase().insert("addresses", null, v);
    }

    public void deleteAddress(long id) { getWritableDatabase().delete("addresses", "id=?", new String[]{String.valueOf(id)}); }

    // ---------------- orders ----------------
    /** Creates an order from the user's cart inside one transaction; stock is checked and decremented. */
    public Order placeOrder(long userId, String address, String payment, String coupon) {
        if (address == null || address.trim().isEmpty()) throw new IllegalArgumentException("Select a delivery address");
        List<CartLine> lines = cart(userId);
        if (lines.isEmpty()) throw new IllegalStateException("Your cart is empty");
        double sub = 0;
        for (CartLine l : lines) {
            if (l.qty > l.product.stock) throw new IllegalStateException(l.product.name + " has only " + l.product.stock + " left");
            sub += l.total();
        }
        Pricing.Totals t = Pricing.totals(sub, coupon);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues o = new ContentValues();
            o.put("user_id", userId); o.put("subtotal", t.subtotal); o.put("discount", t.couponDiscount); o.put("delivery", t.delivery); o.put("total", t.total);
            o.put("payment", payment); o.put("address", address); o.put("coupon", coupon == null ? "" : coupon.trim().toUpperCase(Locale.ROOT)); o.put("status", OrderStatus.CONFIRMED.name());
            o.put("created_at", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date()));
            long oid = db.insertOrThrow("orders", null, o);
            for (CartLine l : lines) {
                ContentValues i = new ContentValues();
                i.put("order_id", oid); i.put("name", l.product.name); i.put("emoji", l.product.emoji); i.put("unit_price", l.product.finalPrice()); i.put("qty", l.qty);
                db.insertOrThrow("order_items", null, i);
                db.execSQL("UPDATE products SET stock = stock - ? WHERE id=?", new Object[]{l.qty, l.product.id});
            }
            db.delete("cart", "user_id=?", new String[]{String.valueOf(userId)});
            db.setTransactionSuccessful();
            return order(oid);
        } finally {
            db.endTransaction();
        }
    }

    private Order orderRow(Cursor c) {
        Order o = new Order();
        o.id = c.getLong(0); o.userId = c.getLong(1); o.subtotal = c.getDouble(2); o.discount = c.getDouble(3); o.delivery = c.getDouble(4); o.total = c.getDouble(5);
        o.payment = c.getString(6); o.address = c.getString(7); o.coupon = c.getString(8); o.status = OrderStatus.parse(c.getString(9)); o.createdAt = c.getString(10); o.customer = c.getString(11);
        try (Cursor i = getReadableDatabase().rawQuery("SELECT name,emoji,unit_price,qty FROM order_items WHERE order_id=?", new String[]{String.valueOf(o.id)})) {
            while (i.moveToNext()) {
                OrderItem it = new OrderItem();
                it.name = i.getString(0); it.emoji = i.getString(1); it.unitPrice = i.getDouble(2); it.qty = i.getInt(3);
                o.items.add(it);
            }
        }
        return o;
    }

    private static final String OSEL = "SELECT o.id,o.user_id,o.subtotal,o.discount,o.delivery,o.total,o.payment,o.address,o.coupon,o.status,o.created_at,u.name FROM orders o JOIN users u ON u.id=o.user_id";

    public Order order(long id) {
        try (Cursor c = getReadableDatabase().rawQuery(OSEL + " WHERE o.id=?", new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? orderRow(c) : null;
        }
    }

    public List<Order> orders(long userId) {
        List<Order> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(OSEL + " WHERE o.user_id=? ORDER BY o.id DESC", new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) out.add(orderRow(c));
        }
        return out;
    }

    public List<Order> allOrders() {
        List<Order> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(OSEL + " ORDER BY o.id DESC", null)) {
            while (c.moveToNext()) out.add(orderRow(c));
        }
        return out;
    }

    public void updateStatus(long orderId, OrderStatus s) {
        ContentValues v = new ContentValues();
        v.put("status", s.name());
        getWritableDatabase().update("orders", v, "id=?", new String[]{String.valueOf(orderId)});
    }

    /** Customer cancellation: allowed only before dispatch; stock is returned. */
    public void cancelOrder(long orderId, long userId) {
        Order o = order(orderId);
        if (o == null || o.userId != userId) throw new IllegalArgumentException("Order not found");
        if (!o.status.canCancel()) throw new IllegalStateException("This order has already been dispatched and cannot be cancelled");
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (OrderItem i : o.items) db.execSQL("UPDATE products SET stock = stock + ? WHERE name=?", new Object[]{i.qty, i.name});
            updateStatus(orderId, OrderStatus.CANCELLED);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public SalesStats salesStats() {
        SalesStats s = new SalesStats();
        for (Order o : allOrders()) {
            s.orders++;
            s.byStatus[o.status.ordinal()]++;
            if (o.status == OrderStatus.CANCELLED) s.cancelled++;
            else s.revenue += o.total;
            if (o.status == OrderStatus.DELIVERED) s.delivered++;
            if (!o.status.isFinal()) s.active++;
        }
        s.revenue = Pricing.round2(s.revenue);
        return s;
    }
}
