package com.example.jobi.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobi.Grupos.MenuGroup;
import com.example.jobi.Menu;
import com.example.jobi.Model.Group;
import com.example.jobi.R;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private Context mContext;
    private List <Group> mGroup;

    public GroupAdapter(Context mContext, List <Group> mGroup ){
    this.mContext = mContext;
    this.mGroup = mGroup;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        return new GroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Group group = mGroup.get(position);
    holder.username.setText(group.getNombre());
    Glide.with(mContext).load(group.getImagenURL()).into(holder.profile_image);

    holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           Intent intent = new Intent(mContext, MenuGroup.class);
            intent.putExtra("groupId",group.getId());
            mContext.startActivity(intent);
        }
    });

    }

    @Override
    public int getItemCount() {
        return mGroup.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{
        public TextView username;
        public ImageView profile_image;

        public  ViewHolder(View view){
        super(view);

        username = view.findViewById(R.id.nombreusuario);
       profile_image = view.findViewById(R.id.fotouser);


        }

    }


}
