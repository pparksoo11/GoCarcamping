package com.example.GoAutoCamping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


public class Recommend_Adapter extends RecyclerView.Adapter<Recommend_Adapter.ItemViewHolder> {
    private List<Recommend_hotDTO> models;
    private Context context;
    ImageView imageView;
    TextView text1;
    Home_OnItemClickListener listener;
    View view;

    public interface Recommend_OnItemClickListener {
        void onItemClick(ItemViewHolder holder, View view, int pos);
    }

    public Recommend_OnItemClickListener recommend_listener;

    public void setOnItemClickListener(Recommend_OnItemClickListener a_listener) {
        recommend_listener = a_listener;
    }

    public Recommend_Adapter(List<Recommend_hotDTO> models, Context context) {
        this.models = models;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_carditem2, parent,false);
        ItemViewHolder viewHolder = new ItemViewHolder(view, recommend_listener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Glide.with(view.getContext())
                .load(models.get(position).getRecommendImage())
                .into(imageView);

        text1.setText(models.get(position).getRecommendTitle());
    }


    @Override
    public int getItemCount() {
        return models.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        ItemViewHolder(View itemView, final Recommend_OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            text1 = itemView.findViewById(R.id.text1);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if(listener != null) {
                        listener.onItemClick(ItemViewHolder.this, view, pos);
                    }
                }
            });
        }
    }

}
