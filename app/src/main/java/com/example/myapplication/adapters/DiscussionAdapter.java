package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Comment;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.List;

public class DiscussionAdapter extends RecyclerView.Adapter<DiscussionAdapter.DiscussionViewHolder> {

    private final Context context;
    private List<Comment> commentList;
    private final OnReplyClickListener replyClickListener;

    public interface OnReplyClickListener {
        void onReplyClick(Comment parentComment);
    }

    public DiscussionAdapter(Context context, OnReplyClickListener replyClickListener) {
        this.context = context;
        this.replyClickListener = replyClickListener;
        this.commentList = new ArrayList<>();
    }

    public void submitList(List<Comment> rawList) {
        List<Comment> sortedList = new ArrayList<>();
        if (rawList != null) {
            for (Comment parent : rawList) {
                if (parent.getParentId() == null) {
                    sortedList.add(parent);
                    for (Comment child : rawList) {
                        if (parent.getId().equals(child.getParentId())) {
                            sortedList.add(child);
                        }
                    }
                }
            }
        }
        this.commentList = sortedList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DiscussionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_discussion, parent, false);
        return new DiscussionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscussionViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        if (comment.getUsers() != null) {
            holder.tvUserName.setText(comment.getUsers().getFullName());
            Glide.with(context)
                    .load(comment.getUsers().getAvatarUrl())
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .into(holder.ivUserAvatar);
        } else {
            holder.tvUserName.setText("Unknown User");
        }

        holder.tvCommentContent.setText(comment.getContent());

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.rootDiscussionItem.getLayoutParams();
        if (comment.getParentId() != null) {
            params.setMarginStart(100);
            holder.tvReply.setVisibility(View.GONE);
        } else {
            params.setMarginStart(0);
            holder.tvReply.setVisibility(View.VISIBLE);
        }
        holder.rootDiscussionItem.setLayoutParams(params);

        holder.tvReply.setOnClickListener(v -> replyClickListener.onReplyClick(comment));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class DiscussionViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivUserAvatar;
        TextView tvUserName, tvCommentContent, tvReply;
        LinearLayout rootDiscussionItem;

        public DiscussionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            tvReply = itemView.findViewById(R.id.tvReply);
            rootDiscussionItem = itemView.findViewById(R.id.rootDiscussionItem);
        }
    }
}