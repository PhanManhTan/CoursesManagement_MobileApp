package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final String[] titles;
    private final String[] descriptions;
    private final int[] images;

    public OnboardingAdapter(String[] titles, String[] descriptions, int[] images) {
        this.titles = titles;
        this.descriptions = descriptions;
        this.images = images;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_onboarding_page, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.tvTitle.setText(titles[position]);
        holder.tvDesc.setText(descriptions[position]);
        holder.ivImage.setImageResource(images[position]);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDesc;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivOnboarding);
            tvTitle = itemView.findViewById(R.id.tvOnboardingTitle);
            tvDesc = itemView.findViewById(R.id.tvOnboardingDesc);
        }
    }
}
