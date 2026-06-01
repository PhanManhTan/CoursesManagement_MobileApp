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
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.models.Cart;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.User;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<Cart> cartList;
    private OnItemClickListener listener;
    private UserRepository userRepository;

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
        if (userRepository == null) {
            userRepository = new UserRepository(parent.getContext());
        }
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart cartItem = cartList.get(position);
        Course course = cartItem.getCourse();

        if (course != null) {
            holder.tvTitle.setText(course.getTitle());

            // Hiển thị trạng thái đang tải tên giảng viên
            holder.tvInstructor.setText(R.string.loading);
            String instructorId = course.getInstructorId();
            holder.tvInstructor.setTag(instructorId);

            userRepository.getById(instructorId, new UserRepository.RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user != null && instructorId.equals(holder.tvInstructor.getTag())) {
                        holder.tvInstructor.setText(holder.itemView.getContext().getString(
                                R.string.instructor_label_format, user.getFullName()));
                    }
                }

                @Override
                public void onError(String message) {
                    if (instructorId.equals(holder.tvInstructor.getTag())) {
                        holder.tvInstructor.setText(R.string.unknown_instructor);
                    }
                }
            });

            // FIX: Luôn ưu tiên lấy discount_price làm giá bán chính thức.
            // Ngay cả khi discount_price = 0 (khóa học miễn phí), nó vẫn sẽ lấy 0đ thay vì quay về giá gốc.
            double displayPrice = course.getDiscountPrice();

            holder.tvPrice.setText(String.format("%,.0fđ", displayPrice));

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
