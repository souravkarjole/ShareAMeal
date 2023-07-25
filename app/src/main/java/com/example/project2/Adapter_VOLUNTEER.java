package com.example.project2;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter_VOLUNTEER extends RecyclerView.Adapter<Adapter_VOLUNTEER.ViewHolder>{

    private List<HandlerRecyclerViewClass_VOLUNTEER> itemList;

    public interface OnItemClickListener {
        void onItemClick(int position,String id);
        void onClickDeleteItem(int position);
    }

    private static Adapter_VOLUNTEER.OnItemClickListener listener;

    public void setOnItemClickListener(Adapter_VOLUNTEER.OnItemClickListener listener) {
        this.listener = listener;
    }


    public Adapter_VOLUNTEER(List<HandlerRecyclerViewClass_VOLUNTEER>itemList){
        this.itemList = itemList;
    }


    @NonNull
    @Override
    public Adapter_VOLUNTEER.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.front_item_layout,parent,false);
        return new Adapter_VOLUNTEER.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Adapter_VOLUNTEER.ViewHolder holder, int position) {
        String getRestaurantName = itemList.get(position).getRestaurant();

        holder.setData(getRestaurantName);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView res_textView;
        private ConstraintLayout itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            res_textView = itemView.findViewById(R.id.restaurantName);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        String id = itemList.get(position).getId();
                        listener.onItemClick(position,id);
                    }
                }
            });
        }

        public void setData(String res) {
            res_textView.setText(res);
        }
    }
}


