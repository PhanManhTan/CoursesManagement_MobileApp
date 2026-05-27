package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Cart;
import com.example.myapplication.models.Course;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<Cart> cartList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(Cart cart, int position);
    }

    public CartAdapter(List<Cart> cartList) {
        this.cartList = cartList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart cartItem = cartList.get(position);
        Course course = cartItem.getCourse();

        if (course != null) {
            holder.tvTitle.setText(course.getTitle());
            holder.tvInstructor.setText("Instructor: " + course.getInstructorId());
            holder.tvPrice.setText(String.format("%,.0fđ", course.getDiscountPrice() > 0 ? course.getDiscountPrice() : course.getPrice()));

            if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(course.getThumbnailUrl())
                        .placeholder(R.drawable.image_courses)
                        .into(holder.ivThumb);
            } else {
                holder.ivThumb.setImageResource(R.drawable.image_courses);
            }
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(cartItem, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList != null ? cartList.size() : 0;
    }

    public void updateData(List<Cart> newList) {
        this.cartList = newList;
        notifyDataSetChanged();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInstructor, tvPrice;
        ImageView ivThumb, btnDelete;
        
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvInstructor = itemView.findViewById(R.id.tvInstructor);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivThumb = itemView.findViewById(R.id.ivCourseThumb);
            btnDelete = itemView.findViewById(R.id.btnRemove);
        }
    }
}