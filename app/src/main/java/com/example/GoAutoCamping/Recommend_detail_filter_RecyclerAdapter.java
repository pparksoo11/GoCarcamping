package com.example.GoAutoCamping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Recommend_detail_filter_RecyclerAdapter extends RecyclerView.Adapter<Recommend_detail_filter_RecyclerAdapter.ItemViewHolder>{

    Context context;
    List<Recommend_filterdesDTO> filterData;

    public Recommend_detail_filter_RecyclerAdapter(Context context, List<Recommend_filterdesDTO> filterData){
        this.context = context;
        this.filterData = filterData;
    }
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_detail_filteritem, parent,false);
        ItemViewHolder viewHolder = new ItemViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.image.setImageResource(filterData.get(position).getRecommendFilterImage());
        holder.name.setText(filterData.get(position).getRecommendFilterName());
        holder.description.setText(filterData.get(position).getRecommendFilterDes());
    }

    @Override
    public int getItemCount() {
        return filterData.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{
        private ImageView image;
        private TextView name;
        private TextView description;

        ItemViewHolder(View itemView){
            super (itemView);
            image = itemView.findViewById(R.id.image_filter);
            name = itemView.findViewById(R.id.title_filter);
            description = itemView.findViewById(R.id.description_filter);


        }
    }
}
