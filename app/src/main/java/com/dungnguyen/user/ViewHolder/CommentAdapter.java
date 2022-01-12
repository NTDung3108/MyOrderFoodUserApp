package com.dungnguyen.user.ViewHolder;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dungnguyen.user.Model.Rating;
import com.dungnguyen.user.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    Context mContext;
    ArrayList<Rating> ratingArrayList;

    public CommentAdapter(Context mContext, ArrayList<Rating> ratingArrayList) {
        this.mContext = mContext;
        this.ratingArrayList = ratingArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentAdapter.ViewHolder holder, int position) {
        final Rating rating = ratingArrayList.get(position);

        holder.txtRating.setText(rating.getRateValue()+"");
        holder.txtComment.setText(rating.getComment());

        if (!rating.getImage().equals("default")){
            holder.imgPicture.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(rating.getImage()).into(holder.imgPicture);
        }

    }

    @Override
    public int getItemCount() {
        return ratingArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtCommentUserName, txtRating, txtComment;
        public CircleImageView imgCommentAvatar;
        public ImageView imgPicture;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCommentUserName = itemView.findViewById(R.id.txtCommentUserName);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtComment = itemView.findViewById(R.id.txtComment);
            imgCommentAvatar = itemView.findViewById(R.id.imgCommentAvatar);
            imgPicture = itemView.findViewById(R.id.imgPicture);
        }
    }


}
