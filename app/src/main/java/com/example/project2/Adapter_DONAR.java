package com.example.project2;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class Adapter_DONAR extends RecyclerView.Adapter<Adapter_DONAR.ViewHolder>{

    private List<HandlerRecyclerViewClass_DONAR> itemList;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onClickDeleteItem(int position);
    }


    private static OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public Adapter_DONAR(List<HandlerRecyclerViewClass_DONAR>itemList){
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.back_item_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String getFeedcount = itemList.get(position).getFoodCount();
        String getRestaurantName = itemList.get(position).getRestaurant();
        String getUsernName = itemList.get(position).getUsername();
        Uri uri = itemList.get(position).getImage();


        holder.setData(uri,getFeedcount,getRestaurantName,getUsernName);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView feedCount_textView;
        private TextView res_textView;
        private TextView user_textView;
        private ImageView deleteItem;
        private ImageView addimage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            feedCount_textView = itemView.findViewById(R.id.feed_count);
            res_textView = itemView.findViewById(R.id.restaurantName);
            user_textView = itemView.findViewById(R.id.user_name);
            deleteItem = itemView.findViewById(R.id.deleteItem);
            addimage = itemView.findViewById(R.id.food_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(position);
                    }
                }
            });

            deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onClickDeleteItem(position);
                    }
                }
            });
        }

        public void setData(Uri image,String feed_count,String res,String username) {
            Glide.with(itemView.getContext())
                    .load(image)
                    .apply(new RequestOptions().override(1024, 1024) // set the image size limit
                            .encodeQuality(70)) // set the image compression quality
                    .placeholder(R.drawable.ic_baseline_downloading_24)
                    .into(addimage);
//            addimage.setImageURI(image);
            feedCount_textView.setText(feed_count);
            res_textView.setText(res);
            user_textView.setText(username);
        }
    }
}
