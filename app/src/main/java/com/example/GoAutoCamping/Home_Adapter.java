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

public class Home_Adapter extends RecyclerView.Adapter<Home_Adapter.ItemViewHolder> implements Home_OnItemClickListener {
    private List<Home_model> models;
    private Context context;
    ImageView imageView;
    TextView text1;
    Home_OnItemClickListener listener;
    View view;

    public Home_Adapter(List<Home_model> models, Context context) {
        this.models = models;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_carditem, parent,false);
        ItemViewHolder viewHolder = new ItemViewHolder(view, this);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Glide.with(view.getContext())
                .load(models.get(position).getImage())
                .into(imageView);

        text1.setText(models.get(position).getTitle());
    }

    public void setOnItemClicklistener(Home_OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemClick(ItemViewHolder holder, View view, int position) {
        if(listener != null) {
            listener.onItemClick(holder,view,position);
        }
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        ItemViewHolder(View itemView, final Home_OnItemClickListener listener) {
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
