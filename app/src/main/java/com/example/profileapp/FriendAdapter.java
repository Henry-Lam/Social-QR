package com.example.profileapp;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ExampleViewHolder> {
    private ArrayList<FriendItem> mExampleList;
    private OnItemClickListener_1 mListener;
    static Context FrActivityContext;

    public interface OnItemClickListener_1 {
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener_1 listener){
        mListener = listener;
    }


    public static class ExampleViewHolder extends RecyclerView.ViewHolder{

        public ImageView mImageView;
        public TextView mTextView1;
        public TextView mTextView2;
        public ImageView mDeleteImage;

        public ExampleViewHolder(@NonNull View itemView, final OnItemClickListener_1 listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.friendProfilePic);
            mTextView1 = itemView.findViewById(R.id.textView1);
            mTextView2 = itemView.findViewById(R.id.textView2);
            mDeleteImage = itemView.findViewById(R.id.image_delete);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            mDeleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        FrActivityContext = mDeleteImage.getContext();
                        AlertDialog.Builder builder = new AlertDialog.Builder(FrActivityContext);
                        builder.setTitle("Remove " + mDeleteImage.getTag().toString() + "?");

                        // Set up the buttons
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int position = getAdapterPosition();
                                if(position != RecyclerView.NO_POSITION){
                                    listener.onDeleteClick(position);
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }


    public FriendAdapter(ArrayList<FriendItem> exampleList){
        mExampleList = exampleList;

    }

    //crtl + i to generate these methods
    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
        ExampleViewHolder evh = new ExampleViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull ExampleViewHolder holder, int position) {
        FriendItem currentItem = mExampleList.get(position);

        Glide.with(currentItem.getmContext()).load(currentItem.getmImageUri().toString()).into(holder.mImageView);
        holder.mTextView1.setText(currentItem.getmText1());
        holder.mTextView2.setText(currentItem.getmText2());

        holder.mDeleteImage.setTag(currentItem.getmText1());
        // To specify this user's name in remove alert box
    }

    @Override
    public int getItemCount() {
        // the return value is how many items to put in recycler view
        return mExampleList.size();
    }
}

