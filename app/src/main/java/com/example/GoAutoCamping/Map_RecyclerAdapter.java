package com.example.GoAutoCamping;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

public class Map_RecyclerAdapter extends RecyclerView.Adapter<Map_RecyclerAdapter.ItemViewHolder>{

    List<Map_placedata> placeData;
    private Activity activity;
    private MainActivity ma;
    private int localDataSize;

    //08.10 addItem nullpoint 문제 해결 해야됨

    public Map_RecyclerAdapter(Context context, List<Map_placedata> placeData, int localDataSize){
        this.activity = activity;
        this.placeData = placeData;
        this.localDataSize = localDataSize;
    }

    public interface OnItemClickListener{
        void onItemClick(View v, int position);
    }
    private OnItemClickListener mListener = null;
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }

    @Override
    public int getItemCount() {
        return placeData.size();
    }

    public void removeItem(){
        placeData.clear();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView tvName; //타이틀
        private TextView tvAdd; //주소
        private TextView rate; //별점수
        private RatingBar starCk; //별점

        ItemViewHolder(View itemView){
            super (itemView);
            tvName = itemView.findViewById(R.id.mark_lv_place);
            tvAdd = itemView.findViewById(R.id.mark_lv_add);
            rate = itemView.findViewById(R.id.rateNum);
            starCk = itemView.findViewById(R.id.starRate);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    if(position!=RecyclerView.NO_POSITION){
                        mListener.onItemClick(v, position);
                    }
                }
            });

        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.map_searchlist, parent,false);
        ItemViewHolder viewHolder = new ItemViewHolder(view);

        return viewHolder;
    }


    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Map_placedata data = placeData.get(position);

        holder.tvName.setText(data.getName());
        holder.tvAdd.setText(data.getAdd());
        if(position < localDataSize){

        }
        holder.rate.setText(String.valueOf((float) data.getRate()));
        holder.starCk.setRating((float) data.getRate());

    }
    public Map_placedata getItem(int position){
        return  placeData.get(position);
    }

}



