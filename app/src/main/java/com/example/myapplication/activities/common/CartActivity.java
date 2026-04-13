package com.example.myapplication.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

        RecyclerView rvCart = findViewById(R.id.rvCart);
        TextView tvTotalPrice = findViewById(R.id.tvTotalPrice);
        Button btnCheckout = findViewById(R.id.btnCheckout);

        List<Course> mockCart = new ArrayList<>();
        mockCart.add(new Course(
                "course_01",
                "Advanced Android Development",
                "instructor_123",
                "Khóa học Android nâng cao với Java",
                49.99
        ));
        mockCart.add(new Course(
                "course_02",
                "UI/UX Design for Beginners",
                "instructor_456",
                "Thiết kế giao diện cơ bản",
                29.99
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