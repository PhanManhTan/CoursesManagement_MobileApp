package com.example.myapplication.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.CartAdapter;
import com.example.myapplication.data.repository.CartRepository;
import com.example.myapplication.models.Cart;
import com.example.myapplication.models.Course;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private TextView tvTotalPrice;
    private Button btnCheckout;
    private ImageView btnBack;
    
    private CartAdapter adapter;
    private List<Cart> cartItems = new ArrayList<>();
    private CartRepository cartRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        initRepositories();
        setupRecyclerView();

        loadCartData();

        btnBack.setOnClickListener(v -> finish());
        // Trong file CartActivity.java
        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, R.string.cart_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            double total = 0;
            ArrayList<String> cartIds = new ArrayList<>();
            ArrayList<String> courseIds = new ArrayList<>();

            for (Cart item : cartItems) {
                Course course = item.getCourse();
                if (course != null) {
                    total += course.getDiscountPrice();
                }
                cartIds.add(item.getId());
                courseIds.add(item.getCourseId());
            }

            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            intent.putExtra("TOTAL_AMOUNT", total);
            intent.putStringArrayListExtra("CART_IDS", cartIds);
            intent.putStringArrayListExtra("COURSE_IDS", courseIds);
            startActivity(intent);
        });
    }

    private void initViews() {
        rvCart = findViewById(R.id.rvCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initRepositories() {
        cartRepository = new CartRepository(this);
        sessionManager = new SessionManager(this);
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(cartItems);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);

        adapter.setOnItemClickListener(new CartAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(Cart cart, int position) {
                removeFromCart(cart.getId(), position);
            }
        });
    }

    private void loadCartData() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        cartRepository.getByUserId(userId, new CartRepository.RepositoryCallback<List<Cart>>() {
            @Override
            public void onSuccess(List<Cart> data) {
                runOnUiThread(() -> {
                    cartItems.clear();
                    if (data != null) {
                        for (Cart item : data) {
                            if (item.getCourse() != null) {
                                cartItems.add(item);
                            } else {
                                cartRepository.removeFromCart(item.getId(), new CartRepository.RepositoryCallback<Void>() {
                                    @Override public void onSuccess(Void d) {}
                                    @Override public void onError(String m) {}
                                });
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    calculateTotal();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(CartActivity.this, getString(R.string.failed_load_cart, message), Toast.LENGTH_SHORT).show());
            }
        });
    }


    private void removeFromCart(String cartId, int position) {
        cartRepository.removeFromCart(cartId, new CartRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runOnUiThread(() -> {
                    cartItems.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, cartItems.size());
                    calculateTotal();
                    Toast.makeText(CartActivity.this, R.string.item_removed, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(CartActivity.this, getString(R.string.failed_remove_item, message), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void calculateTotal() {
        double total = 0;
        for (Cart item : cartItems) {
            Course course = item.getCourse();
            if (course != null) {
                total += course.getDiscountPrice();
            }
        }
        tvTotalPrice.setText(String.format("%,.0fđ", total));
    }
}
