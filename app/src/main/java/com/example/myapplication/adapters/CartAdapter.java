package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Course;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<Course> cartList;

    public CartAdapter(List<Course> cartList) {
        this.cartList = cartList;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Course course = cartList.get(position);

        holder.tvTitle.setText(course.getTitle());

        holder.tvInstructor.setText("Instructor ID: " + course.getInstructorId());

        holder.tvPrice.setText(String.format("$%.2f", course.getPrice()));
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInstructor, tvPrice;
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvInstructor = itemView.findViewById(R.id.tvInstructor);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}