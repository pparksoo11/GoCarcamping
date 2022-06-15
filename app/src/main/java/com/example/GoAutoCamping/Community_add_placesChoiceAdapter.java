package com.example.GoAutoCamping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Community_add_placesChoiceAdapter extends RecyclerView.Adapter<Community_add_placesChoiceAdapter.ViewHolder> {

    private ArrayList<Map_placesChoiceData> choiceData = new ArrayList<>();

    public interface OnItemClickListener{
        void onItemClick(View v, int pos);
    }

    private OnItemClickListener mListener = null;
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle, tvAdd, tvRoadAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.placeItTitle);
            tvAdd = itemView.findViewById(R.id.placeItAdd);
            tvRoadAdd = itemView.findViewById(R.id.placeItRoadAdd);

            //리사이클러뷰 클릭될때
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if(pos!=RecyclerView.NO_POSITION){
                        mListener.onItemClick(view, pos);
                    }
                }
            });
        }
        void onBind(Map_placesChoiceData mcd){
            tvTitle.setText(mcd.getTitle());
            tvAdd.setText(mcd.getAdd());
            tvRoadAdd.setText(mcd.getRoadAdd());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.map_placeitem, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBind(choiceData.get(position));
    }
    public void removeItem(){
        choiceData.clear();
    }

    @Override
    public int getItemCount() {
        return choiceData.size();
    }
    public void setchoiceList(ArrayList<Map_placesChoiceData> list){
        this.choiceData = list;
        notifyDataSetChanged();
    }
}
