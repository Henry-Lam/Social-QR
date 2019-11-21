package com.example.profileapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FriendProfileAdapter extends RecyclerView.Adapter<FriendProfileAdapter.ExampleViewHolder>{
    private ArrayList<FriendProfileItem> mExampleList;
    private FriendProfileAdapter.OnItemClickListener_1 mListener;
    static Context FrActivityContext;

    public interface OnItemClickListener_1 {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(FriendProfileAdapter.OnItemClickListener_1 listener){
        mListener = listener;
    }

    public static class ExampleViewHolder extends RecyclerView.ViewHolder{

        public ImageView mImageView;
        public TextView mTextView1;
        public TextView mTextView2;


        public ExampleViewHolder(@NonNull View itemView, final FriendProfileAdapter.OnItemClickListener_1 listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.friendProfilePic);
            mTextView1 = itemView.findViewById(R.id.textView1);
            mTextView2 = itemView.findViewById(R.id.textView2);

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

        }
    }

    public FriendProfileAdapter(ArrayList<FriendProfileItem> exampleList){
        mExampleList = exampleList;

    }

    //crtl + i to generate these methods
    @NonNull
    @Override
    public FriendProfileAdapter.ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_profile_item, parent, false);
        FriendProfileAdapter.ExampleViewHolder evh = new FriendProfileAdapter.ExampleViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendProfileAdapter.ExampleViewHolder holder, int position) {
        FriendProfileItem currentItem = mExampleList.get(position);

        holder.mImageView.setImageResource(currentItem.getmMediaImage());
        holder.mTextView1.setText(currentItem.getmText1());
        holder.mTextView2.setText(currentItem.getmText2());

    }

    @Override
    public int getItemCount() {
        // the return value is how many items to put in recycler view
        return mExampleList.size();
    }
}