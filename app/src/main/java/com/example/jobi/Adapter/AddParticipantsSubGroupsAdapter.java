package com.example.jobi.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobi.Model.User;
import com.example.jobi.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class AddParticipantsSubGroupsAdapter extends RecyclerView.Adapter<AddParticipantsSubGroupsAdapter.ViewHolder> {
    private Context mContext;
    private List <User> mUsers;
    String group_id;
    String subgroup_id;
    String role;

    public AddParticipantsSubGroupsAdapter(Context mContext, List <User> mUsers, String group_id, String subgroupid,String role ){
    this.mContext = mContext;
    this.mUsers = mUsers;
    this.group_id = group_id;
    this.subgroup_id = subgroupid;
    this.role = role;
    }

    @NonNull
    @Override
    public AddParticipantsSubGroupsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        return new AddParticipantsSubGroupsAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull AddParticipantsSubGroupsAdapter.ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        Glide.with(mContext).load(user.getImagenURL()).into(holder.profile_image);
        holder.img_on.setVisibility(View.GONE);
        holder.img_off.setVisibility(View.GONE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddParticipant(user);
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
        public  ViewHolder(View view){
        super(view);

        username = view.findViewById(R.id.nombreusuario);
       profile_image = view.findViewById(R.id.fotouser);
            img_on = view.findViewById(R.id.img_on);
            img_off = view.findViewById(R.id.img_off);

        }

    }
    private void Ya_es_participante(User modeluser, ViewHolder holder){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(group_id).child("Participantes").child(modeluser.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.child("role").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void AddParticipant(User user){
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("id", user.getId());
        hashMap.put("role", "participante");

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");

    ref.child(group_id).child("SubGroups").child(subgroup_id).child("Participantes").child(user.getId()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
    @Override
    public void onSuccess(Void unused) {
        Toast.makeText(mContext, "Ya es participante",Toast.LENGTH_SHORT).show();
    }
    }).addOnFailureListener(new OnFailureListener() {
    @Override
    public void onFailure(@NonNull Exception e) {
        Toast.makeText(mContext, "No se pudo agregar el participante",Toast.LENGTH_SHORT).show();
    }
    });


    }


}
