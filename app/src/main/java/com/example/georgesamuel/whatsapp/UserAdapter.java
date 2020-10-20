package com.example.georgesamuel.whatsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private ArrayList<User> userList;
    private Context context;
    private UserClickListener clickListener;

    public UserAdapter(Context context, ArrayList<User> userList, UserClickListener clickListener) {
        this.context = context;
        this.userList = userList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        final User user = userList.get(position);
        holder.tvName.setText(user.getName());
        holder.tvStatus.setText(user.getStatus());
        if(!user.getImagePath().equals("")){
            Picasso.get()
                    .load(user.getImagePath())
                    .placeholder(R.drawable.ic_baseline_perm_identity_24)
                    .fit()
                    .into(holder.imgProfile);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onUserCLickedListener(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProfile;
        TextView tvName, tvStatus;

        public UserViewHolder(View view){
            super(view);
            imgProfile = view.findViewById(R.id.imgProfile);
            tvName = view.findViewById(R.id.tvName);
            tvStatus = view.findViewById(R.id.tvStatus);
        }
    }

    interface UserClickListener {

        void onUserCLickedListener(User user);
    }
}
