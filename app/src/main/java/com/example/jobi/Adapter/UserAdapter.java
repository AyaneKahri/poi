package com.example.jobi.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobi.MessageActivity;
import com.example.jobi.Model.Chat;
import com.example.jobi.Model.User;
import com.example.jobi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List <User> mUsers;
    private boolean isOnline;

    public UserAdapter(Context mContext, List <User> mUsers , boolean isOnline){
    this.mContext = mContext;
    this.mUsers = mUsers;
        this.isOnline = isOnline;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    User user = mUsers.get(position);
    holder.username.setText(user.getUsername());
    Glide.with(mContext).load(user.getImagenURL()).into(holder.profile_image);
        Seen(user.getId(),holder);
    if(user.getStatus().equals("online")){
        holder.img_on.setVisibility(View.VISIBLE);
        holder.img_off.setVisibility(View.GONE);
    }   if(user.getStatus().equals("offline")){
        holder.img_on.setVisibility(View.GONE);
        holder.img_off.setVisibility(View.VISIBLE);
    }

    holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, MessageActivity.class);
            intent.putExtra("id",user.getId());
            mContext.startActivity(intent);
        }
    });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{
        public TextView username;
        public ImageView profile_image;
        public ImageView img_on;
        public ImageView img_off;
        public ImageView noti;
        public  ViewHolder(View view){
        super(view);
        username = view.findViewById(R.id.nombreusuario);
       profile_image = view.findViewById(R.id.fotouser);
            img_on = view.findViewById(R.id.img_on);
            img_off = view.findViewById(R.id.img_off);
            noti = view.findViewById(R.id.notif);
        }

    }

    //Campanita de mensaje
    private void Seen(final String userid, ViewHolder holder ){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String men = snapshot.child("message").getValue().toString();
                    String recividor = snapshot.child("reciver").getValue().toString();
                    String enviador = snapshot.child("sender").getValue().toString();
                    String type = snapshot.child("type").getValue().toString();
                    String visto = snapshot.child("isseen").getValue().toString();
                    Chat chat = new Chat(enviador, recividor,men,type, "", "",visto);
                    if (firebaseUser != null && chat != null) {
                        if (chat.getReciver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ) {
                                if(visto=="true"){
                                    holder.noti.setVisibility(View.GONE);
                                }else{
                                    holder.noti.setVisibility(View.VISIBLE);
                                }
                        }
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
