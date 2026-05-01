package com.example.myapplication.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapters.CartAdapter;
import com.example.myapplication.models.Course;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_activity);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView rvCart = findViewById(R.id.rvCart);
        TextView tvTotalPrice = findViewById(R.id.tvTotalPrice);
        Button btnCheckout = findViewById(R.id.btnCheckout);

        List<Course> mockCart = new ArrayList<>();
        mockCart.add(new Course(
                "course_01",
                "instructor_123",
                "Advanced Android Development",
                "Khóa học Android nâng cao với Java",
                "https://via.placeholder.com/150",
                49.99,
                39.99,
                "published",
                "cat_android",
                "2023-10-01"
        ));
        mockCart.add(new Course(
                "course_02",
                "instructor_456",
                "UI/UX Design for Beginners",
                "Thiết kế giao diện cơ bản",
                "https://via.placeholder.com/150",
                29.99,
                19.99,
                "published",
                "cat_design",
                "2023-10-05"
        ));

        double total = 0;
        for (Course c : mockCart) {
            total += c.getPrice();
        }
        tvTotalPrice.setText(String.format("$%.2f", total));

        CartAdapter adapter = new CartAdapter(mockCart);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);

        btnCheckout.setOnClickListener(v -> {
            startActivity(new Intent(CartActivity.this, CheckoutActivity.class));
        });
    }
}
